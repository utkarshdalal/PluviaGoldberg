package com.utkarshdalal.PluviaGoldberg.ui.screen.library.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.utkarshdalal.PluviaGoldberg.PrefManager
import com.utkarshdalal.PluviaGoldberg.data.LibraryItem
import com.utkarshdalal.PluviaGoldberg.ui.data.LibraryState
import com.utkarshdalal.PluviaGoldberg.ui.enums.AppFilter
import com.utkarshdalal.PluviaGoldberg.ui.internal.fakeAppInfo
import com.utkarshdalal.PluviaGoldberg.ui.theme.PluviaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryListPane(
    state: LibraryState,
    listState: LazyListState,
    sheetState: SheetState,
    onFilterChanged: (AppFilter) -> Unit,
    onModalBottomSheet: (Boolean) -> Unit,
    onIsSearching: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onNavigate: (Int) -> Unit,
    onSearchQuery: (String) -> Unit,
    onSettings: () -> Unit,
) {
    val expandedFab by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
    val snackBarHost = remember { SnackbarHostState() }

    // Determine the orientation to add additional scaffold padding.
    val configuration = LocalConfiguration.current
    val isPortrait = remember(configuration) {
        configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    Scaffold(
        modifier = if (isPortrait) Modifier else Modifier.statusBarsPadding(),
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
            AnimatedVisibility(
                visible = !state.isSearching,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                ExtendedFloatingActionButton(
                    text = { Text(text = "Filters") },
                    expanded = expandedFab,
                    icon = { Icon(imageVector = Icons.Default.FilterList, contentDescription = null) },
                    onClick = { onModalBottomSheet(true) },
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            LibraryList(
                list = state.appInfoList,
                listState = listState,
                contentPaddingValues = PaddingValues(
                    top = paddingValues.calculateTopPadding(),
                    bottom = 72.dp,
                ),
                onItemClick = onNavigate,
            )
            if (state.modalBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { onModalBottomSheet(false) },
                    sheetState = sheetState,
                    content = {
                        LibraryBottomSheet(
                            selectedFilters = state.appInfoSortType,
                            onFilterChanged = onFilterChanged,
                        )
                    },
                )
            }
        }
    }
}

/***********
 * PREVIEW *
 ***********/

@OptIn(ExperimentalMaterial3Api::class)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview
@Composable
private fun Preview_LibraryListPane() {
    val context = LocalContext.current
    PrefManager.init(context)
    val sheetState = rememberModalBottomSheetState()
    var state by remember {
        mutableStateOf(
            LibraryState(
                appInfoList = List(20) { idx ->
                    val item = fakeAppInfo(idx)
                    LibraryItem(
                        index = idx,
                        appId = item.id,
                        name = item.name,
                        iconHash = item.iconHash,
                    )
                },
            ),
        )
    }
    PluviaTheme {
        Surface {
            LibraryListPane(
                listState = LazyListState(2, 64),
                state = state,
                sheetState = sheetState,
                onFilterChanged = { },
                onModalBottomSheet = {
                    val currentState = state.modalBottomSheet
                    println("State: $currentState")
                    state = state.copy(modalBottomSheet = !currentState)
                },
                onIsSearching = { },
                onSearchQuery = { },
                onSettings = { },
                onLogout = { },
                onNavigate = { },
            )
        }
    }
}
