package com.OxGames.Pluvia

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.OxGames.Pluvia.enums.LoginResult
import com.OxGames.Pluvia.events.EventDispatcher
import com.OxGames.Pluvia.events.SteamEvent
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.steam.authentication.AuthPollResult
import `in`.dragonbra.javasteam.steam.authentication.AuthSessionDetails
import `in`.dragonbra.javasteam.steam.authentication.IChallengeUrlChanged
import `in`.dragonbra.javasteam.steam.authentication.QrAuthSession
import `in`.dragonbra.javasteam.steam.discovery.FileServerListProvider
import `in`.dragonbra.javasteam.steam.handlers.steamapps.SteamApps
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends
import `in`.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails
import `in`.dragonbra.javasteam.steam.handlers.steamuser.SteamUser
import `in`.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback
import `in`.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager
import `in`.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback
import `in`.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback
import `in`.dragonbra.javasteam.steam.steamclient.configuration.ISteamConfigurationBuilder
import `in`.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration
import `in`.dragonbra.javasteam.util.log.DefaultLogListener
import `in`.dragonbra.javasteam.util.log.LogManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.Closeable
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.nio.file.Paths
import java.util.Scanner
import kotlin.io.path.pathString


class SteamService : Service(), IChallengeUrlChanged {
    private var _steamClient: SteamClient? = null
    private var _callbackManager: CallbackManager? = null
    private var _steamUser: SteamUser? = null
    private var _steamApps: SteamApps? = null
    private var _steamFriends: SteamFriends? = null
    // private var _steamContent = null // TODO: add SteamContent to Java SteamKit
    private var _accountName: String? = null
    private var _accessToken: String? = null
    private var _refreshToken: String? = null

    private val _callbackSubscriptions: ArrayList<Closeable> = ArrayList()

    private var _loginResult: LoginResult = LoginResult.Failed

    private var retryAttempt = 0

    companion object {
        const val MAX_RETRY_ATTEMPTS = 5
        const val LOGIN_ID = 382945

        private var cellID = 0
        private var instance: SteamService? = null
        val events: EventDispatcher = EventDispatcher()

        var isConnecting: Boolean = false
            private set
        var isDisconnecting: Boolean = false
            private set
        var isRunning: Boolean = false
            private set
        var isLoggingIn: Boolean = false
            private set
        var isWaitingForQRAuth: Boolean = false
            private set
        var isReceivingLicenseList: Boolean = false
            private set
        var isRequestingPkgInfo: Boolean = false
            private set
        var isRequestingAppInfo: Boolean = false
            private set

        private fun saveStringToFile(path: String, data: String, errorTag: String? = null, errorMessage: ((e: Exception) -> String)? = null) {
            try {
                FileWriter(path).use { fw ->
                    fw.write(data)
                }
            } catch (e: Exception) {
                Log.e(errorTag ?: "FileError", errorMessage?.invoke(e) ?: "Could not write to $path: $e")
            }
        }
        private fun getCellIdPath(): String {
            return Paths.get(instance!!.cacheDir.path, "cellid.txt").pathString
        }
        private fun getServerListPath(): String {
            return Paths.get(instance!!.cacheDir.path, "server_list.bin").pathString
        }
        private fun getAccountNamePath(): String {
            return Paths.get(instance!!.cacheDir.path, "account_name.txt").pathString
        }
        private fun getAccessTokenPath(): String {
            return Paths.get(instance!!.cacheDir.path, "access_token.txt").pathString
        }
        private fun getRefreshTokenPath(): String {
            return Paths.get(instance!!.cacheDir.path, "refresh_token.txt").pathString
        }
        fun startLoginWithQr() {
            CoroutineScope(Dispatchers.IO).launch {
                val steamClient = instance!!._steamClient
                val steamUser = instance!!._steamUser
                if (steamClient != null && steamUser != null) {
                    isWaitingForQRAuth = true
                    val authSession = steamClient.authentication.beginAuthSessionViaQR(AuthSessionDetails())
                    // Steam will periodically refresh the challenge url, this callback allows you to draw a new qr code.
                    authSession.challengeUrlChanged = instance
                    events.emit(SteamEvent.QrChallengeReceived(authSession.challengeUrl))

                    Log.d("SteamService", "PollingInterval: ${authSession.pollingInterval.toLong()}")
                    var authPollResult: AuthPollResult? = null
                    while (isWaitingForQRAuth && authPollResult == null) {
                        try {
                            authPollResult = authSession.pollAuthSessionStatus()
                        } catch(e: Exception) {
                            Log.w("SteamService", "Poll auth session status error: $e")
                            break
                        }
                        if (authPollResult != null)
                            Log.d("SteamService", "AccessToken: ${authPollResult.accessToken}\nAccountName: ${authPollResult.accountName}\nRefreshToken: ${authPollResult.refreshToken}\nNewGuardData: ${authPollResult.newGuardData ?: "No new guard data"}")
                        else
                            Log.d("SteamService", "AuthPollResult is null")
                        delay(authSession.pollingInterval.toLong())
                    }
                    isWaitingForQRAuth = false
                    events.emit(SteamEvent.QrAuthEnded(authPollResult != null))

                    // there is a chance qr got cancelled and there is no authPollResult
                    if (authPollResult != null) {
                        instance!!._accountName = authPollResult.accountName
                        instance!!._accessToken = authPollResult.accessToken
                        instance!!._refreshToken = authPollResult.refreshToken

                        isLoggingIn = true
                        steamUser.logOn(LogOnDetails(
                            username = authPollResult.accountName,
                            accessToken = authPollResult.refreshToken,
                            // Set LoginID to a non-zero value if you have another client connected using the same account,
                            // the same private ip, and same public ip.
                            // source: https://github.com/Longi94/JavaSteam/blob/08690d0aab254b44b0072ed8a4db2f86d757109b/javasteam-samples/src/main/java/in/dragonbra/javasteamsamples/_000_authentication/SampleLogonAuthentication.java#L146C13-L147C56
                            loginID = LOGIN_ID
                        ))
                    }
                } else {
                    Log.e("SteamService", "Could not start QR logon: Failed to connect to Steam")
                    events.emit(SteamEvent.QrAuthEnded(false))
                }
            }
        }
        fun stopLoginWithQr() {
            isWaitingForQRAuth = false
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // to view log messages in logcat
        LogManager.addListener(DefaultLogListener())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            // if we've previously connected and saved our cellid, load it.
            val loginKeyFile = File(getCellIdPath())
            Log.d("SteamService", "Checking for existence of: ${getCellIdPath()}")
            if (loginKeyFile.exists()) {
                try {
                    Scanner(loginKeyFile).use { s ->
                        cellID = s.nextLine().toInt()
                    }
                } catch (e: FileNotFoundException) {
                    Log.e("SteamService", "Error parsing cellid from cellid.txt. Continuing with cellid 0.")
                }
                Log.d("SteamService", "Using persisted cell ID: $cellID")
            } else {
                Log.e("SteamService", "cellid file doesn't exist.")
            }

            Log.d("SteamService", "Using server list path: ${getServerListPath()}")
            val configuration =
                SteamConfiguration.create { iSteamConfigurationBuilder: ISteamConfigurationBuilder ->
                    iSteamConfigurationBuilder.withCellID(cellID)
                    iSteamConfigurationBuilder.withServerListProvider(FileServerListProvider(File(getServerListPath())))
                }

            // create our steam client instance
            _steamClient = SteamClient(configuration)
            // create the callback manager which will route callbacks to function calls
            _callbackManager = CallbackManager(_steamClient!!)
            // get the different handlers to be used throughout the service
            _steamUser = _steamClient!!.getHandler(SteamUser::class.java)
            _steamApps = _steamClient!!.getHandler(SteamApps::class.java)
            _steamFriends = _steamClient!!.getHandler(SteamFriends::class.java)

            // subscribe to the callbacks we are interested in
            _callbackSubscriptions.add(_callbackManager!!.subscribe(ConnectedCallback::class.java, this::onConnected))
            _callbackSubscriptions.add(_callbackManager!!.subscribe(DisconnectedCallback::class.java, this::onDisconnected))
            _callbackSubscriptions.add(_callbackManager!!.subscribe(LoggedOnCallback::class.java, this::onLoggedOn))
            _callbackSubscriptions.add(_callbackManager!!.subscribe(LoggedOffCallback::class.java, this::onLoggedOff))

            isRunning = true

            // we should use Dispatchers.IO here since we are running a sleeping/blocking function
            // "The idea is that the IO dispatcher spends a lot of time waiting (IO blocked),
            // while the Default dispatcher is intended for CPU intensive tasks, where there
            // is little or no sleep."
            // source: https://stackoverflow.com/a/59040920
            CoroutineScope(Dispatchers.IO).launch {
                while (isRunning) {
                    // Log.d("SteamService", "runWaitCallbacks")
                    try {
                        _callbackManager!!.runWaitCallbacks(1000L)
                    } catch (e: Exception) {
                        Log.e("SteamService", "runWaitCallbacks failed: $e")
                    }
                }
            }

            connectToSteam()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        CoroutineScope(Dispatchers.IO).launch {
            stop()
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun connectToSteam() {
        isConnecting = true
        CoroutineScope(Dispatchers.Default).launch {
            // this call errors out if run on the main thread
            _steamClient!!.connect()
        }
    }

    private suspend fun stop()
    {
        Log.d("SteamService", "Stopping Steam service")
        if (_steamClient != null && _steamClient!!.isConnected) {
            isDisconnecting = true
            _steamClient!!.disconnect()
            while (isDisconnecting)
                delay(200L)
            // the reason we don't clearValues() here is because the onDisconnect
            // callback does it for us
        } else
            clearValues()
    }
    private fun clearValues()
    {
        // _loginResult = LoginResult.Failed
        isRunning = false
        isConnecting = false
        isLoggingIn = false
        isWaitingForQRAuth = false
        isReceivingLicenseList = false
        isRequestingPkgInfo = false
        isRequestingAppInfo = false

        // _steamData = null
        _steamClient = null
        _steamUser = null
        _steamApps = null
        _steamFriends = null
        // _steamContent = null

        for (subscription in _callbackSubscriptions)
            subscription.close()
        _callbackSubscriptions.clear()
        _callbackManager = null

        // packageInfo.Clear()
        // appInfo.Clear()
        // personaStates.Clear()

        isDisconnecting = false
        retryAttempt = 0

        events.clearAllListeners()
    }

    private fun readFileAsString(path: String): String? {
        var result: String? = null

        val file = File(path)
        Log.d("SteamService", "Checking for existence of: $path")
        if (file.exists()) {
            try {
                Scanner(file).use { s ->
                    result = s.nextLine()
                }
            } catch (e: FileNotFoundException) {
                Log.e("SteamService", "Error parsing file $path")
            }
            Log.d("SteamService", "Successfully read contents of $path")
        } else {
            Log.e("SteamService", "File doesn't exist $path")
        }
        return result
    }
    private fun onConnected(callback: ConnectedCallback) {
        Log.d("SteamService", "Connected to Steam")
        retryAttempt = 0
        isConnecting = false
        // TODO: check if there is a remembered user and log them in
        _accountName = readFileAsString(getAccountNamePath())
        _accessToken = readFileAsString(getAccessTokenPath())
        _refreshToken = readFileAsString(getRefreshTokenPath())
        if (_accountName != null && _refreshToken != null) {
            isLoggingIn = true
            events.emit(SteamEvent.LogonStarted(_accountName!!))
            _steamUser!!.logOn(LogOnDetails(
                username = _accountName!!,
                accessToken = _refreshToken,
                // Set LoginID to a non-zero value if you have another client connected using the same account,
                // the same private ip, and same public ip.
                // source: https://github.com/Longi94/JavaSteam/blob/08690d0aab254b44b0072ed8a4db2f86d757109b/javasteam-samples/src/main/java/in/dragonbra/javasteamsamples/_000_authentication/SampleLogonAuthentication.java#L146C13-L147C56
                loginID = LOGIN_ID
            ))
        }
        events.emit(SteamEvent.Connected)
    }

    private fun onDisconnected(callback: DisconnectedCallback) {
        Log.d("SteamService", "Disconnected from Steam")
        if (!isDisconnecting && retryAttempt < MAX_RETRY_ATTEMPTS) {
            retryAttempt++
            Log.d("SteamService", "Attempting to reconnect (retry $retryAttempt)")
            connectToSteam()
        } else {
            events.emit(SteamEvent.Disconnected)
            clearValues()
            stopSelf()
        }
    }

    private fun onLoggedOn(callback: LoggedOnCallback) {
        Log.d("SteamService", "Logged onto Steam: ${callback.result}")
        val isSteamGuard = callback.result == EResult.AccountLogonDenied
        val is2FA = callback.result == EResult.AccountLoginDeniedNeedTwoFactor
        var logonSucess = false

        if (is2FA)
            _loginResult = LoginResult.TwoFactorCode
        else if (isSteamGuard)
            _loginResult = LoginResult.EmailAuth
        else if (callback.result != EResult.OK)
            _loginResult = LoginResult.Failed
        else
        {
            logonSucess = true
            // save the current cellid somewhere. if we lose our saved server list, we can use this when retrieving
            // servers from the Steam Directory.
            saveStringToFile(getCellIdPath(), java.lang.String.valueOf(callback.cellID), "SteamService") { "Failed to write cellId to file: $it" }
            saveStringToFile(getAccountNamePath(), _accountName!!, "SteamService") { "Failed to write accountName to file: $it" }
            saveStringToFile(getAccessTokenPath(), _accessToken!!, "SteamService") { "Failed to write accessToken to file: $it" }
            saveStringToFile(getRefreshTokenPath(), _refreshToken!!, "SteamService") { "Failed to write refreshToken to file: $it" }

            isReceivingLicenseList = true // since we automatically receive the license list from steam on log on
            _steamFriends!!.requestFriendInfo(_steamUser!!.steamID) // in order to get user avatar url and other info
            _loginResult = LoginResult.Success
        }

        events.emit(SteamEvent.LogonEnded(_accountName ?: "", logonSucess))
        isLoggingIn = false
    }

    private fun onLoggedOff(callback: LoggedOffCallback) {
        Log.d("SteamService", "Logged off of Steam: ${callback.result}")

        // isRunning = false
    }

    override fun onChanged(qrAuthSession: QrAuthSession?) {
        Log.d("SteamService", "QR code changed: ${qrAuthSession?.challengeUrl}")
        if (qrAuthSession != null)
            events.emit(SteamEvent.QrChallengeReceived(qrAuthSession.challengeUrl))
    }
}