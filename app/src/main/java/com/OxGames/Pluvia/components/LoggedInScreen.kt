package com.OxGames.Pluvia.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ViewList
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
import androidx.compose.ui.Alignment
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
import com.OxGames.Pluvia.enums.OS
import com.OxGames.Pluvia.events.SteamEvent
import kotlinx.coroutines.launch
import java.util.EnumSet

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
        PluviaApp.events.on<SteamEvent.PersonaStateReceived>(onPersonaStateReceived)
        PluviaApp.events.on<SteamEvent.AppInfoReceived>(onAppInfoReceived)

        SteamService.requestUserPersona()

        onDispose {
            PluviaApp.events.off<SteamEvent.PersonaStateReceived>(onPersonaStateReceived)
            PluviaApp.events.off<SteamEvent.AppInfoReceived>(onAppInfoReceived)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                items(appsList) {
                    AppItem(
                        appInfo = it,
                        onClick = {
                            Log.d("LoggedInScreen", "Clicked on ${it.name}")
                            val appInfo = it
                            val pkgInfo = SteamService.getPkgInfoOf(it.appId)
                            Log.d("LoggedInScreen", "Pkg (${pkgInfo?.packageId}) depot ids: ${pkgInfo?.depotIds?.joinToString(",")}")
                            val depotId = pkgInfo?.depotIds?.firstOrNull {
                                Log.d("LoggedInScreen", "Depot ($it) OSList: " + appInfo.depots[it]?.osList?.toString())
                                appInfo.depots[it]?.osList?.contains(OS.windows) == true
                            }
                            if (depotId != null)
                                SteamService.downloadApp(appInfo.appId, depotId, "public")
                            else
                                Log.e("LoggedInScreen", "Failed to download app (${appInfo.appId}), could not find appropriate depot")
                            // TODO: go to app
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppItem(appInfo: AppInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = appInfo.iconUrl,
            contentDescription = "App icon",
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxHeight()
                .padding(8.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(appInfo.name)
    }
}