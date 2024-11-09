package com.OxGames.Pluvia.ui.component

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import coil3.compose.AsyncImage
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.data.AppInfo
import com.OxGames.Pluvia.enums.AppType
import com.OxGames.Pluvia.events.AndroidEvent
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.ui.data.NavScreen
import com.OxGames.Pluvia.ui.enums.NavType
import com.OxGames.Pluvia.ui.enums.ScreenType
import com.OxGames.Pluvia.ui.enums.PluviaScreen
import kotlinx.coroutines.launch
import java.util.EnumSet
import java.util.concurrent.LinkedBlockingDeque

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggedInScreen(lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current) {
    // since jetpack compose's TopAppBarLayout has an "implicit" constraint on heightPx that's not
    // reflected in the constraints themselves so we cannot "fillMaxHeight" or "fillMaxSize"
    // source: https://issuetracker.google.com/issues/300953236?hl=zh-tw
    val topAppBarHeight = 64.dp
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var profilePicUrl by remember { mutableStateOf<String>(SteamService.MISSING_AVATAR_URL) }
    var appsList by remember { mutableStateOf<List<AppInfo>>(SteamService.getAppList(EnumSet.of(AppType.game))) }

    var currentScreen by remember {
        mutableStateOf(NavScreen(PluviaScreen.Library, ScreenType.ROOT, EnumSet.of(NavType.MENU)))
    }
    val screenHistory = LinkedBlockingDeque<NavScreen>()

    val gotoScreen: (screen: NavScreen) -> Unit = {
        if (it.screenType == ScreenType.ROOT) {
            screenHistory.clear()
        } else {
            screenHistory.put(currentScreen)
        }
        currentScreen = it
    }
    val goBack: () -> Unit = {
        if (screenHistory.isNotEmpty()) {
            currentScreen = screenHistory.pop()
        } else {
            // TODO: log out prompt
        }
    }

    DisposableEffect(lifecycleOwner) {
        val onPersonaStateReceived: (SteamEvent.PersonaStateReceived) -> Unit = {
            val persona = SteamService.getPersonaStateOf(it.steamId)
            if (persona != null && persona.friendID.accountID == SteamService.getUserSteamId()?.accountID) {
                Log.d("LoggedInScreen", "Setting avatar url to ${persona.avatarUrl}")
                profilePicUrl = persona.avatarUrl
            }
        }
        val onAppInfoReceived: (SteamEvent.AppInfoReceived) -> Unit = {
            appsList = SteamService.getAppList(EnumSet.of(AppType.game))
            Log.d("LoggedInScreen", "Updating games list with ${appsList.count()} item(s)")
        }
        val onGotoAppScreen: (AndroidEvent.GotoAppScreen) -> Unit = {
            gotoScreen(NavScreen(PluviaScreen.App, ScreenType.END, EnumSet.of(NavType.BACK)))
        }
        val onBackPressed: (AndroidEvent.BackPressed) -> Unit = {
            goBack()
        }
        PluviaApp.events.on<SteamEvent.PersonaStateReceived>(onPersonaStateReceived)
        PluviaApp.events.on<SteamEvent.AppInfoReceived>(onAppInfoReceived)
        PluviaApp.events.on<AndroidEvent.GotoAppScreen>(onGotoAppScreen)
        PluviaApp.events.on<AndroidEvent.BackPressed>(onBackPressed)

        SteamService.requestUserPersona()

        onDispose {
            PluviaApp.events.off<SteamEvent.PersonaStateReceived>(onPersonaStateReceived)
            PluviaApp.events.off<SteamEvent.AppInfoReceived>(onAppInfoReceived)
            PluviaApp.events.off<AndroidEvent.GotoAppScreen>(onGotoAppScreen)
            PluviaApp.events.off<AndroidEvent.BackPressed>(onBackPressed)
        }
    }

    ModalNavigationDrawer(
        drawerState = if (currentScreen.hasMenu) drawerState else DrawerState(DrawerValue.Closed),
        gesturesEnabled = currentScreen.hasMenu,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerItem(
                    icon = { Icon(imageVector = Icons.Outlined.ViewList, "Library") },
                    label = { Text("Library") },
                    selected = false,
                    onClick = {
                        // TODO: navigate to library
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(imageVector = Icons.Filled.Download, "Downloads") },
                    label = { Text("Downloads") },
                    selected = false,
                    onClick = {
                        // TODO: navigate to downloads
                    }
                )
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
        Scaffold(modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("Pluvia") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        }) { Icon(imageVector = Icons.Filled.Menu, "Open menu") }
                    },
                    actions = {
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
                )
            }
        ) { innerPadding ->
            // LibraryScreen(
            //     innerPadding = innerPadding,
            //     appsList = appsList
            // )
        }
    }
}