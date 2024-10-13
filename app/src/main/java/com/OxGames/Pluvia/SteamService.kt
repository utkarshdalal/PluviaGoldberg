package com.OxGames.Pluvia

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.OxGames.Pluvia.data.SteamData
import com.OxGames.Pluvia.enums.LoginResult
import com.OxGames.Pluvia.events.EventDispatcher
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.utils.FileUtils
import com.OxGames.Pluvia.utils.SteamUtils
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
import `in`.dragonbra.javasteam.steam.steamclient.callbacks.CMListCallback
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.Closeable
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.pathString


class SteamService : Service(), IChallengeUrlChanged {
    private var _steamClient: SteamClient? = null
    private var _callbackManager: CallbackManager? = null
    private var _steamUser: SteamUser? = null
    private var _steamApps: SteamApps? = null
    private var _steamFriends: SteamFriends? = null
    // private var _steamContent = null // TODO: add SteamContent to Java SteamKit

    private val _callbackSubscriptions: ArrayList<Closeable> = ArrayList()

    private var _loginResult: LoginResult = LoginResult.Failed

    private var retryAttempt = 0

    companion object {
        const val MAX_RETRY_ATTEMPTS = 5
        const val LOGIN_ID = 382945

        private var steamData: SteamData = SteamData()
        private var instance: SteamService? = null

        var isConnecting: Boolean = false
            private set
        var isStopping: Boolean = false
            private set
        var isConnected: Boolean = false
            private set
        var isRunning: Boolean = false
            private set
        var isLoggingIn: Boolean = false
            private set
        // var isLoggingOut: Boolean = false
        //     private set
        var isWaitingForQRAuth: Boolean = false
            private set
        var isReceivingLicenseList: Boolean = false
            private set
        var isRequestingPkgInfo: Boolean = false
            private set
        var isRequestingAppInfo: Boolean = false
            private set

        private fun getServerListPath(): String {
            return Paths.get(instance!!.cacheDir.path, "server_list.bin").pathString
        }
        private fun getSteamDataPath(): String {
            return Paths.get(instance!!.dataDir.path, "steam_data.json").pathString
        }
        private fun loadSteamData() {
            val steamDataStr: String? = FileUtils.readFileAsString(getSteamDataPath())
            if (steamDataStr != null)
                steamData = Json.decodeFromString<SteamData>(steamDataStr)
        }
        private fun saveSteamData() {
            FileUtils.writeStringToFile(Json.encodeToString(steamData), getSteamDataPath())
        }

        private fun login(username: String, accessToken: String? = null, refreshToken: String? = null, password: String? = null, shouldRememberPassword: Boolean = false, twoFactorAuth: String? = null, emailAuth: String? = null) {
            val steamUser = instance!!._steamUser!!

            Log.d("SteamService", "Login Information\n\tUsername: $username\n\tAccessToken: $accessToken\n\tRefreshToken: $refreshToken\n\tPassword: $password\n\tShouldRememberPass: $shouldRememberPassword\n\tTwoFactorAuth: $twoFactorAuth\n\tEmailAuth: $emailAuth")

            steamData.accountName = username
            if ((password != null && shouldRememberPassword) || refreshToken != null) {
                if (password != null)
                    steamData.password = password
                if (accessToken != null) {
                    steamData.password = null
                    steamData.accessToken = accessToken
                }
                if (refreshToken != null) {
                    steamData.password = null
                    steamData.refreshToken = refreshToken
                }
            }

            isLoggingIn = true
            PluviaApp.events.emit(SteamEvent.LogonStarted(username))
            steamUser.logOn(LogOnDetails(
                // Steam strips all non-ASCII characters from usernames and passwords
                // source: https://github.com/steevp/UpdogFarmer/blob/8f2d185c7260bc2d2c92d66b81f565188f2c1a0e/app/src/main/java/com/steevsapps/idledaddy/LoginActivity.java#L166C9-L168C104
                // more: https://github.com/winauth/winauth/issues/368#issuecomment-224631002
                username = SteamUtils.removeSpecialChars(username).trim(),
                password = if (password != null) SteamUtils.removeSpecialChars(password).trim() else null,
                shouldRememberPassword = shouldRememberPassword,
                twoFactorCode = twoFactorAuth,
                authCode = emailAuth,
                accessToken = refreshToken,
                // Set LoginID to a non-zero value if you have another client connected using the same account,
                // the same private ip, and same public ip.
                // source: https://github.com/Longi94/JavaSteam/blob/08690d0aab254b44b0072ed8a4db2f86d757109b/javasteam-samples/src/main/java/in/dragonbra/javasteamsamples/_000_authentication/SampleLogonAuthentication.java#L146C13-L147C56
                loginID = LOGIN_ID
            ))
        }
        fun logOn(username: String, password: String, shouldRememberPassword: Boolean = false, twoFactorAuth: String? = null, emailAuth: String? = null) {
            CoroutineScope(Dispatchers.Default).launch {
                login(
                    username = username,
                    password = password,
                    shouldRememberPassword = shouldRememberPassword,
                    twoFactorAuth = twoFactorAuth,
                    emailAuth = emailAuth
                )
            }
        }
        fun startLoginWithQr() {
            CoroutineScope(Dispatchers.IO).launch {
                val steamClient = instance!!._steamClient
                if (steamClient != null) {
                    isWaitingForQRAuth = true
                    val authSession = steamClient.authentication.beginAuthSessionViaQR(AuthSessionDetails())
                    // Steam will periodically refresh the challenge url, this callback allows you to draw a new qr code.
                    authSession.challengeUrlChanged = instance
                    PluviaApp.events.emit(SteamEvent.QrChallengeReceived(authSession.challengeUrl))

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
                    PluviaApp.events.emit(SteamEvent.QrAuthEnded(authPollResult != null))

                    // there is a chance qr got cancelled and there is no authPollResult
                    if (authPollResult != null) {
                        login(
                            username = authPollResult.accountName,
                            accessToken = authPollResult.accessToken,
                            refreshToken = authPollResult.refreshToken
                        )
                    }
                } else {
                    Log.e("SteamService", "Could not start QR logon: Failed to connect to Steam")
                    PluviaApp.events.emit(SteamEvent.QrAuthEnded(false))
                }
            }
        }
        fun stopLoginWithQr() {
            isWaitingForQRAuth = false
        }
        fun logOut() {
            CoroutineScope(Dispatchers.Default).launch {
                isConnected = false
                // isLoggingOut = true
                performLogOffDuties()
                val steamUser = instance!!._steamUser!!
                steamUser.logOff()
            }
        }
        private fun clearUserData() {
            steamData.cellId = 0
            steamData.accountName = null
            steamData.accessToken = null
            steamData.refreshToken = null
            steamData.password = null
            saveSteamData()
            isLoggingIn = false
        }
        private fun performLogOffDuties() {
            val username = steamData.accountName
            clearUserData()
            PluviaApp.events.emit(SteamEvent.LoggedOut(username))
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
            loadSteamData()

            Log.d("SteamService", "Using server list path: ${getServerListPath()}")
            val configuration =
                SteamConfiguration.create { iSteamConfigurationBuilder: ISteamConfigurationBuilder ->
                    iSteamConfigurationBuilder.withCellID(steamData.cellId)
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
            _callbackSubscriptions.add(_callbackManager!!.subscribe(CMListCallback::class.java, this::onCMList))
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
            isStopping = true
            _steamClient!!.disconnect()
            while (isStopping)
                delay(200L)
            // the reason we don't clearValues() here is because the onDisconnect
            // callback does it for us
        } else
            clearValues()
    }
    private fun clearValues()
    {
        _loginResult = LoginResult.Failed
        isRunning = false
        isConnected = false
        isConnecting = false
        isLoggingIn = false
        isWaitingForQRAuth = false
        isReceivingLicenseList = false
        isRequestingPkgInfo = false
        isRequestingAppInfo = false

        steamData = SteamData()
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

        isStopping = false
        retryAttempt = 0

        PluviaApp.events.clearAllListenersOf<SteamEvent>()
    }

    private fun onConnected(callback: ConnectedCallback) {
        Log.d("SteamService", "Connected to Steam")
        retryAttempt = 0
        isConnecting = false
        isConnected = true
        var isAutoLoggingIn = false
        loadSteamData()
        if (_loginResult != LoginResult.TwoFactorCode && _loginResult != LoginResult.EmailAuth) {
            if (steamData.accountName != null && (steamData.refreshToken != null || steamData.password != null)) {
                isAutoLoggingIn = true
                login(
                    username = steamData.accountName!!,
                    refreshToken = steamData.refreshToken,
                    password = steamData.password,
                    shouldRememberPassword = steamData.password != null
                )
            }
        }
        PluviaApp.events.emit(SteamEvent.Connected(isAutoLoggingIn))
    }
    private fun onCMList(callback: CMListCallback) {
        Log.d("SteamService", "CMListCallback:\nJobID: ${callback.jobID}\nServerList: ${callback.servers}")
    }
    private fun onDisconnected(callback: DisconnectedCallback) {
        Log.d("SteamService", "Disconnected from Steam")
        isConnected = false
        if (!isStopping && retryAttempt < MAX_RETRY_ATTEMPTS) {
            retryAttempt++
            Log.d("SteamService", "Attempting to reconnect (retry $retryAttempt)")
            // isLoggingOut = false
            connectToSteam()
        } else {
            PluviaApp.events.emit(SteamEvent.Disconnected)
            clearValues()
            stopSelf()
        }
    }

    private fun reconnect() {
        isConnected = false
        isConnecting = true
        PluviaApp.events.emit(SteamEvent.Disconnected)
        _steamClient!!.disconnect()
    }

    private fun onLoggedOn(callback: LoggedOnCallback) {
        Log.d("SteamService", "Logged onto Steam: ${callback.result}")
        var logonSucess = false
        val username = steamData.accountName

        when (callback.result) {
            EResult.AccountLogonDenied -> {
                _loginResult = LoginResult.EmailAuth
                reconnect()
            }
            EResult.AccountLoginDeniedNeedTwoFactor -> {
                _loginResult = LoginResult.TwoFactorCode
                reconnect()
            }
            EResult.TryAnotherCM -> {
                _loginResult = LoginResult.TryAgain
                reconnect()
            }
            EResult.OK -> {
                logonSucess = true
                // save the current cellid somewhere. if we lose our saved server list, we can use this when retrieving
                // servers from the Steam Directory.
                steamData.cellId = callback.cellID
                saveSteamData()

                isReceivingLicenseList = true // since we automatically receive the license list from steam on log on
                _steamFriends!!.requestFriendInfo(_steamUser!!.steamID) // in order to get user avatar url and other info
                _loginResult = LoginResult.Success
            }
            else -> {
                clearUserData()
                _loginResult = LoginResult.Failed
                reconnect()
            }
        }

        PluviaApp.events.emit(SteamEvent.LogonEnded(username, _loginResult))
        isLoggingIn = false
    }

    private fun onLoggedOff(callback: LoggedOffCallback) {
        Log.d("SteamService", "Logged off of Steam: ${callback.result}")
        performLogOffDuties()
    }

    override fun onChanged(qrAuthSession: QrAuthSession?) {
        Log.d("SteamService", "QR code changed: ${qrAuthSession?.challengeUrl}")
        if (qrAuthSession != null)
            PluviaApp.events.emit(SteamEvent.QrChallengeReceived(qrAuthSession.challengeUrl))
    }
}