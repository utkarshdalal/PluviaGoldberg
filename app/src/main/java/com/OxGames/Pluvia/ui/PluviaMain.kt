package com.OxGames.Pluvia.ui

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.enums.LoginResult
import com.OxGames.Pluvia.enums.PathType
import com.OxGames.Pluvia.events.AndroidEvent
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.ui.screen.home.HomeScreen
import com.OxGames.Pluvia.ui.screen.login.UserLoginScreen
import com.OxGames.Pluvia.ui.enums.Orientation
import com.OxGames.Pluvia.ui.enums.PluviaScreen
import com.OxGames.Pluvia.ui.model.HomeViewModel
import com.OxGames.Pluvia.ui.model.UserLoginViewModel
import com.OxGames.Pluvia.ui.screen.xserver.XServerScreen
import com.OxGames.Pluvia.ui.theme.PluviaTheme
import com.winlator.container.ContainerManager
import com.winlator.core.WineInfo
import com.winlator.xenvironment.ImageFsInstaller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.EnumSet

@Composable
fun PluviaMain(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current

    var resettedScreen by rememberSaveable { mutableStateOf<PluviaScreen?>(null) }
    var currentScreen by rememberSaveable {
        mutableStateOf(
            PluviaScreen.valueOf(
                resettedScreen?.name ?: PluviaScreen.LoginUser.name
            )
        )
    }
    var hasLaunched by rememberSaveable { mutableStateOf(false) }
    var dialogVisible by remember { mutableStateOf(false) }
    var dialogProgress by remember { mutableFloatStateOf(0f) }

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
            // reset available orientations
            PluviaApp.events.emit(AndroidEvent.SetAllowedOrientation(EnumSet.of(Orientation.UNSPECIFIED))) // TODO: add option for user to set
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
                    saveState = false
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
            context.startService(intent)
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
        when {
            dialogVisible -> {
                Dialog(
                    onDismissRequest = {}
                ) {
                    Card {
                        Column(
                            modifier = Modifier
                                // .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Loading...")
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(progress = { dialogProgress })
                        }
                    }
                }
            }
        }
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
                    viewModel = viewModel
                )
            }
            /** Library, Downloads, Friends **/
            composable(route = PluviaScreen.Home.name) {
                val viewModel: HomeViewModel = viewModel()
                HomeScreen(
                    viewModel = viewModel,
                    onClickPlay = {
                        appId = it
                        dialogVisible = true
                        // TODO: add a way to cancel
                        // TODO: add fail conditions
                        CoroutineScope(Dispatchers.IO).launch {
                            // set up Ubuntu file system
                            val imageFsInstallSuccess =
                                ImageFsInstaller.installIfNeededFuture(context) { progress ->
                                    // Log.d("XServerScreen", "$progress")
                                    dialogProgress = progress / 100f
                                }.get()

                            // TODO: set up containers for each appId+depotId combo (intent extra "container_id")
                            val containerId = 1

                            // create container if it does not already exist
                            val containerManager = ContainerManager(context)
                            val container = containerManager.getContainerById(containerId)
                                ?: containerManager.createDefaultContainerFuture(WineInfo.MAIN_WINE_VERSION)
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
                            // download save files from steam cloud
                            SteamService.downloadUserFiles(appId, this) { prefix ->
                                PathType.from(prefix).toAbsPath(context, appId)
                            }.await()

                            // SteamUtils.replaceSteamApi(context, appId)

                            dialogVisible = false
                            CoroutineScope(Dispatchers.Main).launch {
                                navController.navigate(PluviaScreen.XServer.name)
                            }
                        }
                    }
                )
            }

            /** Full Screen Chat **/

            /** Game Screen **/
            composable(route = PluviaScreen.XServer.name) {
                XServerScreen(
                    appId = appId,
                    navigateBack = {
                        CoroutineScope(Dispatchers.Main).launch {
                            navController.popBackStack()
                        }
                    }
                )
            }
        }
    }
}
