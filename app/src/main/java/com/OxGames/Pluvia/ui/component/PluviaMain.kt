package com.OxGames.Pluvia.ui.component

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.enums.LoginResult
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.ui.enums.PluviaScreen
import com.OxGames.Pluvia.ui.model.UserLoginViewModel
import com.OxGames.Pluvia.ui.theme.PluviaTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = PluviaScreen.valueOf(
        backStackEntry?.destination?.route ?: PluviaScreen.LoginUser.name
    )

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

    var isSteamConnected by remember { mutableStateOf(SteamService.isConnected) }
    // var isLoggingIn by remember { mutableStateOf(SteamService.isLoggingIn) }
    var isLoggedIn by remember { mutableStateOf(SteamService.isLoggedIn) }
    var profilePicUrl by remember { mutableStateOf<String>(SteamService.MISSING_AVATAR_URL) }

    DisposableEffect(lifecycleOwner) {
        val onSteamConnected: (SteamEvent.Connected) -> Unit = {
            Log.d("PluviaMain", "Received is connected")
            // isLoggingIn = it.isAutoLoggingIn
            isSteamConnected = true
        }
        val onSteamDisconnected: (SteamEvent.Disconnected) -> Unit = {
            Log.d("PluviaMain", "Received disconnected from Steam")
            isSteamConnected = false
        }
        // val onLoggingIn: (SteamEvent.LogonStarted) -> Unit = {
        //     Log.d("PluviaMain", "Received logon started")
        //     isLoggingIn = true
        // }
        val onLogonEnded: (SteamEvent.LogonEnded) -> Unit = {
            Log.d("PluviaMain", "Received logon ended")
            // isLoggingIn = false
            isLoggedIn = it.loginResult == LoginResult.Success

            when (it.loginResult) {
                LoginResult.Success -> {
                    Log.d("PluviaMain", "Navigating to library")
                    navController.navigate(PluviaScreen.Library.name)
                } // TODO: add preference for first screen on login
                LoginResult.EmailAuth -> {
                    Log.d("PluviaMain", "Navigating to email auth")
                    navController.navigate(PluviaScreen.LoginTwoFactor.name)
                }
                LoginResult.TwoFactorCode -> {
                    Log.d("PluviaMain", "Navigating to 2fa")
                    navController.navigate(PluviaScreen.LoginTwoFactor.name)
                }
                else -> { Log.d("PluviaMain", "Received non-result: ${it.loginResult}") }
            }
        }
        val onLoggedOut: (SteamEvent.LoggedOut) -> Unit = {
            Log.d("PluviaMain", "Received logged out")
            // isLoggingIn = false
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

        PluviaApp.events.on<SteamEvent.Connected>(onSteamConnected)
        PluviaApp.events.on<SteamEvent.Disconnected>(onSteamDisconnected)
        // PluviaApp.events.on<SteamEvent.LogonStarted>(onLoggingIn)
        PluviaApp.events.on<SteamEvent.LogonEnded>(onLogonEnded)
        PluviaApp.events.on<SteamEvent.LoggedOut>(onLoggedOut)
        PluviaApp.events.on<SteamEvent.PersonaStateReceived>(onPersonaStateReceived)

        if (!isSteamConnected) {
            val intent = Intent(context, SteamService::class.java)
            context.startService(intent)
        }

        onDispose {
            PluviaApp.events.off<SteamEvent.Connected>(onSteamConnected)
            PluviaApp.events.off<SteamEvent.Disconnected>(onSteamDisconnected)
            // PluviaApp.events.off<SteamEvent.LogonStarted>(onLoggingIn)
            PluviaApp.events.off<SteamEvent.LogonEnded>(onLogonEnded)
            PluviaApp.events.off<SteamEvent.LoggedOut>(onLoggedOut)
            PluviaApp.events.off<SteamEvent.PersonaStateReceived>(onPersonaStateReceived)
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
                        icon = { Icon(imageVector = Icons.Filled.Logout, "Log out") },
                        label = { Text("Log out") },
                        selected = false,
                        onClick = { SteamService.logOut() }
                    )
                }
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(currentScreen.title)) },
                        navigationIcon = {
                            if (currentScreen.hasMenu) {
                                IconButton(onClick = {
                                    toggleDrawer()
                                }) { Icon(imageVector = Icons.Filled.Menu, "Open menu") }
                            } else if (backStackEntry?.destination?.parent?.route != null) {
                                // only if we don't have a menu and there is a parent route
                                IconButton(onClick = {
                                    navController.popBackStack()
                                }) { Icon(imageVector = Icons.Filled.ArrowBack, "Go back") }
                            }
                        },
                        actions = {
                            if (isLoggedIn) {
                                Box(modifier = Modifier
                                    .height(topAppBarHeight)
                                ) {
                                    AsyncImage(
                                        model = profilePicUrl,
                                        contentDescription = "Profile picture",
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .clip(CircleShape)
                                    )
                                }
                            }
                        }
                    )
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = PluviaScreen.LoginUser.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    composable(route = PluviaScreen.LoginUser.name) {
                        UserLoginScreen(userLoginViewModel)
                    }
                    composable(route = PluviaScreen.LoginQR.name) {
                        QrLoginScreen()
                    }
                    composable(route = PluviaScreen.LoginTwoFactor.name) {
                        TwoFactorAuthScreen(userLoginViewModel)
                    }
                    composable(route = PluviaScreen.Library.name) {
                        LibraryScreen()
                    }
                    composable(route = PluviaScreen.Downloads.name) {
                        DownloadsScreen()
                    }
                }
            }
        }
    }
}