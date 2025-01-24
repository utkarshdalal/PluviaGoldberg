package com.OxGames.Pluvia.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.room.withTransaction
import com.OxGames.Pluvia.BuildConfig
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.PrefManager
import com.OxGames.Pluvia.data.AppInfo
import com.OxGames.Pluvia.data.BranchInfo
import com.OxGames.Pluvia.data.ConfigInfo
import com.OxGames.Pluvia.data.DepotInfo
import com.OxGames.Pluvia.data.DownloadInfo
import com.OxGames.Pluvia.data.GameProcessInfo
import com.OxGames.Pluvia.data.LaunchInfo
import com.OxGames.Pluvia.data.LibraryAssetsInfo
import com.OxGames.Pluvia.data.LibraryCapsuleInfo
import com.OxGames.Pluvia.data.LibraryHeroInfo
import com.OxGames.Pluvia.data.LibraryLogoInfo
import com.OxGames.Pluvia.data.PackageInfo
import com.OxGames.Pluvia.data.PostSyncInfo
import com.OxGames.Pluvia.data.SaveFilePattern
import com.OxGames.Pluvia.data.SteamFriend
import com.OxGames.Pluvia.data.UFS
import com.OxGames.Pluvia.data.UserFileInfo
import com.OxGames.Pluvia.db.PluviaDatabase
import com.OxGames.Pluvia.db.dao.ChangeNumbersDao
import com.OxGames.Pluvia.db.dao.FileChangeListsDao
import com.OxGames.Pluvia.db.dao.SteamFriendDao
import com.OxGames.Pluvia.enums.AppType
import com.OxGames.Pluvia.enums.ControllerSupport
import com.OxGames.Pluvia.enums.LoginResult
import com.OxGames.Pluvia.enums.OS
import com.OxGames.Pluvia.enums.OSArch
import com.OxGames.Pluvia.enums.PathType
import com.OxGames.Pluvia.enums.ReleaseState
import com.OxGames.Pluvia.enums.SaveLocation
import com.OxGames.Pluvia.enums.SyncResult
import com.OxGames.Pluvia.events.AndroidEvent
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.utils.FileUtils
import com.OxGames.Pluvia.utils.SteamUtils
import com.OxGames.Pluvia.utils.generateManifest
import com.OxGames.Pluvia.utils.toLangImgMap
import com.google.android.play.core.ktx.bytesDownloaded
import com.google.android.play.core.ktx.requestCancelInstall
import com.google.android.play.core.ktx.requestInstall
import com.google.android.play.core.ktx.requestSessionState
import com.google.android.play.core.ktx.status
import com.google.android.play.core.ktx.totalBytesToDownload
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.winlator.xenvironment.ImageFs
import dagger.hilt.android.AndroidEntryPoint
import `in`.dragonbra.javasteam.enums.ELicenseType
import `in`.dragonbra.javasteam.enums.EOSType
import `in`.dragonbra.javasteam.enums.EPaymentMethod
import `in`.dragonbra.javasteam.enums.EPersonaState
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.networking.steam3.ProtocolTypes
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.CChat_RequestFriendPersonaStates_Request
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientObjects.ECloudPendingRemoteOperation
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
import `in`.dragonbra.javasteam.steam.handlers.steamapps.GamePlayedInfo
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
import `in`.dragonbra.javasteam.types.SteamID
import `in`.dragonbra.javasteam.util.NetHelpers
import `in`.dragonbra.javasteam.util.log.LogListener
import `in`.dragonbra.javasteam.util.log.LogManager
import java.io.BufferedReader
import java.io.Closeable
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Date
import java.util.EnumSet
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.io.path.pathString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.future.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@AndroidEntryPoint
class SteamService : Service(), IChallengeUrlChanged {

    @Inject
    lateinit var db: PluviaDatabase

    @Inject
    lateinit var friendDao: SteamFriendDao

    @Inject
    lateinit var changeNumbersDao: ChangeNumbersDao

    @Inject
    lateinit var fileChangeListsDao: FileChangeListsDao

    // @Inject
    // lateinit var paths: PathsModule

    private lateinit var notificationHelper: NotificationHelper

    private var _callbackManager: CallbackManager? = null

    internal var steamClient: SteamClient? = null

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
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val MAX_RETRY_ATTEMPTS = 20
        const val MAX_USER_FILE_RETRIES = 3

        const val INVALID_APP_ID: Int = Int.MAX_VALUE
        const val INVALID_PKG_ID: Int = Int.MAX_VALUE
        const val INVALID_DEPOT_ID: Int = Int.MAX_VALUE
        const val INVALID_MANIFEST_ID: Long = Long.MAX_VALUE

        /**
         * Default timeout to use when making requests
         */
        var requestTimeout = 10000L

        /**
         * Default timeout to use when reading the response body
         */

        private val PROTOCOL_TYPES = EnumSet.of(ProtocolTypes.TCP, ProtocolTypes.UDP)

        private var instance: SteamService? = null

        private val downloadJobs = ConcurrentHashMap<Int, DownloadInfo>()

        var responseBodyTimeout = 60000L

        private var syncInProgress: Boolean = false

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
        var isLoggingOut: Boolean = false
            private set
        val isLoggedIn: Boolean
            get() = instance?.steamClient?.steamID?.isValid == true
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

        val defaultAppInstallPath: String
            get() = Paths.get(instance!!.dataDir.path, "Steam", "steamapps", "common").pathString

        val defaultAppStagingPath: String
            get() = Paths.get(instance!!.dataDir.path, "Steam", "steamapps", "staging").pathString

        val userSteamId: SteamID?
            get() = instance?.steamClient?.steamID

        suspend fun setPersonaState(state: EPersonaState) = withContext(Dispatchers.IO) {
            PrefManager.personaState = state
            instance?._steamFriends?.setPersonaState(state)
        }

        suspend fun requestUserPersona() = withContext(Dispatchers.IO) {
            // in order to get user avatar url and other info
            userSteamId?.let { instance?._steamFriends?.requestFriendInfo(it) }
        }

        suspend fun getPersonaStateOf(steamId: SteamID): SteamFriend? = withContext(Dispatchers.IO) {
            instance!!.db.steamFriendDao().findFriend(steamId.convertToUInt64()).first()
        }

        fun getAppList(filter: EnumSet<AppType>): List<AppInfo> {
            return instance?.appInfo?.values?.filter { filter.contains(it.type) } ?: emptyList()
        }

        fun getPkgInfoOf(appId: Int): PackageInfo? {
            return instance?.packageInfo?.values?.firstOrNull {
                // logD("Pkg (${it.packageId}) apps: ${it.appIds.joinToString(",")}")
                it.appIds.contains(appId)
            }
        }

        fun getAppInfoOf(appId: Int): AppInfo? {
            return instance?.appInfo?.values?.firstOrNull {
                it.appId == appId
            }
        }

        fun getAppDownloadInfo(appId: Int): DownloadInfo? {
            return downloadJobs[appId]
        }

        fun isAppInstalled(appId: Int): Boolean {
            val appDownloadInfo = getAppDownloadInfo(appId)
            val isNotDownloading = appDownloadInfo == null || appDownloadInfo.getProgress() >= 1f
            val appDirPath = Paths.get(getAppDirPath(appId))
            val pathExists = Files.exists(appDirPath)

            // logD("isDownloading: $isNotDownloading && pathExists: $pathExists && appDirPath: $appDirPath")

            return isNotDownloading && pathExists
        }

        fun getAppDlc(appId: Int): Map<Int, DepotInfo> {
            return getAppInfoOf(appId)?.let {
                it.depots.filter { it.value.dlcAppId != INVALID_APP_ID }
            } ?: emptyMap()
        }

        fun getOwnedAppDlc(appId: Int): Map<Int, DepotInfo> = getAppDlc(appId).filter {
            getPkgInfoOf(it.value.dlcAppId)?.let { pkg ->
                instance?.steamClient?.let { steamClient ->
                    pkg.ownerAccountId == steamClient.steamID.accountID.toInt()
                }
            } == true
        }

        fun getDownloadableDepots(appId: Int): Map<Int, DepotInfo> = getAppInfoOf(appId)?.depots?.filter { depotEntry ->
            val depot = depotEntry.value

            (depot.manifests.isNotEmpty() || depot.sharedInstall) &&
                (depot.osList.contains(OS.windows) || (!depot.osList.contains(OS.linux) && !depot.osList.contains(OS.macos))) &&
                (depot.osArch == OSArch.Arch64 || depot.osArch == OSArch.Unknown) &&
                (depot.dlcAppId == INVALID_APP_ID || getOwnedAppDlc(appId).containsKey(depot.depotId))
        } ?: emptyMap()

        fun getAppDirPath(appId: Int): String {
            var appName = getAppInfoOf(appId)?.config?.installDir.orEmpty()

            if (appName.isEmpty()) {
                appName = getAppInfoOf(appId)?.name.orEmpty()
            }

            return Paths.get(PrefManager.appInstallPath, appName).pathString
        }

        fun deleteApp(appId: Int): Boolean {
            with(instance!!) {
                dbScope.launch {
                    db.withTransaction {
                        changeNumbersDao.deleteByAppId(appId)
                        fileChangeListsDao.deleteByAppId(appId)
                    }
                }
            }

            val appDirPath = getAppDirPath(appId)

            return File(appDirPath).deleteRecursively()
        }

        fun downloadApp(appId: Int): DownloadInfo? {
            return getAppInfoOf(appId)?.let { appInfo ->
                Timber.i("App contains ${appInfo.depots.size} depot(s): ${appInfo.depots.keys}")
                downloadApp(appId, getDownloadableDepots(appId).keys.toList(), "public")
            }
        }

        fun isImageFsInstalled(context: Context): Boolean {
            return ImageFs.find(context).rootDir.exists()
        }

        fun isImageFsInstallable(context: Context): Boolean {
            val splitManager = SplitInstallManagerFactory.create(context)
            return splitManager.installedModules.contains("ubuntufs") // || FileUtils.assetExists(context.assets, "imagefs.txz")
        }

        fun downloadImageFs(
            onDownloadProgress: (Float) -> Unit,
            parentScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
        ) = parentScope.async {
            if (!isImageFsInstalled(instance!!) && !isImageFsInstallable(instance!!)) {
                Timber.i("imagefs.txz will be downloaded")
                val splitManager = SplitInstallManagerFactory.create(instance!!)
                // if (!splitManager.installedModules.contains("ubuntufs")) {
                val moduleInstallSessionId = splitManager.requestInstall(listOf("ubuntufs"))
                var isInstalling = true
                // try {
                do {
                    val sessionState = splitManager.requestSessionState(moduleInstallSessionId)
                    // logD("imagefs.txz session state status: ${sessionState.status}")
                    when (sessionState.status) {
                        SplitInstallSessionStatus.INSTALLED -> isInstalling = false
                        SplitInstallSessionStatus.PENDING,
                        SplitInstallSessionStatus.INSTALLING,
                        SplitInstallSessionStatus.DOWNLOADED,
                        SplitInstallSessionStatus.DOWNLOADING,
                        -> {
                            if (!isActive) {
                                Timber.i("ubuntufs module download cancelling due to scope becoming inactive")
                                splitManager.requestCancelInstall(moduleInstallSessionId)
                                break
                            }
                            val downloadPercent =
                                sessionState.bytesDownloaded.toFloat() / sessionState.totalBytesToDownload
                            // logD("imagefs.txz download percent: $downloadPercent")
                            // downloadInfo.setProgress(downloadPercent, 0)
                            onDownloadProgress(downloadPercent)
                            delay(100)
                        }

                        else -> {
                            cancel("Failed to install ubuntufs module: ${sessionState.status}")
                        }
                    }
                } while (isInstalling)
                // } catch (e: Exception) {
                //     if (moduleInstallSessionId != -1) {
                //         val splitManager = SplitInstallManagerFactory.create(instance!!)
                //         val sessionState = splitManager.requestSessionState(moduleInstallSessionId)
                //         if (sessionState.status == SplitInstallSessionStatus.DOWNLOADING ||
                //             sessionState.status == SplitInstallSessionStatus.DOWNLOADED ||
                //             sessionState.status == SplitInstallSessionStatus.INSTALLING
                //         ) {
                //             splitManager.requestCancelInstall(moduleInstallSessionId)
                //         }
                //     }
                // }
                val installedProperly = splitManager.installedModules.contains("ubuntufs")
                Timber.i("imagefs.txz module installed properly: $installedProperly")
                // }
            } else {
                Timber.i("ubuntufs module already installed, skipping download")
            }
        }

        fun downloadApp(appId: Int, depotIds: List<Int>, branch: String): DownloadInfo? {
            if (downloadJobs.contains(appId)) {
                Timber.w("Could not start new download job for $appId since one already exists")
                return getAppDownloadInfo(appId)
            }

            if (depotIds.isEmpty()) {
                Timber.w("No depots to download for $appId")
                return null
            }

            Timber.i("Found ${depotIds.size} depot(s) to download: $depotIds")

            val needsImageFsDownload = !ImageFs.find(instance!!).rootDir.exists() &&
                !FileUtils.assetExists(instance!!.assets, "imagefs.txz")
            val indexOffset = if (needsImageFsDownload) 1 else 0

            val downloadInfo = DownloadInfo(depotIds.size + indexOffset).also { downloadInfo ->
                downloadInfo.setDownloadJob(
                    CoroutineScope(Dispatchers.IO).launch {
                        // TODO: change downloads to be one item/depot per job and connect them to the game requesting to download them
                        try {
                            downloadImageFs(
                                onDownloadProgress = {
                                    downloadInfo.setProgress(it, 0)
                                },
                                this,
                            ).await()
                            depotIds.forEachIndexed { jobIndex, depotId ->
                                // TODO: download shared install depots to a common location
                                ContentDownloader(instance!!.steamClient!!).downloadApp(
                                    appId = appId,
                                    depotId = depotId,
                                    installPath = PrefManager.appInstallPath,
                                    stagingPath = PrefManager.appStagingPath,
                                    branch = branch,
                                    // maxDownloads = 1,
                                    onDownloadProgress = { downloadInfo.setProgress(it, jobIndex + indexOffset) },
                                    parentScope = coroutineContext.job as CoroutineScope,
                                ).await()
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Download failed")
                        }

                        downloadJobs.remove(appId)
                    },
                )
            }

            downloadJobs[appId] = downloadInfo

            return downloadInfo
        }

        fun getWindowsLaunchInfos(appId: Int): List<LaunchInfo> {
            return getAppInfoOf(appId)?.let { appInfo ->
                appInfo.config.launch.filter { launchInfo ->
                    // since configOS was unreliable and configArch was even more unreliable
                    launchInfo.executable.endsWith(".exe")
                }
            } ?: emptyList()
        }

        suspend fun notifyRunningProcesses(vararg gameProcesses: GameProcessInfo) = withContext(Dispatchers.IO) {
            instance?.let { steamInstance ->
                val gamesPlayed = gameProcesses.mapNotNull { gameProcess ->
                    getAppInfoOf(gameProcess.appId)?.let { appInfo ->
                        getPkgInfoOf(gameProcess.appId)?.let { pkgInfo ->
                            appInfo.branches[gameProcess.branch]?.let { branch ->
                                val processId = gameProcess.processes
                                    .firstOrNull { it.parentIsSteam }
                                    ?.processId
                                    ?: gameProcess.processes.firstOrNull()?.processId
                                    ?: 0

                                GamePlayedInfo(
                                    gameId = gameProcess.appId.toLong(),
                                    processId = processId,
                                    ownerId = pkgInfo.ownerAccountId,
                                    // TODO: figure out what this is and un-hardcode
                                    launchSource = 100,
                                    gameBuildId = branch.buildId.toInt(),
                                    processIdList = gameProcess.processes,
                                )
                            }
                        }
                    }
                }

                Timber.i(
                    "GameProcessInfo:" +
                        gamesPlayed.joinToString("\n") {
                            "\n\tprocessId: ${it.processId}" +
                                "\n\tgameId: ${it.gameId}" +
                                "\n\tprocesses: ${
                                    it.processIdList.joinToString("\n") {
                                        "\n\t\tprocessId: ${it.processId}" +
                                            "\n\t\tprocessIdParent: ${it.processIdParent}" +
                                            "\n\t\tparentIsSteam: ${it.parentIsSteam}"
                                    }
                                }"
                        },
                )

                steamInstance._steamApps?.notifyGamesPlayed(
                    gamesPlayed = gamesPlayed,
                    clientOsType = EOSType.AndroidUnknown,
                )
            }
        }

        fun beginLaunchApp(
            appId: Int,
            parentScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
            ignorePendingOperations: Boolean = false,
            preferredSave: SaveLocation = SaveLocation.None,
            prefixToPath: (String) -> String,
        ): Deferred<PostSyncInfo> = parentScope.async {
            if (syncInProgress) {
                Timber.w("Cannot launch app when sync already in progress")
                return@async PostSyncInfo(SyncResult.InProgress)
            }

            syncInProgress = true

            var syncResult = PostSyncInfo(SyncResult.UnknownFail)

            PrefManager.clientId?.let { clientId ->
                instance?.let { steamInstance ->
                    getAppInfoOf(appId)?.let { appInfo ->
                        steamInstance._steamCloud?.let { steamCloud ->
                            val postSyncInfo = SteamAutoCloud.syncUserFiles(
                                appInfo = appInfo,
                                clientId = clientId,
                                steamInstance = steamInstance,
                                steamCloud = steamCloud,
                                preferredSave = preferredSave,
                                parentScope = parentScope,
                                prefixToPath = prefixToPath,
                            ).await()

                            // steamCloud.appCloudSyncStats(
                            //     appId = appId,
                            //     platformType = EPlatformType.Win32,
                            //     preload = false,
                            //     blockingAppLaunch = true,
                            //     filesUploaded = postSyncInfo?.filesUploaded ?: 0,
                            //     filesDownloaded = postSyncInfo?.filesDownloaded ?: 0,
                            //     filesDeleted = postSyncInfo?.filesDeleted ?: 0,
                            //     filesManaged = postSyncInfo?.filesManaged ?: 0,
                            //     bytesUploaded = postSyncInfo?.bytesUploaded ?: 0,
                            //     bytesDownloaded = postSyncInfo?.bytesDownloaded ?: 0,
                            //     microsecTotal = postSyncInfo?.microsecTotal ?: 0,
                            //     microsecInitCaches = postSyncInfo?.microsecInitCaches ?: 0,
                            //     microsecValidateState = postSyncInfo?.microsecValidateState ?: 0,
                            //     microsecAcLaunch = postSyncInfo?.microsecAcLaunch ?: 0,
                            //     microsecAcPrepUserFiles = postSyncInfo?.microsecAcPrepUserFiles ?: 0,
                            //     microsecAcExit = postSyncInfo?.microsecAcExit ?: 0,
                            //     microsecBuildSyncList = postSyncInfo?.microsecBuildSyncList ?: 0,
                            //     microsecDeleteFiles = postSyncInfo?.microsecDeleteFiles ?: 0,
                            //     microsecDownloadFiles = postSyncInfo?.microsecDownloadFiles ?: 0,
                            //     microsecUploadFiles = postSyncInfo?.microsecUploadFiles ?: 0,
                            // )

                            postSyncInfo?.let {
                                syncResult = it

                                if (it.syncResult == SyncResult.Success || it.syncResult == SyncResult.UpToDate) {
                                    Timber.i(
                                        "Signaling app launch:\n\tappId: %d\n\tclientId: %s\n\tosType: %s",
                                        appId,
                                        PrefManager.clientId,
                                        EOSType.AndroidUnknown,
                                    )

                                    val pendingRemoteOperations = steamCloud.signalAppLaunchIntent(
                                        appId = appId,
                                        clientId = clientId,
                                        machineName = SteamUtils.getMachineName(steamInstance),
                                        ignorePendingOperations = ignorePendingOperations,
                                        osType = EOSType.AndroidUnknown,
                                    ).await()

                                    if (pendingRemoteOperations.isNotEmpty() && !ignorePendingOperations) {
                                        syncResult = PostSyncInfo(
                                            SyncResult.PendingOperations,
                                            pendingRemoteOperations = pendingRemoteOperations,
                                        )
                                    } else if (ignorePendingOperations &&
                                        pendingRemoteOperations.any {
                                            it.operation == ECloudPendingRemoteOperation.k_ECloudPendingRemoteOperationAppSessionActive
                                        }
                                    ) {
                                        steamInstance._steamUser!!.kickPlayingSession()
                                    }
                                    // else {
                                    //     val gameId = GameID()
                                    //     gameId.appID = appId
                                    //     // TODO: un-hardcode
                                    //     gameId.appType = GameID.GameType.APP
                                    //     // TODO: un-hardcode
                                    //     gameId.modID = 0
                                    //     steamInstance._steamCloud?.sendClientAppUsageEvent(
                                    //         gameId = gameId,
                                    //         // TODO: un-hardcode
                                    //         usageEvent = EAppUsageEvent.GameLaunch,
                                    //         // TODO: un-hardcode
                                    //         offline = 0,
                                    //     )
                                    // }
                                }
                            }
                        }
                    }
                }
            }

            syncInProgress = false

            return@async syncResult
        }

        suspend fun closeApp(appId: Int, prefixToPath: (String) -> String) = withContext(Dispatchers.IO) {
            async {
                if (syncInProgress) {
                    Timber.w("Cannot close app when sync already in progress")
                    return@async
                }

                syncInProgress = true

                PrefManager.clientId?.let { clientId ->
                    instance?.let { steamInstance ->
                        getAppInfoOf(appId)?.let { appInfo ->
                            steamInstance._steamCloud?.let { steamCloud ->
                                val postSyncInfo = SteamAutoCloud.syncUserFiles(
                                    appInfo = appInfo,
                                    clientId = clientId,
                                    steamInstance = steamInstance,
                                    steamCloud = steamCloud,
                                    parentScope = this,
                                    prefixToPath = prefixToPath,
                                ).await()
                                // steamCloud.appCloudSyncStats(
                                //     appId = appId,
                                //     platformType = EPlatformType.Win32,
                                //     preload = false,
                                //     blockingAppLaunch = false,
                                //     filesUploaded = postSyncInfo?.filesUploaded ?: 0,
                                //     filesDownloaded = postSyncInfo?.filesDownloaded ?: 0,
                                //     filesDeleted = postSyncInfo?.filesDeleted ?: 0,
                                //     filesManaged = postSyncInfo?.filesManaged ?: 0,
                                //     bytesUploaded = postSyncInfo?.bytesUploaded ?: 0,
                                //     bytesDownloaded = postSyncInfo?.bytesDownloaded ?: 0,
                                //     microsecTotal = postSyncInfo?.microsecTotal ?: 0,
                                //     microsecInitCaches = postSyncInfo?.microsecInitCaches ?: 0,
                                //     microsecValidateState = postSyncInfo?.microsecValidateState ?: 0,
                                //     microsecAcLaunch = postSyncInfo?.microsecAcLaunch ?: 0,
                                //     microsecAcPrepUserFiles = postSyncInfo?.microsecAcPrepUserFiles ?: 0,
                                //     microsecAcExit = postSyncInfo?.microsecAcExit ?: 0,
                                //     microsecBuildSyncList = postSyncInfo?.microsecBuildSyncList ?: 0,
                                //     microsecDeleteFiles = postSyncInfo?.microsecDeleteFiles ?: 0,
                                //     microsecDownloadFiles = postSyncInfo?.microsecDownloadFiles ?: 0,
                                //     microsecUploadFiles = postSyncInfo?.microsecUploadFiles ?: 0,
                                // )
                                steamCloud.signalAppExitSyncDone(
                                    appId = appId,
                                    clientId = clientId,
                                    uploadsCompleted = postSyncInfo?.uploadsCompleted == true,
                                    uploadsRequired = postSyncInfo?.uploadsRequired == false,
                                )
                            }
                        }
                    }
                }

                syncInProgress = false
            }
        }

        fun getProotTime(context: Context): Long {
            val imageFs = ImageFs.find(context)

            if (!imageFs.rootDir.exists()) {
                return 0
            }

            val nativeLibraryDir = context.applicationInfo.nativeLibraryDir

            val command = arrayOf(
                "$nativeLibraryDir/libproot.so",
                "--kill-on-exit",
                "--rootfs=${imageFs.rootDir}",
                "--cwd=${ImageFs.USER}",
                "--bind=/dev",
                "--bind=${imageFs.rootDir}/tmp/shm:/dev/shm",
                "--bind=/proc",
                "--bind=/sys",
                "/usr/bin/env",
                "HOME=/home/${ImageFs.USER}",
                "USER=${ImageFs.USER}",
                "TMPDIR=/tmp",
                "LC_ALL=en_US.utf8",
                // Set PATH environment variable
                "PATH=${imageFs.winePath}/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
                "LD_LIBRARY_PATH=/usr/lib/aarch64-linux-gnu:/usr/lib/arm-linux-gnueabihf",
                "date",
                "+%s%3N",
            )

            val envVars = arrayOf(
                "PROOT_TMP_DIR=${Paths.get(context.filesDir.absolutePath, "tmp")}",
                "PROOT_LOADER=$nativeLibraryDir/libproot-loader.so",
            )

            val process = Runtime.getRuntime().exec(command, envVars, imageFs.rootDir)

            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val error = errorReader.readLine()

            if (error != null) {
                Timber.e("ProotTime: Error: $error")
            }

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readLine().orEmpty()
            process.waitFor()

            Timber.i("ProotTime: Output: $output")

            return output.toLongOrNull() ?: 0
        }

        data class FileChanges(
            val filesDeleted: List<UserFileInfo>,
            val filesModified: List<UserFileInfo>,
            val filesCreated: List<UserFileInfo>,
        )

        private fun login(
            username: String,
            accessToken: String? = null,
            refreshToken: String? = null,
            password: String? = null,
            shouldRememberPassword: Boolean = false,
            twoFactorAuth: String? = null,
            emailAuth: String? = null,
            clientId: Long? = null,
        ) {
            val steamUser = instance!!._steamUser!!

            // Sensitive info, only print in DEBUG build.
            if (BuildConfig.DEBUG) {
                Timber.d(
                    "Login Information\n\tUsername: " +
                        "$username\n\tAccessToken: " +
                        "$accessToken\n\tRefreshToken: " +
                        "$refreshToken\n\tPassword: " +
                        "$password\n\tShouldRememberPass: " +
                        "$shouldRememberPassword\n\tTwoFactorAuth: " +
                        "$twoFactorAuth\n\tEmailAuth: $emailAuth",
                )
            }

            PrefManager.username = username

            if ((password != null && shouldRememberPassword) || refreshToken != null) {
                if (password != null) {
                    PrefManager.password = password
                }

                if (accessToken != null) {
                    PrefManager.password = ""
                    PrefManager.accessToken = accessToken
                }

                if (refreshToken != null) {
                    PrefManager.password = ""
                    PrefManager.refreshToken = refreshToken
                }

                if (clientId != null) {
                    PrefManager.clientId = clientId
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
                    password = if (password != null) {
                        SteamUtils.removeSpecialChars(password)
                            .trim()
                    } else {
                        null
                    },
                    shouldRememberPassword = shouldRememberPassword,
                    twoFactorCode = twoFactorAuth,
                    authCode = emailAuth,
                    accessToken = refreshToken,
                    // Set LoginID to a non-zero value if you have another client connected using the same account,
                    // the same private ip, and same public ip.
                    // source: https://github.com/Longi94/JavaSteam/blob/08690d0aab254b44b0072ed8a4db2f86d757109b/javasteam-samples/src/main/java/in/dragonbra/javasteamsamples/_000_authentication/SampleLogonAuthentication.java#L146C13-L147C56
                    loginID = SteamUtils.getUniqueDeviceId(instance!!),
                    machineName = SteamUtils.getMachineName(instance!!),
                ),
            )
        }

        suspend fun startLoginWithCredentials(
            username: String,
            password: String,
            shouldRememberPassword: Boolean,
            authenticator: IAuthenticator,
        ) = withContext(Dispatchers.IO) {
            Timber.i("Logging in via credentials.")

            instance!!.steamClient?.let { steamClient ->
                val authDetails = AuthSessionDetails().apply {
                    this.username = username.trim()
                    this.password = password.trim()
                    this.persistentSession = shouldRememberPassword
                    this.authenticator = authenticator
                    this.deviceFriendlyName = SteamUtils.getMachineName(instance!!)
                }

                val authSession = steamClient.authentication.beginAuthSessionViaCredentials(authDetails, this).await()

                PluviaApp.events.emit(SteamEvent.LogonStarted(username))

                val pollResult = authSession.pollingWaitForResult().await()

                if (pollResult.accountName.isNotEmpty() && pollResult.refreshToken.isNotEmpty()) {
                    login(
                        clientId = authSession.clientID,
                        username = pollResult.accountName,
                        accessToken = pollResult.accessToken,
                        refreshToken = pollResult.refreshToken,
                        shouldRememberPassword = shouldRememberPassword,
                    )
                }

                return@withContext
            }

            Timber.e("Could not logon: Failed to connect to Steam")
            PluviaApp.events.emit(SteamEvent.LogonEnded(username, LoginResult.Failed))
        }

        suspend fun startLoginWithQr() = withContext(Dispatchers.IO) {
            Timber.i("Logging in via QR.")

            val steamClient = instance!!.steamClient

            if (steamClient != null) {
                isWaitingForQRAuth = true

                val authDetails = AuthSessionDetails().apply {
                    deviceFriendlyName = SteamUtils.getMachineName(instance!!)
                }

                val authSession = steamClient.authentication.beginAuthSessionViaQR(authDetails, this).await()

                // Steam will periodically refresh the challenge url, this callback allows you to draw a new qr code.
                authSession.challengeUrlChanged = instance

                PluviaApp.events.emit(SteamEvent.QrChallengeReceived(authSession.challengeUrl))

                Timber.d("PollingInterval: ${authSession.pollingInterval.toLong()}")

                var authPollResult: AuthPollResult? = null

                while (isWaitingForQRAuth && authPollResult == null) {
                    try {
                        authPollResult = authSession.pollAuthSessionStatus(this).await()
                    } catch (e: Exception) {
                        Timber.e("Poll auth session status error: $e")

                        break
                    }

                    // Sensitive info, only print in DEBUG build.
                    if (BuildConfig.DEBUG && authPollResult != null) {
                        Timber.d(
                            "AccessToken: %s\nAccountName: %s\nRefreshToken: %s\nNewGuardData: %s",
                            authPollResult.accessToken,
                            authPollResult.accountName,
                            authPollResult.refreshToken,
                            authPollResult.newGuardData ?: "No new guard data",
                        )
                    }

                    delay(authSession.pollingInterval.toLong())
                }

                isWaitingForQRAuth = false

                PluviaApp.events.emit(SteamEvent.QrAuthEnded(authPollResult != null))

                // there is a chance qr got cancelled and there is no authPollResult
                if (authPollResult == null) {
                    Timber.w("Got no auth poll result")
                    return@withContext
                }

                login(
                    clientId = authSession.clientID,
                    username = authPollResult.accountName,
                    accessToken = authPollResult.accessToken,
                    refreshToken = authPollResult.refreshToken,
                )
            } else {
                Timber.e("Could not start QR logon: Failed to connect to Steam")

                PluviaApp.events.emit(SteamEvent.QrAuthEnded(false))
            }
        }

        fun stopLoginWithQr() {
            Timber.i("Stopping QR polling")

            isWaitingForQRAuth = false
        }

        fun stop() {
            instance?.let { steamInstance ->
                steamInstance.serviceScope.launch {
                    steamInstance.stop()
                }
            }
        }

        fun logOut() {
            CoroutineScope(Dispatchers.Default).launch {
                // isConnected = false

                isLoggingOut = true

                performLogOffDuties()

                val steamUser = instance!!._steamUser!!
                steamUser.logOff()
            }
        }

        private fun clearUserData() {
            PrefManager.clearPreferences()

            with(instance!!) {
                serviceScope.launch {
                    changeNumbersDao.deleteAll()
                    fileChangeListsDao.deleteAll()
                }

                appInfo.clear()
                packageInfo.clear()
            }

            isLoggingIn = false
        }

        private fun performLogOffDuties() {
            val username = PrefManager.username

            clearUserData()

            PluviaApp.events.emit(SteamEvent.LoggedOut(username))
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        notificationHelper = NotificationHelper(applicationContext)

        // To view log messages in android logcat properly
        val logger = object : LogListener {
            override fun onLog(clazz: Class<*>, message: String?, throwable: Throwable?) {
                val logMessage = message ?: "No message given"
                Timber.i(throwable, "[${clazz.simpleName}] -> $logMessage")
            }

            override fun onError(clazz: Class<*>, message: String?, throwable: Throwable?) {
                val logMessage = message ?: "No message given"
                Timber.e(throwable, "[${clazz.simpleName}] -> $logMessage")
            }
        }
        LogManager.addListener(logger)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Notification intents
        when (intent?.action) {
            NotificationHelper.ACTION_EXIT -> {
                PluviaApp.events.emit(AndroidEvent.EndProcess)
                return START_NOT_STICKY
            }
        }

        if (!isRunning) {
            Timber.i("Using server list path: $serverListPath")

            val configuration = SteamConfiguration.create {
                it.withProtocolTypes(PROTOCOL_TYPES)
                it.withCellID(PrefManager.cellId)
                it.withServerListProvider(FileServerListProvider(File(serverListPath)))
                it.withManifestProvider(FileManifestProvider(File(depotManifestsPath)))
            }

            // create our steam client instance
            steamClient = SteamClient(configuration)

            // remove callbacks we're not using.
            steamClient!!.removeHandler(SteamGameServer::class.java)
            steamClient!!.removeHandler(SteamMasterServer::class.java)
            steamClient!!.removeHandler(SteamWorkshop::class.java)
            steamClient!!.removeHandler(SteamScreenshots::class.java)
            steamClient!!.removeHandler(SteamUserStats::class.java)

            // create the callback manager which will route callbacks to function calls
            _callbackManager = CallbackManager(steamClient!!)
            _unifiedMessages = steamClient!!.getHandler<SteamUnifiedMessages>()

            // get the different handlers to be used throughout the service
            _steamUser = steamClient!!.getHandler(SteamUser::class.java)
            _steamApps = steamClient!!.getHandler(SteamApps::class.java)
            _steamFriends = steamClient!!.getHandler(SteamFriends::class.java)
            _steamCloud = steamClient!!.getHandler(SteamCloud::class.java)

            // subscribe to the callbacks we are interested in
            with(_callbackSubscriptions) {
                with(_callbackManager!!) {
                    add(subscribe(ConnectedCallback::class.java, ::onConnected))
                    add(subscribe(DisconnectedCallback::class.java, ::onDisconnected))
                    add(subscribe(LoggedOnCallback::class.java, ::onLoggedOn))
                    add(subscribe(LoggedOffCallback::class.java, ::onLoggedOff))
                    add(subscribe(PersonaStatesCallback::class.java, ::onPersonaStateReceived))
                    add(subscribe(LicenseListCallback::class.java, ::onLicenseList))
                    add(subscribe(PICSProductInfoCallback::class.java, ::onPICSProductInfo))
                    add(subscribe(NicknameListCallback::class.java, ::onNicknameList))
                    add(subscribe(FriendsListCallback::class.java, ::onFriendsList))
                }
            }

            isRunning = true

            // we should use Dispatchers.IO here since we are running a sleeping/blocking function
            // "The idea is that the IO dispatcher spends a lot of time waiting (IO blocked),
            // while the Default dispatcher is intended for CPU intensive tasks, where there
            // is little or no sleep."
            // source: https://stackoverflow.com/a/59040920
            serviceScope.launch {
                while (isRunning) {
                    // logD("runWaitCallbacks")

                    try {
                        _callbackManager!!.runWaitCallbacks(1000L)
                    } catch (e: Exception) {
                        Timber.e("runWaitCallbacks failed: $e")
                    }
                }
            }

            connectToSteam()
        }

        val notification = notificationHelper.createForegroundNotification("Starting up...")
        startForeground(1, notification)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        stopForeground(STOP_FOREGROUND_REMOVE)
        notificationHelper.cancel()

        serviceScope.launch {
            stop()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun connectToSteam() {
        isConnecting = true

        CoroutineScope(Dispatchers.Default).launch {
            // this call errors out if run on the main thread
            steamClient!!.connect()

            delay(5000)

            if (!isConnected) {
                Timber.w("Failed to connect to Steam, marking endpoint bad and force disconnecting")

                try {
                    steamClient!!.servers.tryMark(steamClient!!.currentEndpoint, PROTOCOL_TYPES, ServerQuality.BAD)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to mark endpoint as bad:")
                }

                try {
                    steamClient!!.disconnect()
                } catch (e: Exception) {
                    Timber.e(e, "There was an issue when disconnecting:")
                }
            }
        }
    }

    private suspend fun stop() {
        Timber.i("Stopping Steam service")
        if (steamClient != null && steamClient!!.isConnected) {
            isStopping = true

            steamClient!!.disconnect()

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
        isLoggingOut = false
        isWaitingForQRAuth = false
        isReceivingLicenseList = false
        isRequestingPkgInfo = false
        isRequestingAppInfo = false

        PrefManager.appInstallPath = defaultAppInstallPath
        PrefManager.appStagingPath = defaultAppStagingPath

        steamClient = null
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

    @Suppress("UNUSED_PARAMETER", "unused")
    private fun onConnected(callback: ConnectedCallback) {
        Timber.i("Connected to Steam")

        retryAttempt = 0
        isConnecting = false
        isConnected = true

        var isAutoLoggingIn = false

        if (PrefManager.username.isNotEmpty() && (PrefManager.refreshToken.isNotEmpty() || PrefManager.password.isNotEmpty())) {
            isAutoLoggingIn = true

            login(
                username = PrefManager.username,
                refreshToken = PrefManager.refreshToken,
                password = PrefManager.password.ifEmpty { null },
                shouldRememberPassword = PrefManager.password.isNotEmpty(),
            )
        }

        PluviaApp.events.emit(SteamEvent.Connected(isAutoLoggingIn))
    }

    private fun onDisconnected(callback: DisconnectedCallback) {
        Timber.i("Disconnected from Steam. User initiated: ${callback.isUserInitiated}")

        isConnected = false

        if (!isStopping && retryAttempt < MAX_RETRY_ATTEMPTS) {
            retryAttempt++

            Timber.w("Attempting to reconnect (retry $retryAttempt)")

            // isLoggingOut = false

            connectToSteam()
        } else {
            PluviaApp.events.emit(SteamEvent.Disconnected)

            clearValues()

            stopSelf()
        }
    }

    private fun reconnect() {
        notificationHelper.notify("Retrying...")

        isConnected = false
        isConnecting = true

        PluviaApp.events.emit(SteamEvent.Disconnected)

        steamClient!!.disconnect()
    }

    /**
     * Request a fresh state of Friend's PersonaStates
     */
    private fun refreshPersonaStates() {
        val request = CChat_RequestFriendPersonaStates_Request.newBuilder().build()
        _unifiedChat?.requestFriendPersonaStates(request)
    }

    private fun onLoggedOn(callback: LoggedOnCallback) {
        Timber.i("Logged onto Steam: ${callback.result}")

        when (callback.result) {
            EResult.TryAnotherCM -> {
                _loginResult = LoginResult.Failed
                reconnect()
            }

            EResult.OK -> {
                // save the current cellid somewhere. if we lose our saved server list, we can use this when retrieving
                // servers from the Steam Directory.
                PrefManager.cellId = callback.cellID

                // Create Unified Handlers
                _unifiedChat = _unifiedMessages!!.createService(Chat::class.java)

                // retrieve persona data of logged in user
                serviceScope.launch {
                    requestUserPersona()
                }

                // since we automatically receive the license list from steam on log on
                isReceivingLicenseList = true

                // Tell steam we're online, this allows friends to update.
                _steamFriends?.setPersonaState(PrefManager.personaState)

                notificationHelper.notify("Connected")

                _loginResult = LoginResult.Success
            }

            else -> {
                clearUserData()

                _loginResult = LoginResult.Failed

                reconnect()
            }
        }

        PluviaApp.events.emit(SteamEvent.LogonEnded(PrefManager.username, _loginResult))

        isLoggingIn = false
    }

    private fun onLoggedOff(callback: LoggedOffCallback) {
        Timber.i("Logged off of Steam: ${callback.result}")

        notificationHelper.notify("Disconnected...")

        if (isLoggingOut || callback.result == EResult.LogonSessionReplaced) {
            performLogOffDuties()

            serviceScope.launch {
                stop()
            }
        } else if (callback.result == EResult.LoggedInElsewhere) {
            // received when a client runs an app and wants to forcibly close another
            // client running an app
            PluviaApp.events.emit(SteamEvent.ForceCloseApp)

            reconnect()
        } else {
            reconnect()
        }
    }

    override fun onChanged(qrAuthSession: QrAuthSession?) {
        Timber.i("QR code changed -> ${if (BuildConfig.DEBUG) qrAuthSession?.challengeUrl else "[redacted]"}")

        if (qrAuthSession != null) {
            PluviaApp.events.emit(SteamEvent.QrChallengeReceived(qrAuthSession.challengeUrl))
        }
    }

    private fun onNicknameList(callback: NicknameListCallback) {
        Timber.d("Nickname list called: ${callback.nicknames.size}")
        dbScope.launch {
            db.withTransaction {
                friendDao.clearAllNicknames()
                friendDao.updateNicknames(callback.nicknames)
            }
        }
    }

    private fun onFriendsList(callback: FriendsListCallback) {
        Timber.d("onFriendsList ${callback.friendList.size}")
        dbScope.launch {
            db.withTransaction {
                callback.friendList
                    .filter { friend ->
                        friend.steamID.isIndividualAccount
                    }
                    .forEach { filteredFriend ->
                        val friendId = filteredFriend.steamID.convertToUInt64()
                        val friend = friendDao.findFriend(friendId).first()

                        if (friend == null) {
                            // Not in the DB, create them.
                            val friendToAdd = SteamFriend(
                                id = filteredFriend.steamID.convertToUInt64(),
                                relation = filteredFriend.relationship,
                            )

                            friendDao.insert(friendToAdd)
                        } else {
                            // In the DB, update them.
                            friendDao.update(
                                friend.copy(relation = filteredFriend.relationship),
                            )
                        }
                    }

                // Add logged in account if we don't exist yet.
                val selfId = userSteamId!!.convertToUInt64()
                val self = friendDao.findFriend(selfId).first()

                if (self == null) {
                    friendDao.insert(SteamFriend(id = selfId))
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

        // Timber.d("Persona state received: ${callback.name}")

        dbScope.launch {
            db.withTransaction {
                val id = callback.friendID.convertToUInt64()
                val friend = friendDao.findFriend(id).first()

                if (friend == null) {
                    Timber.w("onPersonaStateReceived: failed to find friend to update: $id")
                    return@withTransaction
                }

                friendDao.update(
                    friend.copy(
                        statusFlags = callback.statusFlags,
                        state = callback.state ?: EPersonaState.Offline,
                        stateFlags = callback.stateFlags,
                        gameAppID = callback.gameAppID,
                        gameID = callback.gameID,
                        gameName = callback.gameName,
                        gameServerIP = NetHelpers.getIPAddress(callback.gameServerIP),
                        gameServerPort = callback.gameServerPort,
                        queryPort = callback.queryPort,
                        sourceSteamID = callback.sourceSteamID,
                        gameDataBlob = callback.gameDataBlob.decodeToString(),
                        name = callback.name,
                        avatarHash = callback.avatarHash.toHexString(),
                        lastLogOff = callback.lastLogOff,
                        lastLogOn = callback.lastLogOn,
                        clanRank = callback.clanRank,
                        clanTag = callback.clanTag,
                        onlineSessionInstances = callback.onlineSessionInstances,
                    ),
                )
            }
        }

        // Send off an event if we change states.
        if (callback.friendID == steamClient!!.steamID) {
            Timber.d("Emitting PersonaStateReceived")

            dbScope.launch {
                val id = callback.friendID.convertToUInt64()
                val friend = friendDao.findFriend(id).first()

                PluviaApp.events.emit(SteamEvent.PersonaStateReceived(friend))
            }
        }
    }

    private fun onLicenseList(callback: LicenseListCallback) {
        Timber.i("Received License List ${callback.result}, size: ${callback.licenseList.size}")

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
                    licenseType = license.licenseType ?: ELicenseType.NoLicense,
                    paymentMethod = license.paymentMethod ?: EPaymentMethod.None,
                    purchaseCountryCode = license.purchaseCode,
                    appIds = IntArray(0),
                    depotIds = IntArray(0),
                )
            }

            isRequestingPkgInfo = true

            _steamApps!!.picsGetProductInfo(
                apps = emptyList(),
                packages = callback.licenseList.map { PICSRequest(it.packageID, it.accessToken) },
            )
        }

        isReceivingLicenseList = false
    }

    private fun onPICSProductInfo(callback: PICSProductInfoCallback) {
        // logD("Received PICSProductInfo")

        if (callback.packages.isNotEmpty()) {
            for (pkg in callback.packages.values) {
                // logD("Received pkg ${pkg.id}")

                packageInfo[pkg.id]?.let { pi ->
                    pi.appIds = pkg.keyValues["appids"].children.map { it.asInteger() }.toIntArray()
                    pi.depotIds = pkg.keyValues["depotids"].children.map { it.asInteger() }.toIntArray()
                }
            }

            isRequestingPkgInfo = false
            isRequestingAppInfo = true

            _steamApps?.picsGetProductInfo(
                apps = packageInfo.values
                    .flatMap { it.appIds.asIterable() }
                    .map { PICSRequest(it) },
                packages = emptyList(),
            )
        }

        if (callback.apps.isNotEmpty()) {
            val apps = callback.apps.values.toTypedArray()

            for (i in apps.indices) {
                val app = apps[i]

                val pkg = packageInfo.values.firstOrNull { it.appIds.contains(app.id) }

                // logD("Received app ${app.id}")

                val launchConfigs = app.keyValues["config"]["launch"].children

                appInfo[app.id] = AppInfo(
                    appId = app.id,
                    receiveIndex = packageInfo.values
                        .filter { it.receiveIndex < (pkg?.receiveIndex ?: Int.MAX_VALUE) }
                        .fold(initial = 0) { accum, pkgInfo -> accum + pkgInfo.appIds.size } + i,
                    packageId = pkg?.packageId ?: INVALID_PKG_ID,
                    depots = app.keyValues["depots"].children
                        .filter { currentDepot ->
                            currentDepot.name.toIntOrNull() != null
                        }
                        .associate { currentDepot ->
                            val depotId = currentDepot.name.toInt()

                            // val currentDepot = app.keyValues["depots"]["$depotId"]

                            val manifests = currentDepot["manifests"].children.generateManifest()

                            val encryptedManifests = currentDepot["encryptedManifests"].children.generateManifest()

                            depotId to DepotInfo(
                                depotId = depotId,
                                dlcAppId = currentDepot["dlcappid"].asInteger(INVALID_APP_ID),
                                depotFromApp = currentDepot["depotfromapp"].asInteger(
                                    INVALID_APP_ID,
                                ),
                                sharedInstall = currentDepot["sharedinstall"].asBoolean(),
                                osList = OS.from(currentDepot["config"]["oslist"].value),
                                osArch = OSArch.from(currentDepot["config"]["osarch"].value),
                                manifests = manifests,
                                encryptedManifests = encryptedManifests,
                            )
                        },
                    branches = app.keyValues["depots"]["branches"].children.associate {
                        it.name to BranchInfo(
                            name = it.name,
                            buildId = it["buildid"].asLong(),
                            pwdRequired = it["pwdrequired"].asBoolean(),
                            timeUpdated = Date(it["timeupdated"].asLong() * 1000L),
                        )
                    },
                    name = app.keyValues["common"]["name"].value.orEmpty(),
                    type = AppType.valueOf(
                        app.keyValues["common"]["type"].value?.lowercase() ?: "invalid",
                    ),
                    osList = OS.from(app.keyValues["common"]["oslist"].value),
                    releaseState = ReleaseState.valueOf(
                        app.keyValues["common"]["releasestate"].value ?: "released",
                    ),
                    metacriticScore = app.keyValues["common"]["metacritic_score"].asByte(),
                    metacriticFullUrl = app.keyValues["common"]["metacritic_fullurl"].value.orEmpty(),
                    logoHash = app.keyValues["common"]["logo"].value.orEmpty(),
                    logoSmallHash = app.keyValues["common"]["logo_small"].value.orEmpty(),
                    iconHash = app.keyValues["common"]["icon"].value.orEmpty(),
                    clientIconHash = app.keyValues["common"]["clienticon"].value.orEmpty(),
                    clientTgaHash = app.keyValues["common"]["clienttga"].value.orEmpty(),
                    smallCapsule = app.keyValues["common"]["small_capsule"].children.toLangImgMap(),
                    headerImage = app.keyValues["common"]["header_image"].children.toLangImgMap(),
                    libraryAssets = LibraryAssetsInfo(
                        libraryCapsule = LibraryCapsuleInfo(
                            image = app.keyValues["common"]["library_assets_full"]["library_capsule"]["image"].children.toLangImgMap(),
                            image2x = app.keyValues["common"]["library_assets_full"]["library_capsule"]["image2x"].children.toLangImgMap(),
                        ),
                        libraryHero = LibraryHeroInfo(
                            image = app.keyValues["common"]["library_assets_full"]["library_hero"]["image"].children.toLangImgMap(),
                            image2x = app.keyValues["common"]["library_assets_full"]["library_hero"]["image2x"].children.toLangImgMap(),
                        ),
                        libraryLogo = LibraryLogoInfo(
                            image = app.keyValues["common"]["library_assets_full"]["library_logo"]["image"].children.toLangImgMap(),
                            image2x = app.keyValues["common"]["library_assets_full"]["library_logo"]["image2x"].children.toLangImgMap(),
                        ),
                    ),
                    primaryGenre = app.keyValues["common"]["primary_genre"].asBoolean(),
                    reviewScore = app.keyValues["common"]["review_score"].asByte(),
                    reviewPercentage = app.keyValues["common"]["review_percentage"].asByte(),
                    controllerSupport = ControllerSupport.valueOf(
                        app.keyValues["common"]["controller_support"].value ?: "none",
                    ),
                    demoOfAppId = app.keyValues["common"]["extended"]["demoofappid"].asInteger(),
                    developer = app.keyValues["common"]["extended"]["developer"].value.orEmpty(),
                    publisher = app.keyValues["common"]["extended"]["publisher"].value.orEmpty(),
                    homepageUrl = app.keyValues["common"]["extended"]["homepage"].value.orEmpty(),
                    gameManualUrl = app.keyValues["common"]["extended"]["gamemanualurl"].value.orEmpty(),
                    loadAllBeforeLaunch = app.keyValues["common"]["extended"]["loadallbeforelaunch"].asBoolean(),
                    // dlcAppIds = (app.keyValues["common"]["extended"]["listofdlc"].value).Split(",").Select(uint.Parse).ToArray(),
                    dlcAppIds = IntArray(0),
                    isFreeApp = app.keyValues["common"]["extended"]["isfreeapp"].asBoolean(),
                    dlcForAppId = app.keyValues["common"]["extended"]["dlcforappid"].asInteger(),
                    mustOwnAppToPurchase = app.keyValues["common"]["extended"]["mustownapptopurchase"].asInteger(),
                    dlcAvailableOnStore = app.keyValues["common"]["extended"]["dlcavailableonstore"].asBoolean(),
                    optionalDlc = app.keyValues["common"]["extended"]["optionaldlc"].asBoolean(),
                    gameDir = app.keyValues["common"]["extended"]["gamedir"].value.orEmpty(),
                    installScript = app.keyValues["common"]["extended"]["installscript"].value.orEmpty(),
                    noServers = app.keyValues["common"]["extended"]["noservers"].asBoolean(),
                    order = app.keyValues["common"]["extended"]["order"].asBoolean(),
                    primaryCache = app.keyValues["common"]["extended"]["primarycache"].asInteger(),
                    // validOSList = app.keyValues["common"]["extended"]["validoslist"].value!.Split(",").Select(Enum.Parse<OS>).Aggregate((os1, os2) => os1 | os2),
                    validOSList = EnumSet.of(OS.none),
                    thirdPartyCdKey = app.keyValues["common"]["extended"]["thirdpartycdkey"].asBoolean(),
                    visibleOnlyWhenInstalled = app.keyValues["common"]["extended"]["visibleonlywheninstalled"].asBoolean(),
                    visibleOnlyWhenSubscribed = app.keyValues["common"]["extended"]["visibleonlywhensubscribed"].asBoolean(),
                    launchEulaUrl = app.keyValues["common"]["extended"]["launcheula"].value.orEmpty(),
                    requireDefaultInstallFolder = app.keyValues["common"]["config"]["requiredefaultinstallfolder"].asBoolean(),
                    contentType = app.keyValues["common"]["config"]["contentType"].asInteger(),
                    installDir = app.keyValues["common"]["config"]["installdir"].value.orEmpty(),
                    useLaunchCmdLine = app.keyValues["common"]["config"]["uselaunchcommandline"].asBoolean(),
                    launchWithoutWorkshopUpdates = app.keyValues["common"]["config"]["launchwithoutworkshopupdates"].asBoolean(),
                    useMms = app.keyValues["common"]["config"]["usemms"].asBoolean(),
                    installScriptSignature = app.keyValues["common"]["config"]["installscriptsignature"].value.orEmpty(),
                    installScriptOverride = app.keyValues["common"]["config"]["installscriptoverride"].asBoolean(),
                    config = ConfigInfo(
                        installDir = app.keyValues["config"]["installdir"].value.orEmpty(),
                        launch = launchConfigs.map {
                            LaunchInfo(
                                executable = it["executable"].value?.replace('\\', '/').orEmpty(),
                                workingDir = it["workingdir"].value?.replace('\\', '/').orEmpty(),
                                description = it["description"].value.orEmpty(),
                                type = it["type"].value.orEmpty(),
                                configOS = OS.from(it["config"]["oslist"].value),
                                configArch = OSArch.from(it["config"]["osarch"].value),
                            )
                        }.toTypedArray(),
                        steamControllerTemplateIndex = app.keyValues["config"]["steamcontrollertemplateindex"].asInteger(),
                        steamControllerTouchTemplateIndex = app.keyValues["config"]["steamcontrollertouchtemplateindex"].asInteger(),
                    ),
                    ufs = UFS(
                        quota = app.keyValues["ufs"]["quota"].asInteger(),
                        maxNumFiles = app.keyValues["ufs"]["maxnumfiles"].asInteger(),
                        saveFilePatterns = app.keyValues["ufs"]["savefiles"].children.map {
                            SaveFilePattern(
                                root = PathType.from(it["root"].value),
                                path = it["path"].value.orEmpty(),
                                pattern = it["pattern"].value.orEmpty(),
                            )
                        }.toTypedArray(),
                    ),
                )

                // // val isBaba = app.id == 736260
                // // val isNoita = app.id == 881100
                // // val isHades = app.id == 1145360
                // // val isCS2 = app.id == 730
                // // val isPsuedo = app.id == 2365810
                // // val isPathway = app.id == 546430
                // // val isSeaOfStars = app.id == 1244090
                // // val isMessenger = app.id == 764790
                // // val isWargroove = app.id == 607050
                // // val isTetrisEffect = app.id == 1003590
                // // val isLittleKitty = app.id == 1177980
                // val isFactorio = app.id == 427520
                // if (isFactorio) {
                // 	logD("${app.id}: ${app.keyValues["common"]["name"].value}");
                // 	printAllKeyValues(app.keyValues)
                // 	// getPkgInfoOf(app.id)?.let {
                // 	// 	printAllKeyValues(it.original)
                //     // }
                // }
            }

            isRequestingAppInfo = false

            PluviaApp.events.emit(SteamEvent.AppInfoReceived)
        }
    }
}
