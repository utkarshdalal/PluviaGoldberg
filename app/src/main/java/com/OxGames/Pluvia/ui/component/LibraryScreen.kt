package com.OxGames.Pluvia.ui.component

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldScope
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.R
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
import com.OxGames.Pluvia.ui.enums.FabFilter
import com.OxGames.Pluvia.ui.theme.PluviaTheme
import java.util.EnumSet

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun LibraryScreen(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onClickPlay: (Int) -> Unit,
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
    var appsList: List<AppInfo> by remember { mutableStateOf(getAppsList()) }
    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
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
        navigator = navigator,
        fabState = fabState,
        appsList = appsList,
        onClickPlay = onClickPlay,
        onFilter = { filter ->
            when (filter) {
                FabFilter.SEARCH -> {
                    // TODO: actually implement search (probably through the app bar)
                    // searchTerm = if (searchTerm.isEmpty()) "nidhogg" else ""
                    Log.d("LibraryScreen", "Seach not implemented.")
                }

                FabFilter.INSTALLED -> installed = !installed
                FabFilter.ALPHABETIC -> alphabetic = !alphabetic
            }
            appsList = getAppsList()
        }
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun LibraryScreenContent(
    navigator: ThreePaneScaffoldNavigator<Any>,
    fabState: FloatingActionMenuState,
    appsList: List<AppInfo>,
    onClickPlay: (Int) -> Unit,
    onFilter: (FabFilter) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_library))
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navigator.navigateBack()
                        },
                        content = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate Back"
                            )
                        }
                    )
                },
                actions = {

                },
            )
        },
    ) { paddingValues ->
        ListDetailLayout(
            modifier = Modifier.padding(paddingValues),
            fabState = fabState,
            navigator = navigator,
            appsList = appsList,
            onClickPlay = onClickPlay,
            onFilter = onFilter,
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun ListDetailLayout(
    modifier: Modifier = Modifier,
    fabState: FloatingActionMenuState,
    navigator: ThreePaneScaffoldNavigator<Any>,
    appsList: List<AppInfo>,
    onClickPlay: (Int) -> Unit,
    onFilter: (FabFilter) -> Unit,
) {
    NavigableListDetailPaneScaffold(
        modifier = modifier,
        navigator = navigator,
        listPane = {
            LibraryListPane(
                fabState = fabState,
                appList = appsList,
                onAppClick = { appID ->
                    navigator.navigateTo(pane = ListDetailPaneScaffoldRole.Detail, content = appID)
                },
                onFilter = onFilter
            )
        },
        detailPane = {
            LibraryDetailPane(
                appId = (navigator.currentDestination?.content
                    ?: SteamService.INVALID_APP_ID) as Int,
                onClickPlay = onClickPlay
            )
        },
    )
}

@Composable
private fun LibraryListPane(
    fabState: FloatingActionMenuState,
    appList: List<AppInfo>,
    onAppClick: (Int) -> Unit,
    onFilter: (FabFilter) -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            // Suggestion: Hoist?
            FloatingActionMenu(
                state = fabState,
                imageVector = Icons.Filled.FilterList,
                closeImageVector = Icons.Filled.Close
            ) {
                FloatingActionMenuItem(
                    labelText = "Search",
                    onClick = { onFilter(FabFilter.SEARCH) },
                    content = { Icon(Icons.Filled.Search, "Search") }
                )
                FloatingActionMenuItem(
                    labelText = "Installed",
                    onClick = { onFilter(FabFilter.INSTALLED) },
                    content = { Icon(Icons.Filled.InstallMobile, "Installed") }
                )
                FloatingActionMenuItem(
                    labelText = "Alphabetic",
                    onClick = { onFilter(FabFilter.ALPHABETIC) },
                    content = { Icon(Icons.Filled.SortByAlpha, "Alphabetic") }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            items(appList, key = { it.appId }) { item ->
                AppItem(
                    modifier = Modifier.animateItem(),
                    appInfo = item,
                    onClick = {
                        onAppClick(item.appId)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun ThreePaneScaffoldScope.LibraryDetailPane(
    appId: Int,
    onClickPlay: (Int) -> Unit,
) {
    AnimatedPane {
        if (appId == SteamService.INVALID_APP_ID) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // TODO prettify this?
                Text("Select an item")
            }
        } else {
            AppScreen(
                appId = appId,
                onClickPlay = { onClickPlay(appId) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
private fun Preview_LibraryScreenContent() {
    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
    val fabState = rememberFloatingActionMenuState(initialValue = FloatingActionMenuValue.Open)
    PluviaTheme {
        LibraryScreenContent(
            navigator = navigator,
            appsList = List(15) { fakeAppInfo(it).copy(appId = it) },
            fabState = fabState,
            onClickPlay = { },
            onFilter = { },
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview
@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
private fun Preview_LibraryDetailPane() {
    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()
    val fabState = rememberFloatingActionMenuState()
    navigator.navigateTo(pane = ListDetailPaneScaffoldRole.Detail, content = 440)
    PluviaTheme {
        LibraryScreenContent(
            navigator = navigator,
            appsList = List(15) { fakeAppInfo(it).copy(appId = it) },
            fabState = fabState,
            onClickPlay = { },
            onFilter = { },
        )
    }
}
