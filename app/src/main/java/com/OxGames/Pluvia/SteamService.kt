package com.OxGames.Pluvia

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.room.withTransaction
import com.OxGames.Pluvia.data.AppInfo
import com.OxGames.Pluvia.data.ConfigInfo
import com.OxGames.Pluvia.data.DepotInfo
import com.OxGames.Pluvia.data.DownloadInfo
import com.OxGames.Pluvia.data.LaunchInfo
import com.OxGames.Pluvia.data.LibraryAssetsInfo
import com.OxGames.Pluvia.data.LibraryCapsuleInfo
import com.OxGames.Pluvia.data.LibraryHeroInfo
import com.OxGames.Pluvia.data.LibraryLogoInfo
import com.OxGames.Pluvia.data.ManifestInfo
import com.OxGames.Pluvia.data.PackageInfo
import com.OxGames.Pluvia.data.SaveFile
import com.OxGames.Pluvia.data.SteamData
import com.OxGames.Pluvia.data.SteamFriend
import com.OxGames.Pluvia.data.UFS
import com.OxGames.Pluvia.db.PluviaDatabase
import com.OxGames.Pluvia.enums.AppType
import com.OxGames.Pluvia.enums.ControllerSupport
import com.OxGames.Pluvia.enums.Language
import com.OxGames.Pluvia.enums.LoginResult
import com.OxGames.Pluvia.enums.OS
import com.OxGames.Pluvia.enums.OSArch
import com.OxGames.Pluvia.enums.PathType
import com.OxGames.Pluvia.enums.ReleaseState
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.utils.FileUtils
import com.OxGames.Pluvia.utils.SteamUtils
import dagger.hilt.android.AndroidEntryPoint
import `in`.dragonbra.javasteam.enums.EClientPersonaStateFlag
import `in`.dragonbra.javasteam.enums.EPersonaState
import `in`.dragonbra.javasteam.enums.EPersonaStateFlag
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.networking.steam3.ProtocolTypes
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.CChat_RequestFriendPersonaStates_Request
import `in`.dragonbra.javasteam.rpc.service.Chat
import `in`.dragonbra.javasteam.steam.authentication.AuthPollResult
import `in`.dragonbra.javasteam.steam.authentication.AuthSessionDetails
import `in`.dragonbra.javasteam.steam.authentication.IAuthenticator
import `in`.dragonbra.javasteam.steam.authentication.IChallengeUrlChanged
import `in`.dragonbra.javasteam.steam.authentication.QrAuthSession
import `in`.dragonbra.javasteam.steam.contentdownloader.ContentDownloader
import `in`.dragonbra.javasteam.steam.contentdownloader.FileManifestProvider
import `in`.dragonbra.javasteam.steam.discovery.FileServerListProvider
import `in`.dragonbra.javasteam.steam.discovery.ServerQuality
import `in`.dragonbra.javasteam.steam.handlers.steamapps.PICSRequest
import `in`.dragonbra.javasteam.steam.handlers.steamapps.SteamApps
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.LicenseListCallback
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.PICSProductInfoCallback
import `in`.dragonbra.javasteam.steam.handlers.steamcloud.SteamCloud
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.FriendsListCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.NicknameListCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.PersonaStatesCallback
import `in`.dragonbra.javasteam.steam.handlers.steamgameserver.SteamGameServer
import `in`.dragonbra.javasteam.steam.handlers.steammasterserver.SteamMasterServer
import `in`.dragonbra.javasteam.steam.handlers.steamscreenshots.SteamScreenshots
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages
import `in`.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails
import `in`.dragonbra.javasteam.steam.handlers.steamuser.SteamUser
import `in`.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback
import `in`.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback
import `in`.dragonbra.javasteam.steam.handlers.steamuserstats.SteamUserStats
import `in`.dragonbra.javasteam.steam.handlers.steamworkshop.SteamWorkshop
import `in`.dragonbra.javasteam.steam.steamclient.SteamClient
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager
import `in`.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback
import `in`.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback
import `in`.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration
import `in`.dragonbra.javasteam.types.KeyValue
import `in`.dragonbra.javasteam.types.SteamID
import `in`.dragonbra.javasteam.util.NetHelpers
import `in`.dragonbra.javasteam.util.log.DefaultLogListener
import `in`.dragonbra.javasteam.util.log.LogManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.Request
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.EnumSet
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipInputStream
import javax.inject.Inject
import kotlin.io.path.pathString

@AndroidEntryPoint
class SteamService : Service(), IChallengeUrlChanged {

    @Inject
    lateinit var db: PluviaDatabase

    private var _callbackManager: CallbackManager? = null
    private var _steamClient: SteamClient? = null
    private var _steamUser: SteamUser? = null
    private var _steamApps: SteamApps? = null
    private var _steamFriends: SteamFriends? = null
    private var _steamCloud: SteamCloud? = null
    private var _unifiedMessages: SteamUnifiedMessages? = null
    private var _unifiedChat: Chat? = null

    private val _callbackSubscriptions: ArrayList<Closeable> = ArrayList()

    private var _loginResult: LoginResult = LoginResult.Failed

    private var retryAttempt = 0

    private val packageInfo = ConcurrentHashMap<Int, PackageInfo>()
    private val appInfo = ConcurrentHashMap<Int, AppInfo>()

    private val dbScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val MAX_RETRY_ATTEMPTS = 20
        const val LOGIN_ID = 382945
        const val AVATAR_BASE_URL = "https://steamcdn-a.akamaihd.net/steamcommunity/public/images/avatars/"
        const val MISSING_AVATAR_URL = "${AVATAR_BASE_URL}fe/fef49e7fa7e1997310d705b2a6158ff8dc1cdfeb_full.jpg"
        const val INVALID_APP_ID: Int = Int.MAX_VALUE
        const val INVALID_DEPOT_ID: Int = Int.MAX_VALUE
        const val INVALID_MANIFEST_ID: Long = Long.MAX_VALUE
        private val PROTOCOL_TYPES = EnumSet.of(ProtocolTypes.TCP, ProtocolTypes.UDP)

        private var steamData: SteamData? = null
        private var instance: SteamService? = null

        private val downloadJobs = ConcurrentHashMap<Int, DownloadInfo>()

        // var isLoggingOut: Boolean = false
        //     private set

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
        val isLoggedIn: Boolean
            get() = instance?._steamClient?.steamID?.run { isValid } == true
        var isWaitingForQRAuth: Boolean = false
            private set
        var isReceivingLicenseList: Boolean = false
            private set
        var isRequestingPkgInfo: Boolean = false
            private set
        var isRequestingAppInfo: Boolean = false
            private set

        private val serverListPath: String
            get() = Paths.get(instance!!.cacheDir.path, "server_list.bin").pathString

        private val depotManifestsPath: String
            get() = Paths.get(instance!!.dataDir.path, "Steam", "depot_manifests.zip").pathString

        private val steamDataPath: String
            get() = Paths.get(instance!!.dataDir.path, "steam_data.json").pathString

        private val defaultAppInstallPath: String
            get() = Paths.get(instance!!.dataDir.path, "Steam", "steamapps", "common").pathString

        private val defaultAppStagingPath: String
            get() = Paths.get(instance!!.dataDir.path, "Steam", "steamapps", "staging").pathString

        private fun loadSteamData() {
            val steamDataStr = FileUtils.readFileAsString(steamDataPath)
            steamData = if (steamDataStr != null) {
                Json.decodeFromString<SteamData>(steamDataStr)
            } else {
                SteamData(
                    appInstallPath = defaultAppInstallPath,
                    appStagingPath = defaultAppStagingPath
                )
            }
        }

        private fun saveSteamData() {
            FileUtils.writeStringToFile(Json.encodeToString(steamData), steamDataPath)
        }

        fun requestUserPersona() {
            CoroutineScope(Dispatchers.Default).launch {
                getUserSteamId()?.let {
                    // in order to get user avatar url and other info
                    instance?._steamFriends?.requestFriendInfo(it)
                }
            }
        }

        fun getUserSteamId(): SteamID? = instance?._steamClient?.steamID

        fun getPersonaStateOf(steamId: SteamID): SteamFriend? = runBlocking {
            instance!!.db
                .steamFriendDao()
                .findFriend(steamId.convertToUInt64())
                .first()
        }

        fun getAppList(filter: EnumSet<AppType>): List<AppInfo> =
            instance?.appInfo?.values?.filter { filter.contains(it.type) } ?: emptyList()

        fun getPkgInfoOf(appId: Int): PackageInfo? = instance?.packageInfo?.values?.firstOrNull {
            // Log.d("SteamService", "Pkg (${it.packageId}) apps: ${it.appIds.joinToString(",")}")
            it.appIds.contains(appId)
        }

        fun getAppInfoOf(appId: Int): AppInfo? = instance?.appInfo?.values?.firstOrNull {
            it.appId == appId
        }

        fun getAppDownloadInfo(appId: Int): DownloadInfo? = downloadJobs[appId]

        fun isAppInstalled(appId: Int): Boolean = Files.exists(Paths.get(getAppDirPath(appId)))

        fun getAppRawDirPath(appId: Int): String = Paths.get(
            steamData?.appInstallPath ?: "",
            getAppInfoOf(appId)?.config?.installDir ?: ""
        ).pathString

        fun getAppDirPath(appId: Int): String {
            val origPath = getAppRawDirPath(appId)
            return origPath.trim().replace(" ", "_")
        }

        fun downloadApp(appId: Int): DownloadInfo? {
            val appInfo = getAppInfoOf(appId)
            val pkgInfo = getPkgInfoOf(appId)
            val depotId = pkgInfo?.depotIds?.firstOrNull {
                appInfo?.depots?.get(it)?.osList?.contains(OS.windows) == true
            }
            return if (depotId != null) {
                appInfo?.appId?.let {
                    downloadApp(it, depotId, "public")
                }
            } else {
                Log.e(
                    "SteamService",
                    "Failed to download app (${appInfo?.appId}), could not find appropriate depot"
                )

                null
            }
        }

        fun downloadApp(appId: Int, depotId: Int, branch: String): DownloadInfo? {
            if (downloadJobs.contains(appId)) {
                Log.e(
                    "SteamService",
                    "Could not start new download job for $appId since one already exists"
                )

                return getAppDownloadInfo(appId)
            }

            val downloadInfo = DownloadInfo().also { downloadInfo ->
                downloadInfo.setDownloadJob(
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            ContentDownloader(instance!!._steamClient!!).downloadApp(
                                appId,
                                depotId,
                                steamData!!.appInstallPath,
                                steamData!!.appStagingPath,
                                branch,
                                // maxDownloads = 1,
                                onDownloadProgress = { downloadInfo.setProgress(it) },
                                parentScope = coroutineContext.job as CoroutineScope
                            ).await()
                            // rename directory to our specification
                            val origPath = getAppRawDirPath(appId)
                            val newPath = getAppDirPath(appId)
                            if (origPath != newPath && Files.exists(Paths.get(origPath))) {
                                File(origPath).renameTo(File(newPath))
                            }
                        } finally {
                            /* Nothing */
                        }

                        downloadJobs.remove(appId)
                    }
                )
            }

            downloadJobs[appId] = downloadInfo

            return downloadInfo
        }

        /**
         * Default timeout to use when making requests
         */
        var requestTimeout = 10000L

        /**
         * Default timeout to use when reading the response body
         */
        var responseBodyTimeout = 60000L
        fun downloadUserFiles(
            appId: Int,
            parentScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
            prefixToPath: (String) -> String,
        ) = parentScope.async {
            instance?.let { steamInstance ->
                steamInstance._steamCloud?.let { steamCloud ->
                    steamInstance.appInfo[appId]?.let { appInfo ->
                        Log.d("SteamService", "Retrieving save files of ${appInfo.name}")
                        val appFileListChange = steamCloud.cloudService.getAppFileListChange(appId)
                        val pathTypePairs = appFileListChange.pathPrefixes
                            .map {
                                val matchResults = Regex("%\\w+%").findAll(it).map { it.value }.toList()
                                Log.d("SteamService", "Mapping prefix $it and found $matchResults")
                                matchResults
                            }.flatten()
                            .distinct()
                            .map {
                                Pair(it, prefixToPath(it))
                            }
                        val convertedPrefixes = appFileListChange.pathPrefixes.map { prefix ->
                            var modified = prefix
                            pathTypePairs.forEach {
                                modified = modified.replace(it.first, it.second)
                            }
                            modified
                        }
                        // Log.d("SteamService",
                        //     "GetAppFileListChange($appId):" +
                        //     "\n\tTotal Files: ${appFileListChange.files.size}" +
                        //     "\n\tCurrent Change Number: ${appFileListChange.currentChangeNumber}" +
                        //     "\n\tIs Only Delta: ${appFileListChange.isOnlyDelta}" +
                        //     "\n\tApp BuildID Hwm: ${appFileListChange.appBuildIDHwm}" +
                        //     "\n\tPath Prefixes: \n\t\t${appFileListChange.pathPrefixes.joinToString("\n\t\t")}" +
                        //     "\n\tMachine Names: \n\t\t${appFileListChange.machineNames.joinToString("\n\t\t")}" +
                        //     appFileListChange.files.map {
                        //         "\n\t${it.filename}:" +
                        //         "\n\t\tshaFile: ${it.shaFile}" +
                        //         "\n\t\ttimestamp: ${it.timestamp}" +
                        //         "\n\t\trawFileSize: ${it.rawFileSize}" +
                        //         "\n\t\tpersistState: ${it.persistState}" +
                        //         "\n\t\tplatformsToSync: ${it.platformsToSync}" +
                        //         "\n\t\tpathPrefixIndex: ${it.pathPrefixIndex}" +
                        //         "\n\t\tmachineNameIndex: ${it.machineNameIndex}"
                        //     }.joinToString()
                        // )
                        appFileListChange.files.forEach { file ->
                            val prefixedPath = if (file.pathPrefixIndex < appFileListChange.pathPrefixes.size)
                                "${appFileListChange.pathPrefixes[file.pathPrefixIndex]}${file.filename}"
                            else
                                file.filename
                            val fileDownloadInfo = steamCloud.cloudService.clientFileDownload(appId, prefixedPath)
                            // Log.d("SteamService",
                            //     "ClientFileDownload($appId, $prefixedPath):" +
                            //     "\n\tappId: ${fileDownloadInfo.appID}" +
                            //     "\n\tfileSize: ${fileDownloadInfo.fileSize}" +
                            //     "\n\trawFileSize: ${fileDownloadInfo.rawFileSize}" +
                            //     "\n\tshaFile: ${fileDownloadInfo.shaFile}" +
                            //     "\n\ttimestamp: ${fileDownloadInfo.timestamp}" +
                            //     "\n\tisExplicitDelete: ${fileDownloadInfo.isExplicitDelete}" +
                            //     "\n\turlHost: ${fileDownloadInfo.urlHost}" +
                            //     "\n\turlPath: ${fileDownloadInfo.urlPath}" +
                            //     "\n\tuseHttps: ${fileDownloadInfo.useHttps}" +
                            //     "\n\trequestHeaders: ${fileDownloadInfo.requestHeaders}" +
                            //     "\n\tencrypted: ${fileDownloadInfo.encrypted}"
                            // )

                            val actualFilePath = if (file.pathPrefixIndex < appFileListChange.pathPrefixes.size)
                                Paths.get(convertedPrefixes[file.pathPrefixIndex], file.filename)
                            else
                                Paths.get(getAppDirPath(appId), file.filename)
                            Log.d("SteamService", "$prefixedPath -> $actualFilePath")

                            if (fileDownloadInfo.urlHost.isNotEmpty()) {
                                // val httpUrl = HttpUrl.Builder()
                                //     .scheme(if (fileDownloadInfo.useHttps) "https" else "http")
                                //     .host(fileDownloadInfo.urlHost)
                                //     .addPathSegments(fileDownloadInfo.urlPath)
                                //     .build()
                                val scheme = if (fileDownloadInfo.useHttps) "https://" else "http://"
                                val httpUrl = "$scheme${fileDownloadInfo.urlHost}${fileDownloadInfo.urlPath}"
                                Log.d("SteamService", "Downloading $httpUrl")
                                val request = Request.Builder().url(httpUrl).build()
                                val httpClient = steamInstance._steamClient!!.configuration.httpClient
                                withTimeout(requestTimeout) {
                                    val response = httpClient.newCall(request).execute()

                                    if (!response.isSuccessful) {
                                        Log.e("SteamService", "File download of $prefixedPath was unsuccessful")
                                        return@withTimeout
                                    }

                                    val copyToFile: (InputStream) -> Unit = { input ->
                                        Files.createDirectories(actualFilePath.parent)
                                        FileOutputStream(actualFilePath.toString()).use { fs ->
                                            val bytesRead = input.copyTo(fs)
                                            if (bytesRead != fileDownloadInfo.rawFileSize.toLong()) {
                                                Log.e("SteamService", "Bytes read from stream of $prefixedPath does not match expected size")
                                            }
                                        }
                                    }

                                    withTimeout(responseBodyTimeout) {
                                        // var hadZipEntries = true
                                        // val responseBytes = response.body?.bytes()
                                        if (fileDownloadInfo.fileSize != fileDownloadInfo.rawFileSize) {
                                            response.body?.byteStream()?.use { inputStream ->
                                                ZipInputStream(inputStream).use { zipInput ->
                                                    val entry = zipInput.nextEntry
                                                    if (entry == null) {
                                                        Log.w("SteamService", "Downloaded user file $prefixedPath has no zip entries")
                                                        return@withTimeout
                                                    }

                                                    copyToFile(zipInput)
                                                    if (zipInput.nextEntry != null) {
                                                        Log.e("SteamService", "Downloaded user file $prefixedPath has more than one zip entry")
                                                    }
                                                }
                                            }
                                        } else {
                                            response.body?.byteStream()?.use { inputStream ->
                                                copyToFile(inputStream)
                                            }
                                        }
                                    }
                                }
                            } else {
                                Log.w("SteamService", "URL host of $prefixedPath was empty")
                            }
                        }
                    }
                }
            }
        }

        fun getAvatarURL(avatarHash: String): String {
            return avatarHash.ifEmpty { null }
                ?.takeIf { str -> str.isNotEmpty() && !str.all { it == '0' } }
                ?.let { "${AVATAR_BASE_URL}${it.substring(0, 2)}/${it}_full.jpg" }
                ?: MISSING_AVATAR_URL
        }

        fun printAllKeyValues(parent: KeyValue, depth: Int = 0) {
            var tabString = ""
            for (i in 0..depth) {
                tabString += "\t"
            }

            if (parent.children.isNotEmpty()) {
                Log.d("SteamService", "$tabString${parent.name}")
                for (child in parent.children) {
                    printAllKeyValues(child, depth + 1)
                }
            } else {
                Log.d("SteamService", "$tabString${parent.name}: ${parent.value}")
            }
        }

        private fun login(
            username: String,
            accessToken: String? = null,
            refreshToken: String? = null,
            password: String? = null,
            shouldRememberPassword: Boolean = false,
            twoFactorAuth: String? = null,
            emailAuth: String? = null
        ) {
            val steamUser = instance!!._steamUser!!

            Log.d(
                "SteamService",
                "Login Information\n\tUsername: $username\n\tAccessToken: $accessToken\n\tRefreshToken: $refreshToken\n\tPassword: $password\n\tShouldRememberPass: $shouldRememberPassword\n\tTwoFactorAuth: $twoFactorAuth\n\tEmailAuth: $emailAuth"
            )

            steamData!!.accountName = username
            if ((password != null && shouldRememberPassword) || refreshToken != null) {
                if (password != null)
                    steamData!!.password = password
                if (accessToken != null) {
                    steamData!!.password = null
                    steamData!!.accessToken = accessToken
                }
                if (refreshToken != null) {
                    steamData!!.password = null
                    steamData!!.refreshToken = refreshToken
                }
            }

            isLoggingIn = true
            PluviaApp.events.emit(SteamEvent.LogonStarted(username))
            steamUser.logOn(
                LogOnDetails(
                    // Steam strips all non-ASCII characters from usernames and passwords
                    // source: https://github.com/steevp/UpdogFarmer/blob/8f2d185c7260bc2d2c92d66b81f565188f2c1a0e/app/src/main/java/com/steevsapps/idledaddy/LoginActivity.java#L166C9-L168C104
                    // more: https://github.com/winauth/winauth/issues/368#issuecomment-224631002
                    username = SteamUtils.removeSpecialChars(username).trim(),
                    password = if (password != null) SteamUtils.removeSpecialChars(password)
                        .trim() else null,
                    shouldRememberPassword = shouldRememberPassword,
                    twoFactorCode = twoFactorAuth,
                    authCode = emailAuth,
                    accessToken = refreshToken,
                    // Set LoginID to a non-zero value if you have another client connected using the same account,
                    // the same private ip, and same public ip.
                    // source: https://github.com/Longi94/JavaSteam/blob/08690d0aab254b44b0072ed8a4db2f86d757109b/javasteam-samples/src/main/java/in/dragonbra/javasteamsamples/_000_authentication/SampleLogonAuthentication.java#L146C13-L147C56
                    loginID = LOGIN_ID
                )
            )
        }

        fun startLoginWithCredentials(
            username: String,
            password: String,
            shouldRememberPassword: Boolean,
            authenticator: IAuthenticator,
        ) {
            Log.d("SteamService", "Logging in via credentials.")
            CoroutineScope(Dispatchers.IO).launch {
                val steamClient = instance!!._steamClient
                if (steamClient != null) {
                    val authDetails = AuthSessionDetails().apply {
                        this.username = username.trim()
                        this.password = password.trim()
                        this.persistentSession = shouldRememberPassword
                        this.authenticator = authenticator
                    }

                    val authSession = steamClient.authentication
                        .beginAuthSessionViaCredentials(authDetails)

                    PluviaApp.events.emit(SteamEvent.LogonStarted(username))

                    val pollResult = authSession.pollingWaitForResult()

                    if (pollResult.accountName.isNotEmpty() && pollResult.refreshToken.isNotEmpty()) {
                        login(
                            username = pollResult.accountName,
                            accessToken = pollResult.accessToken,
                            refreshToken = pollResult.refreshToken,
                            shouldRememberPassword = shouldRememberPassword,
                        )
                    }
                } else {
                    Log.e("SteamService", "Could not logon: Failed to connect to Steam")
                    PluviaApp.events.emit(SteamEvent.LogonEnded(username, LoginResult.Failed))
                }
            }
        }

        fun startLoginWithQr() {
            Log.d("SteamService", "Logging in via QR.")
            CoroutineScope(Dispatchers.IO).launch {
                val steamClient = instance!!._steamClient
                if (steamClient != null) {
                    isWaitingForQRAuth = true

                    val authSession = steamClient.authentication
                        .beginAuthSessionViaQR(AuthSessionDetails())

                    // Steam will periodically refresh the challenge url, this callback allows you to draw a new qr code.
                    authSession.challengeUrlChanged = instance
                    PluviaApp.events.emit(SteamEvent.QrChallengeReceived(authSession.challengeUrl))

                    Log.d(
                        "SteamService",
                        "PollingInterval: ${authSession.pollingInterval.toLong()}"
                    )
                    var authPollResult: AuthPollResult? = null
                    while (isWaitingForQRAuth && authPollResult == null) {
                        try {
                            authPollResult = authSession.pollAuthSessionStatus()
                        } catch (e: Exception) {
                            Log.w("SteamService", "Poll auth session status error: $e")
                            break
                        }

                        if (authPollResult != null) {
                            Log.d(
                                "SteamService",
                                "AccessToken: ${authPollResult.accessToken}\nAccountName: ${authPollResult.accountName}\nRefreshToken: ${authPollResult.refreshToken}\nNewGuardData: ${authPollResult.newGuardData ?: "No new guard data"}"
                            )
                        } else {
                            // Log.d("SteamService", "AuthPollResult is null")
                        }

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
            Log.d("SteamService", "Stopping QR polling")
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
            steamData!!.cellId = 0
            steamData!!.accountName = null
            steamData!!.accessToken = null
            steamData!!.refreshToken = null
            steamData!!.password = null
            saveSteamData()
            isLoggingIn = false
        }

        private fun performLogOffDuties() {
            val username = steamData!!.accountName
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

            Log.d("SteamService", "Using server list path: $serverListPath")
            val configuration = SteamConfiguration.create {
                it.withProtocolTypes(PROTOCOL_TYPES)
                it.withCellID(steamData!!.cellId)
                it.withServerListProvider(FileServerListProvider(File(serverListPath)))
                it.withManifestProvider(FileManifestProvider(File(depotManifestsPath)))
            }

            // create our steam client instance
            _steamClient = SteamClient(configuration)

            // remove callbacks we're not using.
            _steamClient!!.removeHandler(SteamGameServer::class.java)
            _steamClient!!.removeHandler(SteamMasterServer::class.java)
            _steamClient!!.removeHandler(SteamWorkshop::class.java)
            _steamClient!!.removeHandler(SteamScreenshots::class.java)
            _steamClient!!.removeHandler(SteamUserStats::class.java)

            // create the callback manager which will route callbacks to function calls
            _callbackManager = CallbackManager(_steamClient!!)
            _unifiedMessages = _steamClient!!.getHandler(SteamUnifiedMessages::class.java)

            // get the different handlers to be used throughout the service
            _steamUser = _steamClient!!.getHandler(SteamUser::class.java)
            _steamApps = _steamClient!!.getHandler(SteamApps::class.java)
            _steamFriends = _steamClient!!.getHandler(SteamFriends::class.java)
            _steamCloud = _steamClient!!.getHandler(SteamCloud::class.java)

            // subscribe to the callbacks we are interested in
            _callbackSubscriptions.add(
                _callbackManager!!.subscribe(
                    ConnectedCallback::class.java,
                    this::onConnected
                )
            )
            _callbackSubscriptions.add(
                _callbackManager!!.subscribe(
                    DisconnectedCallback::class.java,
                    this::onDisconnected
                )
            )
            _callbackSubscriptions.add(
                _callbackManager!!.subscribe(
                    LoggedOnCallback::class.java,
                    this::onLoggedOn
                )
            )
            _callbackSubscriptions.add(
                _callbackManager!!.subscribe(
                    LoggedOffCallback::class.java,
                    this::onLoggedOff
                )
            )
            _callbackSubscriptions.add(
                _callbackManager!!.subscribe(
                    PersonaStatesCallback::class.java,
                    this::onPersonaStateReceived
                )
            )
            _callbackSubscriptions.add(
                _callbackManager!!.subscribe(
                    LicenseListCallback::class.java,
                    this::onLicenseList
                )
            )
            _callbackSubscriptions.add(
                _callbackManager!!.subscribe(
                    PICSProductInfoCallback::class.java,
                    this::onPICSProductInfo
                )
            )
            _callbackSubscriptions.add(
                _callbackManager!!.subscribe(
                    NicknameListCallback::class.java,
                    this::onNicknameList
                )
            )
            _callbackSubscriptions.add(
                _callbackManager!!.subscribe(
                    FriendsListCallback::class.java,
                    this::onFriendsList
                )
            )

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

    override fun onBind(intent: Intent?): IBinder? = null

    private fun connectToSteam() {
        isConnecting = true
        CoroutineScope(Dispatchers.Default).launch {
            // this call errors out if run on the main thread
            _steamClient!!.connect()

            delay(5000)
            if (!isConnected) {
                Log.w(
                    "SteamService",
                    "Failed to connect to Steam, marking endpoint bad and force disconnecting"
                )
                try {
                    _steamClient!!.servers.tryMark(
                        _steamClient!!.currentEndpoint,
                        PROTOCOL_TYPES,
                        ServerQuality.BAD
                    )
                } catch (e: Exception) {
                    Log.e("SteamService", "Failed to mark endpoint as bad: $e")
                }
                try {
                    _steamClient!!.disconnect()
                } catch (e: Exception) {
                    Log.e("SteamService", "There was an issue when disconnecting: $e")
                }
            }
        }
    }

    private suspend fun stop() {
        Log.d("SteamService", "Stopping Steam service")
        if (_steamClient != null && _steamClient!!.isConnected) {
            isStopping = true
            _steamClient!!.disconnect()
            while (isStopping) {
                delay(200L)
            }
            // the reason we don't clearValues() here is because the onDisconnect
            // callback does it for us
        } else {
            clearValues()
        }
    }

    private fun clearValues() {
        _loginResult = LoginResult.Failed
        isRunning = false
        isConnected = false
        isConnecting = false
        isLoggingIn = false
        isWaitingForQRAuth = false
        isReceivingLicenseList = false
        isRequestingPkgInfo = false
        isRequestingAppInfo = false

        steamData = SteamData(
            appInstallPath = defaultAppInstallPath,
            appStagingPath = defaultAppStagingPath
        )
        _steamClient = null
        _steamUser = null
        _steamApps = null
        _steamFriends = null
        _steamCloud = null

        for (subscription in _callbackSubscriptions) {
            subscription.close()
        }

        _callbackSubscriptions.clear()
        _callbackManager = null

        _unifiedMessages = null
        _unifiedChat = null

        packageInfo.clear()
        appInfo.clear()

        isStopping = false
        retryAttempt = 0

        PluviaApp.events.clearAllListenersOf<SteamEvent<Any>>()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onConnected(callback: ConnectedCallback) {
        Log.d("SteamService", "Connected to Steam")
        retryAttempt = 0
        isConnecting = false
        isConnected = true

        var isAutoLoggingIn = false

        loadSteamData()

        if (steamData!!.accountName != null &&
            (steamData!!.refreshToken != null || steamData!!.password != null)
        ) {
            isAutoLoggingIn = true
            login(
                username = steamData!!.accountName!!,
                refreshToken = steamData!!.refreshToken,
                password = steamData!!.password,
                shouldRememberPassword = steamData!!.password != null
            )
        }

        PluviaApp.events.emit(SteamEvent.Connected(isAutoLoggingIn))
    }

    @Suppress("UNUSED_PARAMETER")
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

    /**
     * Request a fresh state of Friend's PersonaStates
     */
    private fun refreshPersonaStates() {
        val request = CChat_RequestFriendPersonaStates_Request.newBuilder().build()
        _unifiedChat?.requestFriendPersonaStates(request)
    }

    private fun onLoggedOn(callback: LoggedOnCallback) {
        Log.d("SteamService", "Logged onto Steam: ${callback.result}")
        val username = steamData!!.accountName

        when (callback.result) {
            EResult.TryAnotherCM -> {
                reconnect()
            }

            EResult.OK -> {
                // save the current cellid somewhere. if we lose our saved server list, we can use this when retrieving
                // servers from the Steam Directory.
                steamData!!.cellId = callback.cellID
                saveSteamData()

                // Create Unified Handlers
                _unifiedChat = _unifiedMessages!!.createService(Chat::class.java)

                // retrieve persona data of logged in user
                requestUserPersona()

                // since we automatically receive the license list from steam on log on
                isReceivingLicenseList = true

                // TODO: Preference last known status?
                // Tell steam we're online, this allows friends to update.
                _steamFriends?.setPersonaState(EPersonaState.Online)

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
        if (qrAuthSession != null) {
            PluviaApp.events.emit(SteamEvent.QrChallengeReceived(qrAuthSession.challengeUrl))
        }
    }

    private fun onNicknameList(callback: NicknameListCallback) {
        Log.d("SteamService", "Nickname list called: ${callback.nicknames.size}")
        dbScope.launch {
            db.withTransaction {
                db.steamFriendDao().clearAllNicknames()
                db.steamFriendDao().updateNicknames(callback.nicknames)
            }
        }
    }

    private fun onFriendsList(callback: FriendsListCallback) {
        Log.d("SteamService", "onFriendsList ${callback.friendList.size}")
        dbScope.launch {
            db.withTransaction {
                callback.friendList.filter { friend ->
                    friend.steamID.isIndividualAccount
                }.forEach { filteredFriend ->
                    val friendId = filteredFriend.steamID.convertToUInt64()
                    val friend = db.steamFriendDao().findFriend(friendId).first()

                    if (friend == null) {
                        // Not in the DB, create them.
                        val friendToAdd = SteamFriend(
                            id = filteredFriend.steamID.convertToUInt64(),
                            relation = filteredFriend.relationship.code()
                        )

                        db.steamFriendDao().insert(friendToAdd)
                    } else {
                        // In the DB, update them.
                        db.steamFriendDao().update(
                            friend.copy(relation = filteredFriend.relationship.code())
                        )
                    }
                }

                // Add logged in account if we don't exist yet.
                val selfId = getUserSteamId()!!.convertToUInt64()
                val self = db.steamFriendDao().findFriend(selfId).first()
                if (self == null) {
                    db.steamFriendDao().insert(SteamFriend(id = selfId))
                }
            }

            // NOTE: Our UI could load too quickly on fresh database, our icon will be "?"
            //  unless relaunched or we nav to a new screen.
            refreshPersonaStates()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun onPersonaStateReceived(callback: PersonaStatesCallback) {
        // Ignore accounts that arent individuals
        if (callback.friendID.isIndividualAccount.not()) {
            return
        }

        // Ignore states where the name is blank.
        if (callback.name.isEmpty()) {
            return
        }

        Log.d("SteamService", "Persona state received: ${callback.name}")

        dbScope.launch {
            db.withTransaction {
                val id = callback.friendID.convertToUInt64()
                val friend = db.steamFriendDao().findFriend(id).first()

                if (friend == null) {
                    Log.w("SteamService", "onPersonaStateReceived: " +
                            "failed to find friend to update: $id")
                    return@withTransaction
                }

                db.steamFriendDao().update(
                    friend.copy(
                        statusFlags = EClientPersonaStateFlag.code(callback.statusFlags),
                        state = callback.state.code(),
                        stateFlags = EPersonaStateFlag.code(callback.stateFlags),
                        gameAppID = callback.gameAppID,
                        gameID = callback.gameID.convertToUInt64(),
                        gameName = callback.gameName,
                        gameServerIP = NetHelpers.getIPAddress(callback.gameServerIP),
                        gameServerPort = callback.gameServerPort,
                        queryPort = callback.queryPort,
                        sourceSteamID = callback.sourceSteamID.convertToUInt64(),
                        gameDataBlob = callback.gameDataBlob.decodeToString(),
                        name = callback.name,
                        avatarHash = callback.avatarHash.toHexString(),
                        lastLogOff = callback.lastLogOff.time,
                        lastLogOn = callback.lastLogOn.time,
                        clanRank = callback.clanRank,
                        clanTag = callback.clanTag,
                        onlineSessionInstances = callback.onlineSessionInstances,
                    )
                )
            }
        }

        // Send off a status if we change states.
        if (callback.friendID == getUserSteamId()) {
            Log.d("SteamService", "Emitting PersonaStateReceived")
            dbScope.launch {
                val id = callback.friendID.convertToUInt64()
                val friend = db.steamFriendDao().findFriend(id).first()
                PluviaApp.events.emit(SteamEvent.PersonaStateReceived(friend))
            }
        }
    }

    private fun onLicenseList(callback: LicenseListCallback) {
        Log.d("SteamService", "Received License List ${callback.result}")
        if (callback.result == EResult.OK) {
            for (i in callback.licenseList.indices) {
                val license = callback.licenseList[i]
                packageInfo[license.packageID] = PackageInfo(
                    packageId = license.packageID,
                    receiveIndex = i,
                    ownerAccountId = license.ownerAccountID,
                    lastChangeNumber = license.lastChangeNumber,
                    accessToken = license.accessToken,
                    territoryCode = license.territoryCode,
                    licenseFlags = license.licenseFlags,
                    licenseType = license.licenseType,
                    paymentMethod = license.paymentMethod,
                    purchaseCountryCode = license.purchaseCode,
                    appIds = IntArray(0),
                    depotIds = IntArray(0),
                )
            }

            isRequestingPkgInfo = true
            _steamApps!!.picsGetProductInfo(
                apps = emptyList(),
                packages = callback.licenseList.map { PICSRequest(it.packageID, it.accessToken) }
            )
        }

        isReceivingLicenseList = false
    }

    private fun onPICSProductInfo(callback: PICSProductInfoCallback) {
        // Log.d("SteamService", "Received PICSProductInfo")
        if (callback.packages.isNotEmpty()) {
            for (pkg in callback.packages.values) {
                // Log.d("SteamService", "Received pkg ${pkg.id}")
                packageInfo[pkg.id]?.let { pi ->
                    pi.appIds =
                        pkg.keyValues["appids"].children.map { it.asInteger() }.toIntArray()
                    pi.depotIds =
                        pkg.keyValues["depotids"].children.map { it.asInteger() }.toIntArray()
                }
            }

            isRequestingPkgInfo = false
            isRequestingAppInfo = true
            _steamApps?.picsGetProductInfo(
                apps = packageInfo.values
                    .flatMap { it.appIds.asIterable() }
                    .map { PICSRequest(it) },
                packages = emptyList()
            )
        }

        if (callback.apps.isNotEmpty()) {
            val apps = callback.apps.values.toTypedArray()
            for (i in apps.indices) {
                val app = apps[i]
                // Log.d("SteamService", "Received app ${app.id}")
                val pkg: PackageInfo
                val appDepots = mutableMapOf<Int, DepotInfo>()
                if (packageInfo.values.any { it.appIds.contains(app.id) }) {
                    pkg = packageInfo.values.first { it.appIds.contains(app.id) }
                    for (depotId in pkg.depotIds) {
                        val generateManifest: (List<KeyValue>) -> Map<String, ManifestInfo> = {
                            val output = mutableMapOf<String, ManifestInfo>()
                            for (manifest in it) {
                                output[manifest.name] = ManifestInfo(
                                    name = manifest.name,
                                    gid = manifest["gid"].asLong(),
                                    size = manifest["size"].asLong(),
                                    download = manifest["download"].asLong(),
                                )
                            }
                            output
                        }
                        val currentDepot = app.keyValues["depots"]["$depotId"]
                        val manifests = generateManifest(currentDepot["manifests"].children)
                        val encryptedManifests = generateManifest(
                            currentDepot["encryptedManifests"].children
                        )

                        appDepots[depotId] = DepotInfo(
                            depotId = depotId,
                            depotFromApp = currentDepot["depotfromapp"].asInteger(INVALID_APP_ID),
                            sharedInstall = currentDepot["sharedinstall"].asBoolean(),
                            osList = OS.from(currentDepot["config"]["oslist"].value),
                            manifests = manifests,
                            encryptedManifests = encryptedManifests,
                        )
                    }
                } else {
                    Log.e("SteamService", "App(${app.id}) did not belong to any package")
                    continue
                }

                val toLangImgMap: (List<KeyValue>) -> Map<Language, String> = { keyValues ->
                    keyValues.map {
                        val language: Language = try {
                            Language.valueOf(it.name)
                        } catch (_: Exception) {
                            Log.d("SteamService", "Language ${it.name} does not exist in enum")
                            Language.unknown
                        }
                        Pair(language, it.value)
                    }.filter { it.first != Language.unknown }.toMap()
                }
                val launchConfigs = app.keyValues["config"]["launch"].children
                appInfo[app.id] = AppInfo(
                    appId = app.id,
                    receiveIndex = packageInfo.values
                        .filter { it.receiveIndex < pkg.receiveIndex }
                        .fold(initial = 0) { accum, pkgInfo -> accum + pkgInfo.appIds.size } + i,
                    packageId = pkg.packageId,
                    depots = appDepots,
                    name = app.keyValues["common"]["name"].value ?: "",
                    type = AppType.valueOf(
                        app.keyValues["common"]["type"].value?.lowercase() ?: "invalid"
                    ),
                    osList = OS.from(app.keyValues["common"]["oslist"].value),
                    releaseState = ReleaseState.valueOf(
                        app.keyValues["common"]["releasestate"].value ?: "released"
                    ),
                    metacriticScore = app.keyValues["common"]["metacritic_score"].asByte(),
                    metacriticFullUrl = app.keyValues["common"]["metacritic_fullurl"].value ?: "",
                    logoHash = app.keyValues["common"]["logo"].value ?: "",
                    logoSmallHash = app.keyValues["common"]["logo_small"].value ?: "",
                    iconHash = app.keyValues["common"]["icon"].value ?: "",
                    clientIconHash = app.keyValues["common"]["clienticon"].value ?: "",
                    clientTgaHash = app.keyValues["common"]["clienttga"].value ?: "",
                    smallCapsule = toLangImgMap(app.keyValues["common"]["small_capsule"].children),
                    headerImage = toLangImgMap(app.keyValues["common"]["header_image"].children),
                    libraryAssets = LibraryAssetsInfo(
                        libraryCapsule = LibraryCapsuleInfo(
                            image = toLangImgMap(app.keyValues["common"]["library_assets_full"]["library_capsule"]["image"].children),
                            image2x = toLangImgMap(app.keyValues["common"]["library_assets_full"]["library_capsule"]["image2x"].children)
                        ),
                        libraryHero = LibraryHeroInfo(
                            image = toLangImgMap(app.keyValues["common"]["library_assets_full"]["library_hero"]["image"].children),
                            image2x = toLangImgMap(app.keyValues["common"]["library_assets_full"]["library_hero"]["image2x"].children)
                        ),
                        libraryLogo = LibraryLogoInfo(
                            image = toLangImgMap(app.keyValues["common"]["library_assets_full"]["library_logo"]["image"].children),
                            image2x = toLangImgMap(app.keyValues["common"]["library_assets_full"]["library_logo"]["image2x"].children)
                        )
                    ),
                    primaryGenre = app.keyValues["common"]["primary_genre"].asBoolean(),
                    reviewScore = app.keyValues["common"]["review_score"].asByte(),
                    reviewPercentage = app.keyValues["common"]["review_percentage"].asByte(),
                    controllerSupport = ControllerSupport.valueOf(
                        app.keyValues["common"]["controller_support"].value ?: "none"
                    ),
                    demoOfAppId = app.keyValues["common"]["extended"]["demoofappid"].asInteger(),
                    developer = app.keyValues["common"]["extended"]["developer"].value ?: "",
                    publisher = app.keyValues["common"]["extended"]["publisher"].value ?: "",
                    homepageUrl = app.keyValues["common"]["extended"]["homepage"].value ?: "",
                    gameManualUrl = app.keyValues["common"]["extended"]["gamemanualurl"].value ?: "",
                    loadAllBeforeLaunch = app.keyValues["common"]["extended"]["loadallbeforelaunch"].asBoolean(),
                    // dlcAppIds = (app.keyValues["common"]["extended"]["listofdlc"].value).Split(",").Select(uint.Parse).ToArray(),
                    dlcAppIds = IntArray(0),
                    isFreeApp = app.keyValues["common"]["extended"]["isfreeapp"].asBoolean(),
                    dlcForAppId = app.keyValues["common"]["extended"]["dlcforappid"].asInteger(),
                    mustOwnAppToPurchase = app.keyValues["common"]["extended"]["mustownapptopurchase"].asInteger(),
                    dlcAvailableOnStore = app.keyValues["common"]["extended"]["dlcavailableonstore"].asBoolean(),
                    optionalDlc = app.keyValues["common"]["extended"]["optionaldlc"].asBoolean(),
                    gameDir = app.keyValues["common"]["extended"]["gamedir"].value ?: "",
                    installScript = app.keyValues["common"]["extended"]["installscript"].value ?: "",
                    noServers = app.keyValues["common"]["extended"]["noservers"].asBoolean(),
                    order = app.keyValues["common"]["extended"]["order"].asBoolean(),
                    primaryCache = app.keyValues["common"]["extended"]["primarycache"].asInteger(),
                    // validOSList = app.keyValues["common"]["extended"]["validoslist"].value!.Split(",").Select(Enum.Parse<OS>).Aggregate((os1, os2) => os1 | os2),
                    validOSList = EnumSet.of(OS.none),
                    thirdPartyCdKey = app.keyValues["common"]["extended"]["thirdpartycdkey"].asBoolean(),
                    visibleOnlyWhenInstalled = app.keyValues["common"]["extended"]["visibleonlywheninstalled"].asBoolean(),
                    visibleOnlyWhenSubscribed = app.keyValues["common"]["extended"]["visibleonlywhensubscribed"].asBoolean(),
                    launchEulaUrl = app.keyValues["common"]["extended"]["launcheula"].value ?: "",
                    requireDefaultInstallFolder = app.keyValues["common"]["config"]["requiredefaultinstallfolder"].asBoolean(),
                    contentType = app.keyValues["common"]["config"]["contentType"].asInteger(),
                    installDir = app.keyValues["common"]["config"]["installdir"].value ?: "",
                    useLaunchCmdLine = app.keyValues["common"]["config"]["uselaunchcommandline"].asBoolean(),
                    launchWithoutWorkshopUpdates = app.keyValues["common"]["config"]["launchwithoutworkshopupdates"].asBoolean(),
                    useMms = app.keyValues["common"]["config"]["usemms"].asBoolean(),
                    installScriptSignature = app.keyValues["common"]["config"]["installscriptsignature"].value ?: "",
                    installScriptOverride = app.keyValues["common"]["config"]["installscriptoverride"].asBoolean(),
                    config = ConfigInfo(
                        installDir = app.keyValues["config"]["installdir"].value ?: "",
                        launch = launchConfigs.map {
                            LaunchInfo(
                                executable = it["executable"].value ?: "",
                                workingDir = it["workingdir"].value ?: "",
                                description = it["description"].value ?: "",
                                type = it["type"].value ?: "",
                                configOS = OS.from(it["config"]["oslist"].value),
                                configArch = OSArch.from(it["config"]["osarch"].value)
                            )
                        }.toTypedArray(),
                        steamControllerTemplateIndex = app.keyValues["config"]["steamcontrollertemplateindex"].asInteger(),
                        steamControllerTouchTemplateIndex = app.keyValues["config"]["steamcontrollertouchtemplateindex"].asInteger(),
                    ),
                    ufs = UFS(
                        quota = app.keyValues["ufs"]["quota"].asInteger(),
                        maxNumFiles = app.keyValues["ufs"]["maxnumfiles"].asInteger(),
                        saveFiles = app.keyValues["ufs"]["savefiles"].children.map {
                            SaveFile(
                                root = PathType.from(it["root"].value),
                                path = it["path"].value ?: "",
                                pattern = it["pattern"].value ?: ""
                            )
                        }.toTypedArray()
                    )
                )
            }

            isRequestingAppInfo = false
            PluviaApp.events.emit(SteamEvent.AppInfoReceived)
        }
    }
}