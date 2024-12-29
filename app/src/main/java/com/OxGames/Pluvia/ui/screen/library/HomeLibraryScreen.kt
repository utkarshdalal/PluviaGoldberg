package com.OxGames.Pluvia.ui.screen.library

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.data.AppInfo
import com.OxGames.Pluvia.ui.component.fabmenu.FloatingActionMenu
import com.OxGames.Pluvia.ui.component.fabmenu.FloatingActionMenuItem
import com.OxGames.Pluvia.ui.component.fabmenu.state.FloatingActionMenuState
import com.OxGames.Pluvia.ui.component.fabmenu.state.FloatingActionMenuValue
import com.OxGames.Pluvia.ui.component.fabmenu.state.rememberFloatingActionMenuState
import com.OxGames.Pluvia.ui.component.topbar.AccountButton
import com.OxGames.Pluvia.ui.data.LibraryState
import com.OxGames.Pluvia.ui.enums.FabFilter
import com.OxGames.Pluvia.ui.internal.fakeAppInfo
import com.OxGames.Pluvia.ui.model.LibraryViewModel
import com.OxGames.Pluvia.ui.theme.PluviaTheme

@Composable
fun HomeLibraryScreen(
    viewModel: LibraryViewModel = viewModel(),
    onClickPlay: (Int, Boolean) -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    val vmState by viewModel.state.collectAsStateWithLifecycle()
    val fabState = rememberFloatingActionMenuState()

    LibraryScreenContent(
        vmState = vmState,
        fabState = fabState,
        onFabFilter = viewModel::onFabFilter,
        onClickPlay = onClickPlay,
        onSettings = onSettings,
        onLogout = onLogout,
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun LibraryScreenContent(
    vmState: LibraryState,
    fabState: FloatingActionMenuState,
    onFabFilter: (FabFilter) -> Unit,
    onClickPlay: (Int, Boolean) -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    val snackbarHost = remember { SnackbarHostState() }
    val navigator = rememberListDetailPaneScaffoldNavigator<Int>()

    // Pretty much the same as 'NavigableListDetailPaneScaffold'
    BackHandler(navigator.canNavigateBack(BackNavigationBehavior.PopUntilContentChange)) {
        navigator.navigateBack(BackNavigationBehavior.PopUntilContentChange)
    }

    ListDetailPaneScaffold(
        modifier = Modifier.displayCutoutPadding(),
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHost) },
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text(text = "Library") },
                            actions = {
                                AccountButton(
                                    onSettings = onSettings,
                                    onLogout = onLogout,
                                )
                            },
                        )
                    },
                    floatingActionButton = {
                        FloatingActionMenu(
                            state = fabState,
                            imageVector = Icons.Filled.FilterList,
                            closeImageVector = Icons.Filled.Close,
                        ) {
                            FloatingActionMenuItem(
                                labelText = "Search",
                                onClick = { onFabFilter(FabFilter.SEARCH) },
                                content = { Icon(Icons.Filled.Search, "Search") },
                            )
                            FloatingActionMenuItem(
                                labelText = "Installed",
                                onClick = { onFabFilter(FabFilter.INSTALLED) },
                                content = { Icon(Icons.Filled.InstallMobile, "Installed") },
                            )
                            FloatingActionMenuItem(
                                labelText = "Alphabetic",
                                onClick = { onFabFilter(FabFilter.ALPHABETIC) },
                                content = { Icon(Icons.Filled.SortByAlpha, "Alphabetic") },
                            )
                        }
                    },
                ) { paddingValues ->
                    LibraryListPane(
                        paddingValues = paddingValues,
                        list = vmState.appInfoList,
                        onItemClick = { item ->
                            navigator.navigateTo(
                                pane = ListDetailPaneScaffoldRole.Detail,
                                content = item,
                            )
                        },
                    )
                }
            }
        },
        detailPane = {
            val appId = (navigator.currentDestination?.content ?: SteamService.INVALID_APP_ID)
            AnimatedPane {
                LibraryDetailPane(
                    appId = appId,
                    onBack = {
                        // We're still in Adaptive navigation.
                        navigator.navigateBack()
                    },
                    onClickPlay = { onClickPlay(appId, it) },
                )
            }
        },
    )
}

@Composable
private fun LibraryListPane(
    paddingValues: PaddingValues,
    list: List<AppInfo>,
    onItemClick: (Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        contentPadding = PaddingValues(bottom = 72.dp), // Extra space for fab
    ) {
        items(list, key = { it.appId }) { item ->
            AppItem(
                modifier = Modifier.animateItem(),
                appInfo = item,
                onClick = { onItemClick(item.appId) },
            )
        }
    }
}

@Composable
private fun LibraryDetailPane(
    appId: Int,
    onClickPlay: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    Surface {
        if (appId == SteamService.INVALID_APP_ID) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // TODO prettify this?
                Text("Select an item")
            }
        } else {
            AppScreen(
                appId = appId,
                onClickPlay = onClickPlay,
                onBack = onBack,
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    device = "spec:width=1080px,height=1920px,dpi=440,orientation=landscape",
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    device = "id:pixel_tablet",
)
@Composable
private fun Preview_LibraryScreenContent() {
    PluviaTheme {
        LibraryScreenContent(
            vmState = LibraryState(
                appInfoList = List(15) { fakeAppInfo(it).copy(appId = it) },
            ),
            fabState = rememberFloatingActionMenuState(FloatingActionMenuValue.Open),
            onFabFilter = {},
            onClickPlay = { _, _ -> },
            onSettings = {},
            onLogout = {},
        )
    }
}
