package com.OxGames.Pluvia.ui.component

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.data.AppInfo
import com.OxGames.Pluvia.enums.AppType
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.ui.component.fabmenu.FloatingActionMenu
import com.OxGames.Pluvia.ui.component.fabmenu.FloatingActionMenuItem
import java.util.EnumSet

@Composable
fun LibraryScreen(
    onAppClick: (appId: Int) -> Unit,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    // TODO: make values persistent after app close
    var searchTerm by rememberSaveable { mutableStateOf("") }
    var alphabetic by rememberSaveable { mutableStateOf(false) }
    var installed by rememberSaveable { mutableStateOf(false) }
    val getAppsList: () -> List<AppInfo> = {
        SteamService.getAppList(EnumSet.of(AppType.game))
            .filter { if (installed) SteamService.isAppInstalled(it.appId) else true }
            .filter { it.name.contains(searchTerm, true) }
            .let {
                if (alphabetic) it.sortedBy { appInfo -> appInfo.name }
                else it.sortedBy { appInfo -> appInfo.receiveIndex }.reversed()
            }
    }
    var appsList by remember { mutableStateOf<List<AppInfo>>(getAppsList()) }

    DisposableEffect(lifecycleOwner) {
        val onAppInfoReceived: (SteamEvent.AppInfoReceived) -> Unit = {
            appsList = getAppsList()
            Log.d("LibraryScreen", "Updating games list with ${appsList.count()} item(s)")
        }

        PluviaApp.events.on<SteamEvent.AppInfoReceived, Unit>(onAppInfoReceived)

        onDispose {
            PluviaApp.events.off<SteamEvent.AppInfoReceived, Unit>(onAppInfoReceived)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            items(appsList) {
                AppItem(
                    appInfo = it,
                    onClick = {
                        onAppClick(it.appId)
                    }
                )
            }
        }

        FloatingActionMenu(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-16).dp, y = (-8).dp),
            imageVector = Icons.Filled.FilterList,
            closeImageVector = Icons.Filled.Close
        ) {
            FloatingActionMenuItem(
                labelText = "Search",
                onClick = {
                    // TODO: actually implement search (probably through the app bar)
                    searchTerm = if (searchTerm.isEmpty()) "nidhogg" else ""
                    appsList = getAppsList()
                }
            ) { Icon(Icons.Filled.Search, "Search") }
            FloatingActionMenuItem(
                labelText = "Installed",
                onClick = {
                    installed = !installed
                    appsList = getAppsList()
                }
            ) { Icon(Icons.Filled.InstallMobile, "Installed") }
            FloatingActionMenuItem(
                labelText = "Alphabetic",
                onClick = {
                    alphabetic = !alphabetic
                    appsList = getAppsList()
                }
            ) { Icon(Icons.Filled.SortByAlpha, "Alphabetic") }
        }
    }
}