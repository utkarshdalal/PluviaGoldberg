package com.utkarshdalal.PluviaGoldberg.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.room.withTransaction
import com.utkarshdalal.PluviaGoldberg.BuildConfig
import com.utkarshdalal.PluviaGoldberg.PluviaApp
import com.utkarshdalal.PluviaGoldberg.PrefManager
import com.utkarshdalal.PluviaGoldberg.data.DepotInfo
import com.utkarshdalal.PluviaGoldberg.data.DownloadInfo
import com.utkarshdalal.PluviaGoldberg.data.Emoticon
import com.utkarshdalal.PluviaGoldberg.data.GameProcessInfo
import com.utkarshdalal.PluviaGoldberg.data.LaunchInfo
import com.utkarshdalal.PluviaGoldberg.data.OwnedGames
import com.utkarshdalal.PluviaGoldberg.data.PostSyncInfo
import com.utkarshdalal.PluviaGoldberg.data.SteamApp
import com.utkarshdalal.PluviaGoldberg.data.SteamFriend
import com.utkarshdalal.PluviaGoldberg.data.SteamLicense
import com.utkarshdalal.PluviaGoldberg.data.UserFileInfo
import com.utkarshdalal.PluviaGoldberg.db.PluviaDatabase
import com.utkarshdalal.PluviaGoldberg.db.dao.ChangeNumbersDao
import com.utkarshdalal.PluviaGoldberg.db.dao.EmoticonDao
import com.utkarshdalal.PluviaGoldberg.db.dao.FileChangeListsDao
import com.utkarshdalal.PluviaGoldberg.db.dao.FriendMessagesDao
import com.utkarshdalal.PluviaGoldberg.db.dao.SteamAppDao
import com.utkarshdalal.PluviaGoldberg.db.dao.SteamFriendDao
import com.utkarshdalal.PluviaGoldberg.db.dao.SteamLicenseDao
import com.utkarshdalal.PluviaGoldberg.enums.LoginResult
import com.utkarshdalal.PluviaGoldberg.enums.OS
import com.utkarshdalal.PluviaGoldberg.enums.OSArch
import com.utkarshdalal.PluviaGoldberg.enums.SaveLocation
import com.utkarshdalal.PluviaGoldberg.enums.SyncResult
import com.utkarshdalal.PluviaGoldberg.events.AndroidEvent
import com.utkarshdalal.PluviaGoldberg.events.SteamEvent
import com.utkarshdalal.PluviaGoldberg.service.callback.EmoticonListCallback
import com.utkarshdalal.PluviaGoldberg.service.handler.PluviaHandler
import com.utkarshdalal.PluviaGoldberg.utils.FileUtils
import com.utkarshdalal.PluviaGoldberg.utils.SteamUtils
import com.utkarshdalal.PluviaGoldberg.utils.generateSteamApp
import com.utkarshdalal.PluviaGoldberg.utils.timeChunked
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
import `in`.dragonbra.javasteam.enums.EFriendRelationship
import `in`.dragonbra.javasteam.enums.ELicenseFlags
import `in`.dragonbra.javasteam.enums.EOSType
import `in`.dragonbra.javasteam.enums.EPersonaState
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.networking.steam3.ProtocolTypes
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientObjects.ECloudPendingRemoteOperation
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesFamilygroupsSteamclient
import `in`.dragonbra.javasteam.rpc.service.FamilyGroups
import `in`.dragonbra.javasteam.steam.authentication.AuthPollResult
import `in`.dragonbra.javasteam.steam.authentication.AuthSessionDetails
import `in`.dragonbra.javasteam.steam.authentication.AuthenticationException
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
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.PICSChangesCallback
import `in`.dragonbra.javasteam.steam.handlers.steamapps.callback.PICSProductInfoCallback
import `in`.dragonbra.javasteam.steam.handlers.steamcloud.SteamCloud
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.AliasHistoryCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.FriendsListCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.NicknameListCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.PersonaStateCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.ProfileInfoCallback
import `in`.dragonbra.javasteam.steam.handlers.steamgameserver.SteamGameServer
import `in`.dragonbra.javasteam.steam.handlers.steammasterserver.SteamMasterServer
import `in`.dragonbra.javasteam.steam.handlers.steamscreenshots.SteamScreenshots
import `in`.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages
import `in`.dragonbra.javasteam.steam.handlers.steamuser.ChatMode
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
import java.io.Closeable
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Collections
import java.util.EnumSet
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.io.path.pathString
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.future.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber

@AndroidEntryPoint
class SteamService : Service(), IChallengeUrlChanged {

    @Inject
    lateinit var db: PluviaDatabase

    @Inject
    lateinit var licenseDao: SteamLicenseDao

    @Inject
    lateinit var appDao: SteamAppDao

    @Inject
    lateinit var friendDao: SteamFriendDao

    @Inject
    lateinit var messagesDao: FriendMessagesDao

    @Inject
    lateinit var emoticonDao: EmoticonDao

    @Inject
    lateinit var changeNumbersDao: ChangeNumbersDao

    @Inject
    lateinit var fileChangeListsDao: FileChangeListsDao

    private lateinit var notificationHelper: NotificationHelper

    internal var callbackManager: CallbackManager? = null
    internal var steamClient: SteamClient? = null
    internal val callbackSubscriptions: ArrayList<Closeable> = ArrayList()

    private var _unifiedFriends: SteamUnifiedFriends? = null
    private var _steamUser: SteamUser? = null
    private var _steamApps: SteamApps? = null
    private var _steamFriends: SteamFriends? = null
    private var _steamCloud: SteamCloud? = null
    private var _steamFamilyGroups: FamilyGroups? = null

    private var _loginResult: LoginResult = LoginResult.Failed

    private var retryAttempt = 0

    private val appIdFlowSender: MutableSharedFlow<Int> = MutableSharedFlow()
    private val appIdFlowReceiver = appIdFlowSender.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val onEndProcess: (AndroidEvent.EndProcess) -> Unit = {
        SteamService.stop()
    }

    // The current shared family group the logged in user is joined to.
    private var familyGroupMembers: ArrayList<Int> = arrayListOf()

    companion object {
        private val PICS_CHANGE_CHECK_DELAY = 60.seconds

        const val MAX_SIMULTANEOUS_PICS_REQUESTS = 50
        const val MAX_RETRY_ATTEMPTS = 20

        const val INVALID_APP_ID: Int = Int.MAX_VALUE
        const val INVALID_PKG_ID: Int = Int.MAX_VALUE

        /**
         * Default timeout to use when making requests
         */
        var requestTimeout = 10.seconds

        /**
         * Default timeout to use when reading the response body
         */
        var responseTimeout = 60.seconds

        private val PROTOCOL_TYPES = EnumSet.of(ProtocolTypes.WEB_SOCKET)

        private var instance: SteamService? = null

        private val downloadJobs = ConcurrentHashMap<Int, DownloadInfo>()

        private var syncInProgress: Boolean = false

        var isStopping: Boolean = false
            private set
        var isConnected: Boolean = false
            private set
        var isRunning: Boolean = false
            private set
        var isLoggingOut: Boolean = false
            private set
        val isLoggedIn: Boolean
            get() = instance?.steamClient?.steamID?.isValid == true
        var isWaitingForQRAuth: Boolean = false
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

        val familyMembers: List<Int>
            get() = instance!!.familyGroupMembers

        suspend fun setPersonaState(state: EPersonaState) = withContext(Dispatchers.IO) {
            PrefManager.personaState = state
            instance?._steamFriends?.setPersonaState(state)
        }

        suspend fun requestUserPersona() = withContext(Dispatchers.IO) {
            // in order to get user avatar url and other info
            userSteamId?.let { instance?._steamFriends?.requestFriendInfo(it) }
        }

        suspend fun getPersonaStateOf(steamId: SteamID): SteamFriend? = withContext(Dispatchers.IO) {
            instance!!.db.steamFriendDao().findFriend(steamId.convertToUInt64())
        }

        fun getPkgInfoOf(appId: Int): SteamLicense? {
            return runBlocking(Dispatchers.IO) {
                instance?.licenseDao?.findLicense(
                    instance?.appDao?.findApp(appId)?.packageId ?: INVALID_PKG_ID,
                )
            }
        }

        fun getAppInfoOf(appId: Int): SteamApp? {
            return runBlocking(Dispatchers.IO) { instance?.appDao?.findApp(appId) }
        }

        fun queueAppPICSRequests(apps: List<Int>) {
            if (apps.isEmpty()) {
                return
            }

            instance?.let { steamInstance ->
                steamInstance.scope.launch {
                    val flow = flowOf(*apps.toTypedArray())
                    steamInstance.appIdFlowSender.emitAll(flow)
                }
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
            }.orEmpty()
        }

        fun getOwnedAppDlc(appId: Int): Map<Int, DepotInfo> = getAppDlc(appId).filter {
            getPkgInfoOf(it.value.dlcAppId)?.let { pkg ->
                instance?.steamClient?.let { steamClient ->
                    pkg.ownerAccountId.contains(steamClient.steamID.accountID.toInt())
                }
            } == true
        }

        fun getDownloadableDepots(appId: Int): Map<Int, DepotInfo> = getAppInfoOf(appId)?.depots?.filter { depotEntry ->
            val depot = depotEntry.value

            (depot.manifests.isNotEmpty() || depot.sharedInstall) &&
                (depot.osList.contains(OS.windows) || (!depot.osList.contains(OS.linux) && !depot.osList.contains(OS.macos))) &&
                (depot.osArch == OSArch.Arch64 || depot.osArch == OSArch.Unknown) &&
                (depot.dlcAppId == INVALID_APP_ID || getOwnedAppDlc(appId).containsKey(depot.depotId))
        }.orEmpty()

        fun getAppDirPath(appId: Int): String {
            var appName = getAppInfoOf(appId)?.config?.installDir.orEmpty()

            if (appName.isEmpty()) {
                appName = getAppInfoOf(appId)?.name.orEmpty()
            }

            return Paths.get(PrefManager.appInstallPath, appName).pathString
        }

        fun deleteApp(appId: Int): Boolean {
            with(instance!!) {
                scope.launch {
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
                !FileUtils.assetExists(instance!!.assets, "imagefs_longjunyu_patched.txz")
            val indexOffset = if (needsImageFsDownload) 1 else 0

            val downloadInfo = DownloadInfo(depotIds.size + indexOffset).also { downloadInfo ->
                downloadInfo.setDownloadJob(
                    CoroutineScope(Dispatchers.IO).launch {
                        // TODO: change downloads to be one item/depot per job and connect them to the game requesting to download them
                        try {
                            downloadImageFs(
                                onDownloadProgress = { downloadInfo.setProgress(it, 0) },
                                parentScope = this,
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
            }.orEmpty()
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

                                val userAccountId = userSteamId!!.accountID.toInt()
                                GamePlayedInfo(
                                    gameId = gameProcess.appId.toLong(),
                                    processId = processId,
                                    ownerId = if (pkgInfo.ownerAccountId.contains(userAccountId)) {
                                        userAccountId
                                    } else {
                                        pkgInfo.ownerAccountId.first()
                                    },
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
                    "GameProcessInfo:%s",
                    gamesPlayed.joinToString("\n") { game ->
                        """
                        |   processId: ${game.processId}
                        |   gameId: ${game.gameId}
                        |   processes: ${
                            game.processIdList.joinToString("\n") { process ->
                                """
                                |   processId: ${process.processId}
                                |   processIdParent: ${process.processIdParent}
                                |   parentIsSteam: ${process.parentIsSteam}
                                """.trimMargin()
                            }
                        }
                        """.trimMargin()
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

                            postSyncInfo?.let { info ->
                                syncResult = info

                                if (info.syncResult == SyncResult.Success || info.syncResult == SyncResult.UpToDate) {
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
                                            syncResult = SyncResult.PendingOperations,
                                            pendingRemoteOperations = pendingRemoteOperations,
                                        )
                                    } else if (ignorePendingOperations &&
                                        pendingRemoteOperations.any {
                                            it.operation == ECloudPendingRemoteOperation.k_ECloudPendingRemoteOperationAppSessionActive
                                        }
                                    ) {
                                        steamInstance._steamUser!!.kickPlayingSession()
                                    }
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
            rememberSession: Boolean = false,
            twoFactorAuth: String? = null,
            emailAuth: String? = null,
            clientId: Long? = null,
        ) {
            val steamUser = instance!!._steamUser!!

            // Sensitive info, only print in DEBUG build.
            if (BuildConfig.DEBUG) {
                Timber.d(
                    """
                    Login Information:
                     Username: $username
                     AccessToken: $accessToken
                     RefreshToken: $refreshToken
                     Password: $password
                     Remember Session: $rememberSession
                     TwoFactorAuth: $twoFactorAuth
                     EmailAuth: $emailAuth
                    """.trimIndent(),
                )
            }

            PrefManager.username = username

            if ((password != null && rememberSession) || refreshToken != null) {
                if (accessToken != null) {
                    PrefManager.accessToken = accessToken
                }

                if (refreshToken != null) {
                    PrefManager.refreshToken = refreshToken
                }

                if (clientId != null) {
                    PrefManager.clientId = clientId
                }
            }

            val event = SteamEvent.LogonStarted(username)
            PluviaApp.events.emit(event)

            steamUser.logOn(
                LogOnDetails(
                    username = SteamUtils.removeSpecialChars(username).trim(),
                    password = password?.let { SteamUtils.removeSpecialChars(it).trim() },
                    shouldRememberPassword = rememberSession,
                    twoFactorCode = twoFactorAuth,
                    authCode = emailAuth,
                    accessToken = refreshToken,
                    loginID = SteamUtils.getUniqueDeviceId(instance!!),
                    machineName = SteamUtils.getMachineName(instance!!),
                    chatMode = ChatMode.NEW_STEAM_CHAT,
                ),
            )
        }

        suspend fun startLoginWithCredentials(
            username: String,
            password: String,
            rememberSession: Boolean,
            authenticator: IAuthenticator,
        ) = withContext(Dispatchers.IO) {
            try {
                Timber.i("Logging in via credentials.")

                instance!!.steamClient?.let { steamClient ->
                    val authDetails = AuthSessionDetails().apply {
                        this.username = username.trim()
                        this.password = password.trim()
                        this.persistentSession = rememberSession
                        this.authenticator = authenticator
                        this.deviceFriendlyName = SteamUtils.getMachineName(instance!!)
                    }

                    val event = SteamEvent.LogonStarted(username)
                    PluviaApp.events.emit(event)

                    val authSession = steamClient.authentication.beginAuthSessionViaCredentials(authDetails).await()

                    val pollResult = authSession.pollingWaitForResult().await()

                    if (pollResult.accountName.isEmpty() && pollResult.refreshToken.isEmpty()) {
                        throw Exception("No account name or refresh token received.")
                    }

                    login(
                        clientId = authSession.clientID,
                        username = pollResult.accountName,
                        accessToken = pollResult.accessToken,
                        refreshToken = pollResult.refreshToken,
                        rememberSession = rememberSession,
                    )
                } ?: run {
                    Timber.e("Could not logon: Failed to connect to Steam")

                    val event = SteamEvent.LogonEnded(username, LoginResult.Failed, "No connection to Steam")
                    PluviaApp.events.emit(event)
                }
            } catch (e: Exception) {
                Timber.e(e, "Login failed")

                val message = when (e) {
                    is CancellationException -> "Unknown cancellation"
                    is AuthenticationException -> e.result?.name ?: e.message
                    else -> e.message ?: e.javaClass.name
                }

                val event = SteamEvent.LogonEnded(username, LoginResult.Failed, message)
                PluviaApp.events.emit(event)
            }
        }

        suspend fun startLoginWithQr() = withContext(Dispatchers.IO) {
            try {
                Timber.i("Logging in via QR.")

                instance!!.steamClient?.let { steamClient ->
                    isWaitingForQRAuth = true

                    val authDetails = AuthSessionDetails().apply {
                        deviceFriendlyName = SteamUtils.getMachineName(instance!!)
                    }

                    val authSession = steamClient.authentication.beginAuthSessionViaQR(authDetails).await()

                    // Steam will periodically refresh the challenge url, this callback allows you to draw a new qr code.
                    authSession.challengeUrlChanged = instance

                    val qrEvent = SteamEvent.QrChallengeReceived(authSession.challengeUrl)
                    PluviaApp.events.emit(qrEvent)

                    Timber.d("PollingInterval: ${authSession.pollingInterval.toLong()}")

                    var authPollResult: AuthPollResult? = null

                    while (isWaitingForQRAuth && authPollResult == null) {
                        try {
                            authPollResult = authSession.pollAuthSessionStatus().await()
                        } catch (e: Exception) {
                            Timber.e(e, "Poll auth session status error")
                            throw e
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

                    val event = SteamEvent.QrAuthEnded(authPollResult != null)
                    PluviaApp.events.emit(event)

                    // there is a chance qr got cancelled and there is no authPollResult
                    if (authPollResult == null) {
                        Timber.e("Got no auth poll result")
                        throw Exception("Got no auth poll result")
                    }

                    login(
                        clientId = authSession.clientID,
                        username = authPollResult.accountName,
                        accessToken = authPollResult.accessToken,
                        refreshToken = authPollResult.refreshToken,
                    )
                } ?: run {
                    Timber.e("Could not start QR logon: Failed to connect to Steam")

                    val event = SteamEvent.QrAuthEnded(success = false, message = "No connection to Steam")
                    PluviaApp.events.emit(event)
                }
            } catch (e: Exception) {
                Timber.e(e, "QR failed")

                val message = when (e) {
                    is CancellationException -> "QR Session timed out"
                    is AuthenticationException -> e.result?.name ?: e.message
                    else -> e.message ?: e.javaClass.name
                }

                val event = SteamEvent.QrAuthEnded(success = false, message = message)
                PluviaApp.events.emit(event)
            }
        }

        fun stopLoginWithQr() {
            Timber.i("Stopping QR polling")

            isWaitingForQRAuth = false
        }

        fun stop() {
            instance?.let { steamInstance ->
                steamInstance.scope.launch {
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

            clearDatabase()
        }

        fun clearDatabase() {
            with(instance!!) {
                scope.launch {
                    db.withTransaction {
                        db.emoticonDao().deleteAll()
                        db.friendMessagesDao().deleteAllMessages()
                        appDao.deleteAll()
                        changeNumbersDao.deleteAll()
                        fileChangeListsDao.deleteAll()
                        friendDao.deleteAll()
                        licenseDao.deleteAll()
                    }
                }
            }
        }

        private fun performLogOffDuties() {
            val username = PrefManager.username

            clearUserData()

            val event = SteamEvent.LoggedOut(username)
            PluviaApp.events.emit(event)
        }

        suspend fun getEmoticonList() = withContext(Dispatchers.IO) {
            instance?.steamClient!!.getHandler<PluviaHandler>()!!.getEmoticonList()
        }

        suspend fun fetchEmoticons(): List<Emoticon> = withContext(Dispatchers.IO) {
            instance?.emoticonDao!!.getAllAsList()
        }

        suspend fun getProfileInfo(friendID: SteamID): ProfileInfoCallback = withContext(Dispatchers.IO) {
            instance?._steamFriends!!.requestProfileInfo(friendID).await()
        }

        suspend fun getOwnedGames(friendID: Long): List<OwnedGames> = withContext(Dispatchers.IO) {
            instance?._unifiedFriends!!.getOwnedGames(friendID)
        }

        suspend fun getRecentMessages(friendID: Long) = withContext(Dispatchers.IO) {
            instance?._unifiedFriends!!.getRecentMessages(friendID)
        }

        suspend fun ackMessage(friendID: Long) = withContext(Dispatchers.IO) {
            instance?._unifiedFriends!!.ackMessage(friendID)
        }

        suspend fun requestAliasHistory(friendID: Long) = withContext(Dispatchers.IO) {
            instance?.steamClient!!.getHandler<SteamFriends>()?.requestAliasHistory(SteamID(friendID))
        }

        suspend fun sendTypingMessage(friendID: Long) = withContext(Dispatchers.IO) {
            instance?._unifiedFriends!!.setIsTyping(friendID)
        }

        suspend fun sendMessage(friendID: Long, message: String) = withContext(Dispatchers.IO) {
            instance?._unifiedFriends!!.sendMessage(friendID, message)
        }

        suspend fun blockFriend(friendID: Long) = withContext(Dispatchers.IO) {
            val friend = SteamID(friendID)
            val result = instance?._steamFriends!!.ignoreFriend(friend).await()

            if (result.result == EResult.OK) {
                val blockedFriend = instance!!.friendDao.findFriend(friendID)
                blockedFriend?.let {
                    instance?.friendDao!!.update(it.copy(relation = EFriendRelationship.Blocked))
                }
            }
        }

        suspend fun removeFriend(friendID: Long) = withContext(Dispatchers.IO) {
            val friend = SteamID(friendID)
            instance?._steamFriends!!.removeFriend(friend)
            instance?.friendDao!!.remove(friendID)
        }

        suspend fun setNickName(friendID: Long, value: String) = withContext(Dispatchers.IO) {
            val friend = SteamID(friendID)
            instance?._steamFriends!!.setFriendNickname(friend, value)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        PluviaApp.events.on<AndroidEvent.EndProcess, Unit>(onEndProcess)

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
                Timber.d("Exiting app via notification intent")

                val event = AndroidEvent.EndProcess
                PluviaApp.events.emit(event)

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
            steamClient = SteamClient(configuration).apply {
                addHandler(PluviaHandler())

                // remove callbacks we're not using.
                removeHandler(SteamGameServer::class.java)
                removeHandler(SteamMasterServer::class.java)
                removeHandler(SteamWorkshop::class.java)
                removeHandler(SteamScreenshots::class.java)
                removeHandler(SteamUserStats::class.java)
            }

            // create the callback manager which will route callbacks to function calls
            callbackManager = CallbackManager(steamClient!!)

            // get the different handlers to be used throughout the service
            _steamUser = steamClient!!.getHandler(SteamUser::class.java)
            _steamApps = steamClient!!.getHandler(SteamApps::class.java)
            _steamFriends = steamClient!!.getHandler(SteamFriends::class.java)
            _steamCloud = steamClient!!.getHandler(SteamCloud::class.java)

            _unifiedFriends = SteamUnifiedFriends(this)
            _steamFamilyGroups = steamClient!!.getHandler<SteamUnifiedMessages>()!!.createService<FamilyGroups>()

            // subscribe to the callbacks we are interested in
            with(callbackSubscriptions) {
                with(callbackManager!!) {
                    add(subscribe(ConnectedCallback::class.java, ::onConnected))
                    add(subscribe(DisconnectedCallback::class.java, ::onDisconnected))
                    add(subscribe(LoggedOnCallback::class.java, ::onLoggedOn))
                    add(subscribe(LoggedOffCallback::class.java, ::onLoggedOff))
                    add(subscribe(PersonaStateCallback::class.java, ::onPersonaStateReceived))
                    add(subscribe(LicenseListCallback::class.java, ::onLicenseList))
                    add(subscribe(NicknameListCallback::class.java, ::onNicknameList))
                    add(subscribe(FriendsListCallback::class.java, ::onFriendsList))
                    add(subscribe(EmoticonListCallback::class.java, ::onEmoticonList))
                    add(subscribe(AliasHistoryCallback::class.java, ::onAliasHistory))
                    add(subscribe(PICSChangesCallback::class.java, ::onPicsChanges))
                    add(subscribe(PICSProductInfoCallback::class.java, ::onPicsProduct))
                }
            }

            isRunning = true

            // we should use Dispatchers.IO here since we are running a sleeping/blocking function
            // "The idea is that the IO dispatcher spends a lot of time waiting (IO blocked),
            // while the Default dispatcher is intended for CPU intensive tasks, where there
            // is little or no sleep."
            // source: https://stackoverflow.com/a/59040920
            scope.launch {
                while (isRunning) {
                    // logD("runWaitCallbacks")

                    try {
                        callbackManager!!.runWaitCallbacks(1000L)
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

        scope.launch { stop() }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun connectToSteam() {
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
        isLoggingOut = false
        isWaitingForQRAuth = false

        PrefManager.appInstallPath = defaultAppInstallPath
        PrefManager.appStagingPath = defaultAppStagingPath

        steamClient = null
        _steamUser = null
        _steamApps = null
        _steamFriends = null
        _steamCloud = null

        callbackSubscriptions.forEach { it.close() }
        callbackSubscriptions.clear()
        callbackManager = null

        _unifiedFriends?.close()
        _unifiedFriends = null

        isStopping = false
        retryAttempt = 0

        PluviaApp.events.off<AndroidEvent.EndProcess, Unit>(onEndProcess)
        PluviaApp.events.clearAllListenersOf<SteamEvent<Any>>()
    }

    private fun reconnect() {
        notificationHelper.notify("Retrying...")

        isConnected = false

        val event = SteamEvent.Disconnected
        PluviaApp.events.emit(event)

        steamClient!!.disconnect()
    }

    // region [REGION] callbacks
    @Suppress("UNUSED_PARAMETER", "unused")
    private fun onConnected(callback: ConnectedCallback) {
        Timber.i("Connected to Steam")

        retryAttempt = 0
        isConnected = true

        var isAutoLoggingIn = false

        if (PrefManager.username.isNotEmpty() && PrefManager.refreshToken.isNotEmpty()) {
            isAutoLoggingIn = true

            login(
                username = PrefManager.username,
                refreshToken = PrefManager.refreshToken,
                rememberSession = true,
            )
        }

        val event = SteamEvent.Connected(isAutoLoggingIn)
        PluviaApp.events.emit(event)
    }

    private fun onDisconnected(callback: DisconnectedCallback) {
        Timber.i("Disconnected from Steam. User initiated: ${callback.isUserInitiated}")

        isConnected = false

        if (!isStopping && retryAttempt < MAX_RETRY_ATTEMPTS) {
            retryAttempt++

            Timber.w("Attempting to reconnect (retry $retryAttempt)")

            // isLoggingOut = false
            val event = SteamEvent.RemotelyDisconnected
            PluviaApp.events.emit(event)

            connectToSteam()
        } else {
            val event = SteamEvent.Disconnected
            PluviaApp.events.emit(event)

            clearValues()

            stopSelf()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
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

                // retrieve persona data of logged in user
                scope.launch { requestUserPersona() }

                // Request family share info if we have a familyGroupId.
                if (callback.familyGroupId != 0L) {
                    scope.launch {
                        val request = SteammessagesFamilygroupsSteamclient.CFamilyGroups_GetFamilyGroup_Request.newBuilder().apply {
                            familyGroupid = callback.familyGroupId
                        }.build()

                        _steamFamilyGroups!!.getFamilyGroup(request).await().let {
                            if (it.result != EResult.OK) {
                                Timber.w("An error occurred loading family group info.")
                                return@launch
                            }

                            val response = it.body

                            Timber.i("Found family share: ${response.name}, with ${response.membersCount} members.")

                            response.membersList.forEach { member ->
                                val accountID = SteamID(member.steamid).accountID.toInt()
                                familyGroupMembers.add(accountID)
                            }
                        }
                    }
                }

                // continuously check for pics changes
                continuousPICSChangesChecker()

                // request app pics data when needed
                bufferedPICSGetProductInfo()

                // continuously check for game names that friends are playing.
                continuousFriendChecker()

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

        val event = SteamEvent.LogonEnded(PrefManager.username, _loginResult)
        PluviaApp.events.emit(event)
    }

    private fun onLoggedOff(callback: LoggedOffCallback) {
        Timber.i("Logged off of Steam: ${callback.result}")

        notificationHelper.notify("Disconnected...")

        if (isLoggingOut || callback.result == EResult.LogonSessionReplaced) {
            performLogOffDuties()

            scope.launch { stop() }
        } else if (callback.result == EResult.LoggedInElsewhere) {
            // received when a client runs an app and wants to forcibly close another
            // client running an app
            val event = SteamEvent.ForceCloseApp
            PluviaApp.events.emit(event)

            reconnect()
        } else {
            reconnect()
        }
    }

    private fun onNicknameList(callback: NicknameListCallback) {
        Timber.d("Nickname list called: ${callback.nicknames.size}")
        scope.launch {
            db.withTransaction {
                friendDao.clearAllNicknames()
                friendDao.updateNicknames(callback.nicknames)
            }
        }
    }

    private fun onFriendsList(callback: FriendsListCallback) {
        Timber.d("onFriendsList ${callback.friendList.size}")
        scope.launch {
            db.withTransaction {
                callback.friendList
                    .filter { it.steamID.isIndividualAccount }
                    .forEach { filteredFriend ->
                        val friendId = filteredFriend.steamID.convertToUInt64()
                        val friend = friendDao.findFriend(friendId)

                        if (friend == null) {
                            // Not in the DB, create them.
                            val friendToAdd = SteamFriend(
                                id = filteredFriend.steamID.convertToUInt64(),
                                relation = filteredFriend.relationship,
                            )

                            friendDao.insert(friendToAdd)
                        } else {
                            // In the DB, update them.
                            val dbFriend = friend.copy(relation = filteredFriend.relationship)
                            friendDao.update(dbFriend)
                        }
                    }

                // Add logged in account if we don't exist yet.
                val selfId = userSteamId!!.convertToUInt64()
                val self = friendDao.findFriend(selfId)

                if (self == null) {
                    val sid = SteamFriend(id = selfId)
                    friendDao.insert(sid)
                }
            }

            // NOTE: Our UI could load too quickly on fresh database, our icon will be "?"
            //  unless relaunched or we nav to a new screen.
            _unifiedFriends?.refreshPersonaStates()
        }
    }

    private fun onEmoticonList(callback: EmoticonListCallback) {
        Timber.i("Getting emotes and stickers, size: ${callback.emoteList.size}")
        scope.launch {
            db.withTransaction {
                emoticonDao.replaceAll(callback.emoteList)
            }
        }
    }

    private fun onAliasHistory(callback: AliasHistoryCallback) {
        val names = callback.responses.flatMap { map -> map.names }.map { map -> map.name }
        val event = SteamEvent.OnAliasHistory(names)
        PluviaApp.events.emit(event)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun onPersonaStateReceived(callback: PersonaStateCallback) {
        // Ignore accounts that arent individuals
        if (!callback.friendID.isIndividualAccount) {
            return
        }

        // Ignore states where the name is blank.
        if (callback.name.isEmpty()) {
            return
        }

        // Timber.d("Persona state received: ${callback.name}")

        scope.launch {
            db.withTransaction {
                val id = callback.friendID.convertToUInt64()
                val friend = friendDao.findFriend(id)

                if (friend == null) {
                    Timber.w("onPersonaStateReceived: failed to find friend to update: $id")
                    return@withTransaction
                }

                friendDao.update(
                    friend.copy(
                        statusFlags = callback.statusFlags,
                        state = callback.state,
                        stateFlags = callback.stateFlags,
                        gameAppID = callback.gameAppID,
                        gameID = callback.gameID,
                        gameName = appDao.findApp(callback.gameAppID)?.name ?: callback.gameName,
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

                // Send off an event if we change states.
                if (callback.friendID == steamClient!!.steamID) {
                    friendDao.findFriend(id)?.let { account ->
                        val event = SteamEvent.PersonaStateReceived(account)
                        PluviaApp.events.emit(event)
                    }
                }
            }
        }
    }

    private fun onLicenseList(callback: LicenseListCallback) {
        if (callback.result != EResult.OK) {
            Timber.w("Failed to get License list")
            return
        }

        Timber.i("Received License List ${callback.result}, size: ${callback.licenseList.size}")

        scope.launch {
            // Note: I assume with every launch we do, in fact, update the licenses for app the apps if we join or get removed
            //      from family sharing... We really can't test this as there is a 1-year cooldown.
            //      Then 'findStaleLicences' will find these now invalid items to remove.
            val licensesToAdd = callback.licenseList
                .groupBy { it.packageID }
                .map { licensesEntry ->
                    val preferred = licensesEntry.value.firstOrNull {
                        it.ownerAccountID == userSteamId?.accountID?.toInt()
                    } ?: licensesEntry.value.first()
                    SteamLicense(
                        packageId = licensesEntry.key,
                        lastChangeNumber = preferred.lastChangeNumber,
                        timeCreated = preferred.timeCreated,
                        timeNextProcess = preferred.timeNextProcess,
                        minuteLimit = preferred.minuteLimit,
                        minutesUsed = preferred.minutesUsed,
                        paymentMethod = preferred.paymentMethod,
                        licenseFlags = licensesEntry.value
                            .map { it.licenseFlags }
                            .reduceOrNull { first, second ->
                                val combined = EnumSet.copyOf(first)
                                combined.addAll(second)
                                combined
                            } ?: EnumSet.noneOf(ELicenseFlags::class.java),
                        purchaseCode = preferred.purchaseCode,
                        licenseType = preferred.licenseType,
                        territoryCode = preferred.territoryCode,
                        accessToken = preferred.accessToken,
                        ownerAccountId = licensesEntry.value.map { it.ownerAccountID }, // Read note above
                        masterPackageID = preferred.masterPackageID,
                    )
                }
            if (licensesToAdd.isNotEmpty()) {
                Timber.i("Adding ${licensesToAdd.size} licenses")
                db.withTransaction {
                    licenseDao.insert(licensesToAdd)
                }
            }

            val licensesToRemove = licenseDao.findStaleLicences(callback.licenseList.map { it.packageID })
            if (licensesToRemove.isNotEmpty()) {
                Timber.i("Removing ${licensesToRemove.size} (stale) licenses")
                db.withTransaction {
                    val packageIds = licensesToRemove.map { it.packageId }
                    licenseDao.deleteStaleLicenses(packageIds)
                }
            }

            // Get PICS information with the current license database.
            val picsRequests = licenseDao.getAllLicenses().map { PICSRequest(it.packageId, it.accessToken) }
            _steamApps!!.picsGetProductInfo(apps = emptyList(), packages = picsRequests)
        }
    }

    private fun onPicsChanges(callback: PICSChangesCallback) {
        if (PrefManager.lastPICSChangeNumber == callback.currentChangeNumber) {
            Timber.w("Change number was the same as last change number, skipping")
            return
        }

        PrefManager.lastPICSChangeNumber = callback.currentChangeNumber

        Timber.d(
            """
                lastChangeNumber: ${callback.lastChangeNumber}
                currentChangeNumber: ${callback.currentChangeNumber}
                isRequiresFullUpdate: ${callback.isRequiresFullUpdate}
                isRequiresFullAppUpdate: ${callback.isRequiresFullAppUpdate}
                isRequiresFullPackageUpdate: ${callback.isRequiresFullPackageUpdate}
                appChangesCount: ${callback.appChanges.size}
                pkgChangesCount: ${callback.packageChanges.size}
            """.trimIndent(),
        )

        scope.launch {
            callback.appChanges.values
                .filter { changeData ->
                    // only queue PICS requests for apps existing in the db that have changed
                    val app = appDao.findApp(changeData.id) ?: return@filter false
                    changeData.changeNumber != app.lastChangeNumber
                }
                .map { it.id }
                .also { Timber.d("onPicsChanges: Queueing ${it.size} app requests") }
                .also(::queueAppPICSRequests)

            val pkgsWithChanges = callback.packageChanges.values
                .filter { changeData ->
                    // only queue PICS requests for pkgs existing in the db that have changed
                    val pkg = licenseDao.findLicense(changeData.id) ?: return@filter false
                    changeData.changeNumber != pkg.lastChangeNumber
                }
            val pkgsForAccessTokens = pkgsWithChanges.filter { it.isNeedsToken }.map { it.id }
            val accessTokens = _steamApps?.picsGetAccessTokens(emptyList(), pkgsForAccessTokens)?.await()?.packageTokens ?: emptyMap()
            val picsRequest = pkgsWithChanges.map { PICSRequest(it.id, accessTokens[it.id] ?: 0) }
            Timber.d("onPicsChanges: Queueing ${picsRequest.size} package requests")
            _steamApps!!.picsGetProductInfo(apps = emptyList(), packages = picsRequest)
        }
    }

    private fun onPicsProduct(callback: PICSProductInfoCallback) {
        if (callback.packages.isNotEmpty()) {
            Timber.i("onPicsProduct: Received PICS of ${callback.packages.size} package(s)")

            scope.launch {
                // Don't race the queue.
                val queue = Collections.synchronizedList(mutableListOf<Int>())

                db.withTransaction {
                    callback.packages.values.forEach { pkg ->
                        val appIds = pkg.keyValues["appids"].children.map { it.asInteger() }
                        licenseDao.updateApps(pkg.id, appIds)

                        val depotIds = pkg.keyValues["depotids"].children.map { it.asInteger() }
                        licenseDao.updateDepots(pkg.id, depotIds)

                        // Insert a stub row (or update) of SteamApps to the database.
                        appIds.forEach { appid ->
                            val steamApp = appDao.findApp(appid)?.copy(packageId = pkg.id)
                            if (steamApp != null) {
                                appDao.update(steamApp)
                            } else {
                                val stubSteamApp = SteamApp(id = appid, packageId = pkg.id)
                                appDao.insert(stubSteamApp)
                            }
                        }

                        queue.addAll(appIds)
                    }
                }

                // Get PICS information with the app ids.
                queueAppPICSRequests(queue)
            }
        }

        if (callback.apps.isNotEmpty()) {
            Timber.i("onPicsProduct: Received PICS of ${callback.apps.size} apps(s)")

            scope.launch {
                val steamApps = callback.apps.values.mapNotNull { app ->
                    val appFromDb = appDao.findApp(app.id)
                    val packageId = appFromDb?.packageId ?: INVALID_PKG_ID
                    val packageFromDb = if (packageId != INVALID_PKG_ID) licenseDao.findLicense(packageId) else null
                    val ownerAccountId = packageFromDb?.ownerAccountId ?: emptyList()

                    // Apps with -1 for the ownerAccountId should be added.
                    //  This can help with friend game names.

                    // TODO maybe apps with -1 for the ownerAccountId can be stripped with necessities and name.

                    if (app.changeNumber != appFromDb?.lastChangeNumber) {
                        app.keyValues.generateSteamApp().copy(
                            packageId = packageId,
                            ownerAccountId = ownerAccountId,
                            receivedPICS = true,
                            lastChangeNumber = app.changeNumber,
                            licenseFlags = packageFromDb?.licenseFlags ?: EnumSet.noneOf(ELicenseFlags::class.java),
                        )
                    } else {
                        null
                    }
                }

                if (steamApps.isNotEmpty()) {
                    Timber.d("Inserting ${steamApps.size} PICS apps to database")
                    db.withTransaction {
                        appDao.insert(steamApps)
                    }
                }
            }
        }
    }

    override fun onChanged(qrAuthSession: QrAuthSession?) {
        qrAuthSession?.let { qr ->
            if (!BuildConfig.DEBUG) {
                Timber.d("QR code changed -> ${qr.challengeUrl}")
            }

            val event = SteamEvent.QrChallengeReceived(qr.challengeUrl)
            PluviaApp.events.emit(event)
        } ?: run { Timber.w("QR challenge url was null") }
    }
    // endregion

    /**
     * Request changes for apps and packages since a given change number.
     * Checks every [PICS_CHANGE_CHECK_DELAY] seconds.
     * Results are returned in a [PICSChangesCallback]
     */
    private fun continuousPICSChangesChecker() = scope.launch {
        while (isActive && isLoggedIn) {
            _steamApps?.picsGetChangesSince(
                lastChangeNumber = PrefManager.lastPICSChangeNumber,
                sendAppChangeList = true,
                sendPackageChangelist = true,
            )

            delay(PICS_CHANGE_CHECK_DELAY)
        }
    }

    /**
     * Continuously check for friends playing games and query for pics if its a game we don't have in the database.
     */
    private fun continuousFriendChecker() = scope.launch {
        while (isActive && isLoggedIn) {
            // Initial delay before each check
            delay(20.seconds)

            friendDao.findFriendsInGame()
                .also { friends -> Timber.d("Found ${friends.size} friends in game") }
                .forEach { friend ->
                    appDao.findApp(friend.gameAppID)?.let { app ->
                        if (friend.gameName != app.name) {
                            Timber.d("Updating ${friend.name} with game ${app.name}")
                            friendDao.update(friend.copy(gameName = app.name))
                        }
                    } ?: queueAppPICSRequests(listOf(friend.gameAppID)) // Didn't find the app, we'll get it next time.
                }
        }
    }

    /**
     * A buffered flow to parse so many PICS requests in a given moment.
     */
    private fun bufferedPICSGetProductInfo() = scope.launch {
        appIdFlowReceiver
            .timeChunked(MAX_SIMULTANEOUS_PICS_REQUESTS)
            .buffer(Channel.RENDEZVOUS)
            .collect { appIds ->
                Timber.d("Collected ${appIds.size} app(s) to query PICS")
                _steamApps?.picsGetProductInfo(
                    apps = appIds.map { PICSRequest(id = it) },
                    packages = emptyList(),
                )
            }
    }
}
