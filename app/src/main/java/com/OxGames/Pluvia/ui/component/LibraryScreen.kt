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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.data.AppInfo
import com.OxGames.Pluvia.enums.AppType
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.ui.component.fabmenu.FloatingActionMenu
import com.OxGames.Pluvia.ui.component.fabmenu.FloatingActionMenuItem
import com.OxGames.Pluvia.ui.component.fabmenu.state.FloatingActionMenuState
import com.OxGames.Pluvia.ui.component.fabmenu.state.FloatingActionMenuValue
import com.OxGames.Pluvia.ui.component.fabmenu.state.rememberFloatingActionMenuState
import com.OxGames.Pluvia.ui.component.internal.fakeAppInfo
import com.OxGames.Pluvia.ui.theme.PluviaTheme
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

    val fabState = rememberFloatingActionMenuState()

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

    LibraryScreenContent(
        appsList = appsList,
        fabState = fabState,
        onAppClick = onAppClick,
        onInstalledClick = {
            installed = !installed
            appsList = getAppsList()
        },
        onAlphabeticClick = {
            alphabetic = !alphabetic
            appsList = getAppsList()
        },
        onSearchClick = {
            // TODO: actually implement search (probably through the app bar)
            searchTerm = if (searchTerm.isEmpty()) "nidhogg" else ""
            appsList = getAppsList()
        }
    )

}

@Composable
private fun LibraryScreenContent(
    appsList: List<AppInfo>,
    fabState: FloatingActionMenuState,
    onAppClick: (appId: Int) -> Unit,
    onInstalledClick: () -> Unit,
    onAlphabeticClick: () -> Unit,
    onSearchClick: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
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

        // TODO hoist
        FloatingActionMenu(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-16).dp, y = (-8).dp),
            state = fabState,
            imageVector = Icons.Filled.FilterList,
            closeImageVector = Icons.Filled.Close
        ) {
            FloatingActionMenuItem(
                labelText = "Search",
                onClick = onSearchClick
            ) { Icon(Icons.Filled.Search, "Search") }
            FloatingActionMenuItem(
                labelText = "Installed",
                onClick = onInstalledClick
            ) { Icon(Icons.Filled.InstallMobile, "Installed") }
            FloatingActionMenuItem(
                labelText = "Alphabetic",
                onClick = onAlphabeticClick
            ) { Icon(Icons.Filled.SortByAlpha, "Alphabetic") }
        }
    }
}

@Preview
@Composable
private fun Preview_LibraryScreenContent() {
    PluviaTheme {
        Surface {
            LibraryScreenContent(
                appsList = List(10) { fakeAppInfo() },
                fabState = rememberFloatingActionMenuState(),
                onAppClick = { },
                onInstalledClick = { },
                onAlphabeticClick = { },
                onSearchClick = { },
            )
        }
    }
}

@Preview
@Composable
private fun Preview_LibraryScreenContent_OpenFab() {
    PluviaTheme {
        Surface {
            LibraryScreenContent(
                appsList = List(10) { fakeAppInfo() },
                fabState = rememberFloatingActionMenuState(initialValue = FloatingActionMenuValue.Open),
                onAppClick = { },
                onInstalledClick = { },
                onAlphabeticClick = { },
                onSearchClick = { },
            )
        }
    }
}