package com.OxGames.Pluvia.ui

import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.OxGames.Pluvia.BuildConfig
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.data.GameProcessInfo
import com.OxGames.Pluvia.enums.LoginResult
import com.OxGames.Pluvia.enums.PathType
import com.OxGames.Pluvia.enums.SaveLocation
import com.OxGames.Pluvia.enums.SyncResult
import com.OxGames.Pluvia.events.AndroidEvent
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.ui.component.dialog.LoadingDialog
import com.OxGames.Pluvia.ui.component.dialog.MessageDialog
import com.OxGames.Pluvia.ui.component.dialog.state.MessageDialogState
import com.OxGames.Pluvia.ui.enums.Orientation
import com.OxGames.Pluvia.ui.enums.PluviaScreen
import com.OxGames.Pluvia.ui.model.HomeViewModel
import com.OxGames.Pluvia.ui.model.UserLoginViewModel
import com.OxGames.Pluvia.ui.screen.HomeScreen
import com.OxGames.Pluvia.ui.screen.login.UserLoginScreen
import com.OxGames.Pluvia.ui.screen.settings.SettingsScreen
import com.OxGames.Pluvia.ui.screen.xserver.XServerScreen
import com.OxGames.Pluvia.ui.theme.PluviaTheme
import com.winlator.container.ContainerManager
import com.winlator.core.WineInfo
import com.winlator.xenvironment.ImageFsInstaller
import com.winlator.xserver.Window
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientObjects.ECloudPendingRemoteOperation
import `in`.dragonbra.javasteam.steam.handlers.steamapps.AppProcessInfo
import java.nio.file.Paths
import java.util.Date
import java.util.EnumSet
import kotlin.io.path.name
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun PluviaMain(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current

    var resettedScreen by rememberSaveable { mutableStateOf<PluviaScreen?>(null) }
    var currentScreen by rememberSaveable {
        mutableStateOf(
            PluviaScreen.valueOf(
                resettedScreen?.name ?: PluviaScreen.LoginUser.name,
            ),
        )
    }
    var hasLaunched by rememberSaveable { mutableStateOf(false) }
    var loadingDialogVisible by remember { mutableStateOf(false) }
    var loadingProgress by remember { mutableFloatStateOf(0f) }
    var msgDialogState by remember { mutableStateOf(MessageDialogState(false)) }
    var annoyingDialogShown by remember { mutableStateOf(false) }

    LaunchedEffect(lifecycleOwner) {
        if (!hasLaunched) {
            hasLaunched = true
            // Log.d("PluviaMain", "Creating on destination changed listener")
            PluviaApp.onDestinationChangedListener =
                NavController.OnDestinationChangedListener { controller, destination, arguments ->
                    // Log.d("PluviaMain", "onDestinationChanged to ${destination.route}")
                    // in order not to trigger the screen changed launch effect
                    currentScreen =
                        PluviaScreen.valueOf(destination.route ?: PluviaScreen.LoginUser.name)
                }
            // Log.d("PluviaMain", "Starting orientator")
            PluviaApp.events.emit(AndroidEvent.StartOrientator)
        } else {
            // Log.d("PluviaMain", "Removing on destination changed listener")
            navController.removeOnDestinationChangedListener(PluviaApp.onDestinationChangedListener!!)
        }
        // Log.d("PluviaMain", "Adding on destination changed listener")
        navController.addOnDestinationChangedListener(PluviaApp.onDestinationChangedListener!!)
    }

    var topAppBarVisible by rememberSaveable { mutableStateOf(true) }

    var isSteamConnected by remember { mutableStateOf(SteamService.isConnected) }
    var appId by rememberSaveable { mutableIntStateOf(SteamService.INVALID_APP_ID) }
    var bootToContainer by rememberSaveable { mutableStateOf(false) }

    var hasBack by rememberSaveable { mutableStateOf(navController.previousBackStackEntry?.destination?.route != null) }
    LaunchedEffect(currentScreen) {
        // do the following each time we navigate to a new screen
        if (resettedScreen != currentScreen) {
            resettedScreen = currentScreen
            // Log.d("PluviaMain", "Screen changed to $currentScreen, resetting some values")
            // reset top app bar visibility
            topAppBarVisible = true
            // reset system ui visibility
            PluviaApp.events.emit(AndroidEvent.SetSystemUIVisibility(true))
            // TODO: add option for user to set
            // reset available orientations
            PluviaApp.events.emit(AndroidEvent.SetAllowedOrientation(EnumSet.of(Orientation.UNSPECIFIED)))
            // find out if back is available
            hasBack = navController.previousBackStackEntry?.destination?.route != null
        }
    }

    DisposableEffect(lifecycleOwner) {
        val onHideAppBar: (AndroidEvent.SetAppBarVisibility) -> Unit = {
            topAppBarVisible = it.visible
        }
        val onBackPressed: (AndroidEvent.BackPressed) -> Unit = {
            if (hasBack) {
                // TODO: check if back leads to log out and present confidence modal
                navController.popBackStack()
            } else {
                // TODO: quit app?
            }
        }
        val onSteamConnected: (SteamEvent.Connected) -> Unit = {
            Log.d("PluviaMain", "Received is connected")
            isSteamConnected = true
        }
        val onSteamDisconnected: (SteamEvent.Disconnected) -> Unit = {
            Log.d("PluviaMain", "Received disconnected from Steam")
            isSteamConnected = false
        }
        val onLoggingIn: (SteamEvent.LogonStarted) -> Unit = {
            Log.d("PluviaMain", "Received logon started")
        }
        val onLogonEnded: (SteamEvent.LogonEnded) -> Unit = {
            CoroutineScope(Dispatchers.Main).launch {
                Log.d("PluviaMain", "Received logon ended")

                when (it.loginResult) {
                    LoginResult.Success -> {
                        // TODO: add preference for first screen on login
                        Log.d("PluviaMain", "Navigating to library")
                        navController.navigate(PluviaScreen.Home.name)
                        if (!BuildConfig.GOLD && !annoyingDialogShown) {
                            annoyingDialogShown = true
                            msgDialogState = MessageDialogState(
                                visible = true,
                                message = "Thank you for using Pluvia, please consider supporting us by purchasing the app from the store",
                                confirmBtnText = "OK",
                                onConfirmClick = {
                                    msgDialogState = MessageDialogState(visible = false)
                                },
                                onDismissRequest = {
                                    msgDialogState = MessageDialogState(visible = false)
                                },
                            )
                        }
                    }

                    else -> {
                        Log.d("PluviaMain", "Received non-result: ${it.loginResult}")
                    }
                }
            }
        }
        val onLoggedOut: (SteamEvent.LoggedOut) -> Unit = {
            CoroutineScope(Dispatchers.Main).launch {
                Log.d("PluviaMain", "Received logged out")

                // Pop stack and go back to login.
                navController.popBackStack(
                    route = PluviaScreen.LoginUser.name,
                    inclusive = false,
                    saveState = false,
                )
            }
        }

        PluviaApp.events.on<AndroidEvent.SetAppBarVisibility, Unit>(onHideAppBar)
        PluviaApp.events.on<AndroidEvent.BackPressed, Unit>(onBackPressed)
        PluviaApp.events.on<SteamEvent.Connected, Unit>(onSteamConnected)
        PluviaApp.events.on<SteamEvent.Disconnected, Unit>(onSteamDisconnected)
        PluviaApp.events.on<SteamEvent.LogonStarted, Unit>(onLoggingIn)
        PluviaApp.events.on<SteamEvent.LogonEnded, Unit>(onLogonEnded)
        PluviaApp.events.on<SteamEvent.LoggedOut, Unit>(onLoggedOut)

        if (!isSteamConnected) {
            val intent = Intent(context, SteamService::class.java)
            context.startForegroundService(intent)
        }

        // Go to the Home screen if we're already logged in.
        if (SteamService.isLoggedIn) {
            navController.navigate(PluviaScreen.Home.name)
        }

        onDispose {
            PluviaApp.events.off<AndroidEvent.SetAppBarVisibility, Unit>(onHideAppBar)
            PluviaApp.events.off<AndroidEvent.BackPressed, Unit>(onBackPressed)
            PluviaApp.events.off<SteamEvent.Connected, Unit>(onSteamConnected)
            PluviaApp.events.off<SteamEvent.Disconnected, Unit>(onSteamDisconnected)
            // PluviaApp.events.off<SteamEvent.LogonStarte, Unitd>(onLoggingIn)
            PluviaApp.events.off<SteamEvent.LogonEnded, Unit>(onLogonEnded)
            PluviaApp.events.off<SteamEvent.LoggedOut, Unit>(onLoggedOut)
        }
    }

    PluviaTheme {
        LoadingDialog(
            visible = loadingDialogVisible,
            progress = loadingProgress,
        )
        MessageDialog(
            visible = msgDialogState.visible,
            onDismissRequest = msgDialogState.onDismissRequest,
            onConfirmClick = msgDialogState.onConfirmClick,
            confirmBtnText = msgDialogState.confirmBtnText,
            onDismissClick = msgDialogState.onDismissClick,
            dismissBtnText = msgDialogState.dismissBtnText,
            icon = msgDialogState.icon,
            title = msgDialogState.title,
            message = msgDialogState.message,
        )
        NavHost(
            navController = navController,
            startDestination = PluviaScreen.LoginUser.name,
            modifier = Modifier
                .fillMaxSize(),
        ) {
            /** Login **/
            composable(route = PluviaScreen.LoginUser.name) {
                val viewModel: UserLoginViewModel = viewModel()
                UserLoginScreen(
                    viewModel = viewModel,
                )
            }
            /** Library, Downloads, Friends **/
            composable(
                route = PluviaScreen.Home.name,
                deepLinks = listOf(navDeepLink { uriPattern = "pluvia://home" }),
            ) {
                val viewModel: HomeViewModel = viewModel()
                HomeScreen(
                    viewModel = viewModel,
                    onClickPlay = { launchAppId, asContainer ->
                        appId = launchAppId
                        bootToContainer = asContainer
                        launchApp(
                            context = context,
                            appId = appId,
                            setLoadingDialogVisible = { loadingDialogVisible = it },
                            setLoadingProgress = { loadingProgress = it },
                            setMessageDialogState = { msgDialogState = it },
                            onSuccess = {
                                // SteamUtils.replaceSteamApi(context, appId)

                                CoroutineScope(Dispatchers.Main).launch {
                                    navController.navigate(PluviaScreen.XServer.name)
                                }
                            },
                        )
                    },
                    onSettings = {
                        navController.navigate(PluviaScreen.Settings.name)
                    },
                )
            }

            /** Full Screen Chat **/

            /** Game Screen **/
            composable(route = PluviaScreen.XServer.name) {
                XServerScreen(
                    appId = appId,
                    bootToContainer = bootToContainer,
                    navigateBack = {
                        CoroutineScope(Dispatchers.Main).launch {
                            navController.popBackStack()
                        }
                    },
                    onWindowMapped = { window ->
                        SteamService.getAppInfoOf(appId)?.let { appInfo ->
                            // Log.d("PluviaMain", "${appInfo.name} == ${window.name} || ${window.className}")
                            // TODO: this should not be a search, the app should have been launched with a specific launch config that we then use to compare
                            val launchConfig = SteamService.getWindowsLaunchInfos(appId).firstOrNull {
                                val gameExe = Paths.get(it.executable.replace('\\', '/')).name.lowercase()
                                val windowExe = window.className.lowercase()
                                // Log.d("PluviaMain", "Is $gameExe == $windowExe")
                                gameExe == windowExe
                            }
                            if (launchConfig != null) {
                                val steamProcessId = Process.myPid()
                                val processes = mutableListOf<AppProcessInfo>()
                                var currentWindow: Window = window
                                do {
                                    var parentWindow: Window? = window.parent
                                    // Log.d("PluviaMain", "${currentWindow.name}:${currentWindow.className} -> ${parentWindow?.name}:${parentWindow?.className}")
                                    val process = if (
                                        parentWindow != null &&
                                        parentWindow.className.lowercase() != "explorer.exe"
                                    ) {
                                        val processId = currentWindow.processId
                                        val parentProcessId = parentWindow.processId
                                        currentWindow = parentWindow
                                        AppProcessInfo(
                                            processId,
                                            parentProcessId,
                                            false,
                                        )
                                    } else {
                                        parentWindow = null
                                        AppProcessInfo(
                                            currentWindow.processId,
                                            steamProcessId,
                                            true,
                                        )
                                    }
                                    processes.add(process)
                                } while (parentWindow != null)

                                SteamService.notifyRunningProcesses(
                                    GameProcessInfo(
                                        appId = appId,
                                        processes = processes,
                                    ),
                                )
                            }
                        }
                    },
                    onExit = {
                        CoroutineScope(Dispatchers.IO).launch {
                            SteamService.notifyRunningProcesses()
                            SteamService.closeApp(appId, this) { prefix ->
                                PathType.from(prefix).toAbsPath(context, appId)
                            }.await()
                        }
                    },
                )
            }

            /** Settings **/
            composable(route = PluviaScreen.Settings.name) {
                SettingsScreen(
                    onBack = {
                        navController.navigateUp()
                    },
                )
            }
        }
    }
}

fun launchApp(
    context: Context,
    appId: Int,
    ignorePendingOperations: Boolean = false,
    preferredSave: SaveLocation = SaveLocation.None,
    setLoadingDialogVisible: (Boolean) -> Unit,
    setLoadingProgress: (Float) -> Unit,
    setMessageDialogState: (MessageDialogState) -> Unit,
    onSuccess: () -> Unit,
) {
    setLoadingDialogVisible(true)
    // TODO: add a way to cancel
    // TODO: add fail conditions
    CoroutineScope(Dispatchers.IO).launch {
        // set up Ubuntu file system
        val imageFsInstallSuccess =
            ImageFsInstaller.installIfNeededFuture(context) { progress ->
                // Log.d("XServerScreen", "$progress")
                setLoadingProgress(progress / 100f)
            }.get()
        setLoadingProgress(-1f)

        // TODO: set up containers for each appId+depotId combo (intent extra "container_id")
        val containerId = appId

        // create container if it does not already exist
        val containerManager = ContainerManager(context)
        val container = containerManager.getContainerById(containerId)
            ?: containerManager.createDefaultContainerFuture(WineInfo.MAIN_WINE_VERSION, containerId)
                .get()
        // set up container drives to include app
        val currentDrives = container.drives
        val drivePath = "D:${SteamService.getAppDirPath(appId)}"
        if (!currentDrives.contains(drivePath)) {
            container.drives = drivePath
            container.saveData()
        }
        // must activate container before downloading save files
        containerManager.activateContainer(container)
        // sync save files and check no pending remote operations are running
        val prefixToPath: (String) -> String = { prefix ->
            PathType.from(prefix).toAbsPath(context, appId)
        }
        val postSyncInfo = SteamService.beginLaunchApp(
            appId = appId,
            prefixToPath = prefixToPath,
            ignorePendingOperations = ignorePendingOperations,
            preferredSave = preferredSave,
            parentScope = this,
        ).await()

        setLoadingDialogVisible(false)

        when (postSyncInfo.syncResult) {
            SyncResult.Conflict -> {
                setMessageDialogState(
                    MessageDialogState(
                        visible = true,
                        title = "Save Conflict",
                        message = "There is a new remote save and a new local save, which would you " +
                            "like to keep?\n\nLocal save:\n\t${Date(postSyncInfo.localTimestamp)}" +
                            "\nRemote save:\n\t${Date(postSyncInfo.remoteTimestamp)}",
                        dismissBtnText = "Keep local",
                        confirmBtnText = "Keep remote",
                        onConfirmClick = {
                            launchApp(
                                context = context,
                                appId = appId,
                                preferredSave = SaveLocation.Remote,
                                setLoadingDialogVisible = setLoadingDialogVisible,
                                setLoadingProgress = setLoadingProgress,
                                setMessageDialogState = setMessageDialogState,
                                onSuccess = onSuccess,
                            )
                            setMessageDialogState(MessageDialogState(false))
                        },
                        onDismissClick = {
                            launchApp(
                                context = context,
                                appId = appId,
                                preferredSave = SaveLocation.Local,
                                setLoadingDialogVisible = setLoadingDialogVisible,
                                setLoadingProgress = setLoadingProgress,
                                setMessageDialogState = setMessageDialogState,
                                onSuccess = onSuccess,
                            )
                            setMessageDialogState(MessageDialogState(false))
                        },
                        onDismissRequest = {
                            setMessageDialogState(MessageDialogState(false))
                        },
                    ),
                )
            }

            SyncResult.InProgress,
            SyncResult.UnknownFail,
            SyncResult.DownloadFail,
            SyncResult.UpdateFail,
            -> {
                setMessageDialogState(
                    MessageDialogState(
                        visible = true,
                        title = "Sync Error",
                        message = "Failed to sync save files: ${postSyncInfo.syncResult}",
                        dismissBtnText = "Ok",
                        onDismissClick = {
                            setMessageDialogState(MessageDialogState(false))
                        },
                        onDismissRequest = {
                            setMessageDialogState(MessageDialogState(false))
                        },
                    ),
                )
            }

            SyncResult.PendingOperations -> {
                Log.d(
                    "PluviaMain",
                    "Pending remote operations:${
                        postSyncInfo.pendingRemoteOperations.map { pro ->
                            "\n\tmachineName: ${pro.machineName}" +
                                "\n\ttimestamp: ${Date(pro.timeLastUpdated * 1000L)}" +
                                "\n\toperation: ${pro.operation}"
                        }.joinToString("\n")
                    }",
                )
                if (postSyncInfo.pendingRemoteOperations.size == 1) {
                    val pro = postSyncInfo.pendingRemoteOperations.first()
                    when (pro.operation) {
                        ECloudPendingRemoteOperation.k_ECloudPendingRemoteOperationUploadInProgress -> {
                            // maybe this should instead wait for the upload to finish and then
                            // launch the app
                            setMessageDialogState(
                                MessageDialogState(
                                    visible = true,
                                    title = "Upload in Progress",
                                    message = "You played ${SteamService.getAppInfoOf(appId)?.name} " +
                                        "on the device ${pro.machineName} " +
                                        "(${Date(pro.timeLastUpdated * 1000L)}) and the save of " +
                                        "that session is still uploading.\nTry again later.",
                                    dismissBtnText = "Ok",
                                    onDismissClick = {
                                        setMessageDialogState(MessageDialogState(false))
                                    },
                                    onDismissRequest = {
                                        setMessageDialogState(MessageDialogState(false))
                                    },
                                ),
                            )
                        }

                        ECloudPendingRemoteOperation.k_ECloudPendingRemoteOperationUploadPending -> {
                            setMessageDialogState(
                                MessageDialogState(
                                    visible = true,
                                    title = "Pending Upload",
                                    message = "You played " +
                                        "${SteamService.getAppInfoOf(appId)?.name} " +
                                        "on the device ${pro.machineName} " +
                                        "(${Date(pro.timeLastUpdated * 1000L)}), " +
                                        "and that save is not yet in the cloud. " +
                                        "(upload not started)\nYou can still play " +
                                        "this game, but that may create a conflict " +
                                        "when your previous game progress " +
                                        "successfully uploads.",
                                    confirmBtnText = "Play anyway",
                                    dismissBtnText = "Cancel",
                                    onConfirmClick = {
                                        setMessageDialogState(MessageDialogState(false))
                                        launchApp(
                                            context = context,
                                            appId = appId,
                                            ignorePendingOperations = true,
                                            setLoadingDialogVisible = setLoadingDialogVisible,
                                            setLoadingProgress = setLoadingProgress,
                                            setMessageDialogState = setMessageDialogState,
                                            onSuccess = onSuccess,
                                        )
                                    },
                                    onDismissClick = {
                                        setMessageDialogState(MessageDialogState(false))
                                    },
                                    onDismissRequest = {
                                        setMessageDialogState(MessageDialogState(false))
                                    },
                                ),
                            )
                        }

                        ECloudPendingRemoteOperation.k_ECloudPendingRemoteOperationAppSessionActive -> {
                            setMessageDialogState(
                                MessageDialogState(
                                    visible = true,
                                    title = "App Running",
                                    message = "You are logged in on another device (${pro.machineName}) " +
                                        "already playing ${SteamService.getAppInfoOf(appId)?.name} " +
                                        "(${Date(pro.timeLastUpdated * 1000L)}), and that save " +
                                        "is not yet in the cloud. \nYou can still play this game, " +
                                        "but that will disconnect the other session from Steam " +
                                        "and may create a save conflict when that session " +
                                        "progress is synced",
                                    confirmBtnText = "Play anyway",
                                    dismissBtnText = "Cancel",
                                    onConfirmClick = {
                                        setMessageDialogState(MessageDialogState(false))
                                        launchApp(
                                            context = context,
                                            appId = appId,
                                            ignorePendingOperations = true,
                                            setLoadingDialogVisible = setLoadingDialogVisible,
                                            setLoadingProgress = setLoadingProgress,
                                            setMessageDialogState = setMessageDialogState,
                                            onSuccess = onSuccess,
                                        )
                                    },
                                    onDismissClick = {
                                        setMessageDialogState(MessageDialogState(false))
                                    },
                                    onDismissRequest = {
                                        setMessageDialogState(MessageDialogState(false))
                                    },
                                ),
                            )
                        }

                        ECloudPendingRemoteOperation.k_ECloudPendingRemoteOperationAppSessionSuspended -> {
                            // I don't know what this means, yet
                            setMessageDialogState(
                                MessageDialogState(
                                    visible = true,
                                    title = "Sync Error",
                                    message = "App session suspended",
                                    dismissBtnText = "Ok",
                                    onDismissClick = {
                                        setMessageDialogState(MessageDialogState(false))
                                    },
                                    onDismissRequest = {
                                        setMessageDialogState(MessageDialogState(false))
                                    },
                                ),
                            )
                        }

                        ECloudPendingRemoteOperation.k_ECloudPendingRemoteOperationNone -> {
                            // why are we here
                            setMessageDialogState(
                                MessageDialogState(
                                    visible = true,
                                    title = "Sync Error",
                                    message = "Received pending remote operations whose operation was 'none'",
                                    dismissBtnText = "Ok",
                                    onDismissClick = {
                                        setMessageDialogState(MessageDialogState(false))
                                    },
                                    onDismissRequest = {
                                        setMessageDialogState(MessageDialogState(false))
                                    },
                                ),
                            )
                        }
                    }
                } else {
                    // this should probably be handled differently
                    setMessageDialogState(
                        MessageDialogState(
                            visible = true,
                            title = "Sync Error",
                            message = "Multiple pending remote operations, try again later",
                            dismissBtnText = "Ok",
                            onDismissClick = {
                                setMessageDialogState(MessageDialogState(false))
                            },
                            onDismissRequest = {
                                setMessageDialogState(MessageDialogState(false))
                            },
                        ),
                    )
                }
            }

            SyncResult.UpToDate,
            SyncResult.Success,
            -> onSuccess()
        }
    }
}
