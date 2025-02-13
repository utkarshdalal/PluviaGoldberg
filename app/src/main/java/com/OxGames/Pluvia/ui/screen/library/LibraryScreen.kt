package com.OxGames.Pluvia.ui.screen.library

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.OxGames.Pluvia.data.LibraryItem
import com.OxGames.Pluvia.service.SteamService
import com.OxGames.Pluvia.ui.component.fabmenu.state.FloatingActionMenuState
import com.OxGames.Pluvia.ui.component.fabmenu.state.FloatingActionMenuValue
import com.OxGames.Pluvia.ui.component.fabmenu.state.rememberFloatingActionMenuState
import com.OxGames.Pluvia.ui.data.LibraryState
import com.OxGames.Pluvia.ui.enums.FabFilter
import com.OxGames.Pluvia.ui.internal.fakeAppInfo
import com.OxGames.Pluvia.ui.model.LibraryViewModel
import com.OxGames.Pluvia.ui.screen.library.components.LibraryDetailPane
import com.OxGames.Pluvia.ui.screen.library.components.LibraryListPane
import com.OxGames.Pluvia.ui.theme.PluviaTheme

@Composable
fun HomeLibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    onClickPlay: (Int, Boolean) -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val fabState = rememberFloatingActionMenuState()

    LibraryScreenContent(
        state = state,
        listState = viewModel.listState,
        fabState = fabState,
        onFabFilter = viewModel::onFabFilter,
        onIsSearching = viewModel::onIsSearching,
        onSearchQuery = viewModel::onSearchQuery,
        onClickPlay = onClickPlay,
        onSettings = onSettings,
        onLogout = onLogout,
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun LibraryScreenContent(
    state: LibraryState,
    listState: LazyListState,
    fabState: FloatingActionMenuState,
    onIsSearching: (Boolean) -> Unit,
    onSearchQuery: (String) -> Unit,
    onFabFilter: (FabFilter) -> Unit,
    onClickPlay: (Int, Boolean) -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
) {
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
                LibraryListPane(
                    state = state,
                    listState = listState,
                    fabState = fabState,
                    onIsSearching = onIsSearching,
                    onSearchQuery = onSearchQuery,
                    onSettings = onSettings,
                    onLogout = onLogout,
                    onFabFilter = onFabFilter,
                    onNavigate = { item ->
                        navigator.navigateTo(
                            pane = ListDetailPaneScaffoldRole.Detail,
                            content = item,
                        )
                    },
                )
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

/***********
 * PREVIEW *
 ***********/

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
            listState = rememberLazyListState(),
            state = LibraryState(
                appInfoList = List(14) { idx ->
                    val item = fakeAppInfo(idx)
                    LibraryItem(
                        index = idx,
                        appId = item.id,
                        name = item.name,
                        iconHash = item.iconHash,
                    )
                },
            ),
            fabState = rememberFloatingActionMenuState(FloatingActionMenuValue.Open),
            onIsSearching = { },
            onSearchQuery = { },
            onFabFilter = { },
            onClickPlay = { _, _ -> },
            onSettings = { },
            onLogout = { },
        )
    }
}
