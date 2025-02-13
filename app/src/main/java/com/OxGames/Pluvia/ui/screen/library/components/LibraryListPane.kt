package com.OxGames.Pluvia.ui.screen.library.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.OxGames.Pluvia.data.LibraryItem
import com.OxGames.Pluvia.ui.component.fabmenu.state.FloatingActionMenuState
import com.OxGames.Pluvia.ui.component.fabmenu.state.FloatingActionMenuValue
import com.OxGames.Pluvia.ui.component.fabmenu.state.rememberFloatingActionMenuState
import com.OxGames.Pluvia.ui.data.LibraryState
import com.OxGames.Pluvia.ui.enums.FabFilter
import com.OxGames.Pluvia.ui.internal.fakeAppInfo
import com.OxGames.Pluvia.ui.theme.PluviaTheme

@Composable
internal fun LibraryListPane(
    state: LibraryState,
    fabState: FloatingActionMenuState,
    listState: LazyListState,
    onFabFilter: (FabFilter) -> Unit,
    onIsSearching: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onNavigate: (Int) -> Unit,
    onSearchQuery: (String) -> Unit,
    onSettings: () -> Unit,
) {
    val snackBarHost = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHost) },
        topBar = {
            val searchListState = rememberLazyListState()
            LibrarySearchBar(
                state = state,
                listState = searchListState,
                onIsSearching = onIsSearching,
                onSearchQuery = onSearchQuery,
                onSettings = onSettings,
                onLogout = onLogout,
                onItemClick = onNavigate,
            )
        },
        floatingActionButton = {
            LibraryFab(
                fabState = fabState,
                state = state,
                onFabFilter = onFabFilter,
            )
        },
    ) { paddingValues ->
        val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        LibraryList(
            list = state.appInfoList,
            listState = listState,
            paddingValues = PaddingValues(
                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                top = statusBarPadding,
                bottom = paddingValues.calculateBottomPadding(),
            ),
            contentPaddingValues = PaddingValues(
                top = paddingValues.calculateTopPadding().minus(statusBarPadding),
                bottom = 72.dp,
            ),
            onItemClick = onNavigate,
        )
    }
}

/***********
 * PREVIEW *
 ***********/

@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES or android.content.res.Configuration.UI_MODE_TYPE_NORMAL)
@Preview
@Composable
private fun Preview_LibraryListPane() {
    PluviaTheme {
        Surface {
            LibraryListPane(
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
                onSettings = { },
                onLogout = { },
                onNavigate = { },
            )
        }
    }
}
