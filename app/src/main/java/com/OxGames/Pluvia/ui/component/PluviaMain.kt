package com.OxGames.Pluvia.ui.component

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.OxGames.Pluvia.events.AndroidEvent
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.ui.enums.Orientation
import com.OxGames.Pluvia.ui.enums.PluviaScreen
import com.OxGames.Pluvia.ui.model.UserLoginViewModel
import com.OxGames.Pluvia.ui.theme.PluviaTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.EnumSet

@Composable
fun PluviaMain(
    userLoginViewModel: UserLoginViewModel = viewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // since jetpack compose's TopAppBarLayout has an "implicit" constraint on heightPx that's not
    // reflected in the constraints themselves so we cannot "fillMaxHeight" or "fillMaxSize"
    // source: https://issuetracker.google.com/issues/300953236?hl=zh-tw
    val topAppBarHeight = 64.dp

    // val currentBackStackEntry by navController.currentBackStackEntryAsState()
    // val currentScreen = PluviaScreen.valueOf(currentBackStackEntry?.destination?.route ?: PluviaScreen.LoginUser.name)
    var resettedScreen by rememberSaveable { mutableStateOf<PluviaScreen?>(null) }
    var currentScreen by rememberSaveable {
        mutableStateOf(
            PluviaScreen.valueOf(
                resettedScreen?.name ?: PluviaScreen.LoginUser.name
            )
        )
    }
    var hasLaunched by rememberSaveable { mutableStateOf(false) }

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

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val toggleDrawer: () -> Unit = {
        scope.launch {
            drawerState.apply {
                if (isClosed) open() else close()
            }
        }
    }
    val closeDrawer: () -> Unit = {
        scope.launch {
            drawerState.apply {
                if (!isClosed) close()
            }
        }
    }

    var topAppBarVisible by rememberSaveable { mutableStateOf(true) }

    var isSteamConnected by remember { mutableStateOf(SteamService.isConnected) }
    var isLoggingIn by remember { mutableStateOf(SteamService.isLoggingIn) }
    var isLoggedIn by remember { mutableStateOf(SteamService.isLoggedIn) }
    var profilePicUrl by remember {
        mutableStateOf<String>(
            SteamService.getUserSteamId()?.let {
                SteamService.getPersonaStateOf(it)
            }?.avatarUrl ?: SteamService.MISSING_AVATAR_URL
        )
    }
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
            isLoggingIn = it.isAutoLoggingIn
            isSteamConnected = true
        }
        val onSteamDisconnected: (SteamEvent.Disconnected) -> Unit = {
            Log.d("PluviaMain", "Received disconnected from Steam")
            isSteamConnected = false
        }
        val onLoggingIn: (SteamEvent.LogonStarted) -> Unit = {
            Log.d("PluviaMain", "Received logon started")
            isLoggingIn = true
        }
        val onLogonEnded: (SteamEvent.LogonEnded) -> Unit = {
            CoroutineScope(Dispatchers.Main).launch {
                Log.d("PluviaMain", "Received logon ended")
                isLoggingIn = false
                isLoggedIn = it.loginResult == LoginResult.Success

                when (it.loginResult) {
                    LoginResult.Success -> {
                        // TODO: add preference for first screen on login
                        Log.d("PluviaMain", "Navigating to library")
                        navController.navigate(PluviaScreen.Library.name)
                    }

                    LoginResult.EmailAuth, LoginResult.TwoFactorCode -> {
                        Log.d("PluviaMain", "Navigating to 2fa")
                        navController.navigate(PluviaScreen.LoginTwoFactor.name)
                    }

                    else -> {
                        Log.d("PluviaMain", "Received non-result: ${it.loginResult}")
                    }
                }
            }
        }
        val onLoggedOut: (SteamEvent.LoggedOut) -> Unit = {
            Log.d("PluviaMain", "Received logged out")
            isLoggingIn = false
            isLoggedIn = false
        }
        val onPersonaStateReceived: (SteamEvent.PersonaStateReceived) -> Unit = {
            val persona = SteamService.getPersonaStateOf(it.steamId)
            // Log.d("PluviaMain", "Testing persona of ${persona?.name}:${persona?.friendID?.accountID} to ${SteamService.getUserSteamId()?.accountID}")
            if (persona != null && persona.friendID.accountID == SteamService.getUserSteamId()?.accountID) {
                Log.d("PluviaMain", "Setting avatar url to ${persona.avatarUrl}")
                profilePicUrl = persona.avatarUrl
            }
        }

        PluviaApp.events.on<AndroidEvent.SetAppBarVisibility, Unit>(onHideAppBar)
        PluviaApp.events.on<AndroidEvent.BackPressed, Unit>(onBackPressed)
        PluviaApp.events.on<SteamEvent.Connected, Unit>(onSteamConnected)
        PluviaApp.events.on<SteamEvent.Disconnected, Unit>(onSteamDisconnected)
        // PluviaApp.events.on<SteamEvent.LogonState, Unit>(onLoggingIn)
        PluviaApp.events.on<SteamEvent.LogonEnded, Unit>(onLogonEnded)
        PluviaApp.events.on<SteamEvent.LoggedOut, Unit>(onLoggedOut)
        PluviaApp.events.on<SteamEvent.PersonaStateReceived, Unit>(onPersonaStateReceived)

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
            PluviaApp.events.off<SteamEvent.PersonaStateReceived, Unit>(onPersonaStateReceived)
        }
    }

    PluviaTheme {
        ModalNavigationDrawer(
            drawerState = if (currentScreen.hasMenu) drawerState else DrawerState(DrawerValue.Closed),
            gesturesEnabled = currentScreen.hasMenu,
            drawerContent = {
                ModalDrawerSheet {
                    currentScreen.menuNavRoutes?.forEach {
                        NavigationDrawerItem(
                            icon = { Icon(imageVector = it.icon, stringResource(it.title)) },
                            label = { Text(stringResource(it.title)) },
                            selected = false,
                            onClick = {
                                if (currentScreen != it) {
                                    if (!navController.popBackStack(it.name, false)) {
                                        navController.navigate(it.name)
                                    }
                                }
                                closeDrawer()
                            }
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    NavigationDrawerItem(
                        icon = { Icon(imageVector = Icons.AutoMirrored.Filled.Logout, "Log out") },
                        label = { Text("Log out") },
                        selected = false,
                        onClick = { SteamService.logOut() }
                    )
                }
            }
        ) {
//            Scaffold(
//                modifier = Modifier.fillMaxSize(),
//                topBar = {
//                    if (topAppBarVisible) {
//                        TopAppBar(
//                            title = {
//                                val title = if (currentScreen == PluviaScreen.App) {
//                                    SteamService.getAppInfoOf(appId)?.name
//                                } else {
//                                    null
//                                } ?: stringResource(currentScreen.title)
//                                Text(title)
//                            },
//                            navigationIcon = {
//                                if (currentScreen.hasMenu) {
//                                    IconButton(onClick = {
//                                        toggleDrawer()
//                                    }) { Icon(imageVector = Icons.Filled.Menu, "Open menu") }
//                                } else if (hasBack) {
//                                    // only if we don't have a menu and there is a parent route
//                                    IconButton(onClick = {
//                                        navController.popBackStack()
//                                    }) {
//                                        Icon(
//                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                                            "Go back"
//                                        )
//                                    }
//                                }
//                            },
//                            actions = {
//                                if (isLoggedIn) {
//                                    Box(
//                                        modifier = Modifier
//                                            .height(topAppBarHeight)
//                                    ) {
//                                        CoilAsyncImage(
//                                            modifier = Modifier.padding(8.dp),
//                                            url = profilePicUrl,
//                                            size = 48.dp,
//                                            contentDescription = "Profile picture",
//                                        )
//                                    }
//                                }
//                            }
//                        )
//                    }
//                },
//            ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = PluviaScreen.LoginUser.name,
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                composable(route = PluviaScreen.LoginUser.name) {
                    UserLoginScreen(
                        userLoginViewModel = userLoginViewModel
                    )
                }
                composable(route = PluviaScreen.LoginTwoFactor.name) {
                    TwoFactorAuthScreen(userLoginViewModel)
                }
                composable(route = PluviaScreen.Library.name) {
                    LibraryScreen(
                        onClickPlay = {
                            appId = it
                            navController.navigate(PluviaScreen.XServer.name)
                        }
                    )
                }
                composable(route = PluviaScreen.Downloads.name) {
                    DownloadsScreen()
                }
                composable(route = PluviaScreen.XServer.name) {
                    XServerScreen(appId = appId)
                }
            }
        }
    }
//        }
}