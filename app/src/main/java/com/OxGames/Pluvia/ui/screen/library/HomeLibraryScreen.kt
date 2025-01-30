package com.OxGames.Pluvia.ui.screen.library

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.OxGames.Pluvia.data.AppInfo
import com.OxGames.Pluvia.service.SteamService
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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import timber.log.Timber

@Composable
fun HomeLibraryScreen(
    viewModel: LibraryViewModel = viewModel(),
    onClickPlay: (Int, Boolean) -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val fabState = rememberFloatingActionMenuState()

    LibraryScreenContent(
        state = state,
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
    fabState: FloatingActionMenuState,
    onIsSearching: (Boolean) -> Unit,
    onSearchQuery: (String) -> Unit,
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
                        LibrarySearchBar(
                            state = state,
                            onIsSearching = onIsSearching,
                            onSearchQuery = onSearchQuery,
                            onSettings = onSettings,
                            onLogout = onLogout,
                            onItemClick = { item ->
                                navigator.navigateTo(
                                    pane = ListDetailPaneScaffoldRole.Detail,
                                    content = item,
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
                                labelText = "Installed",
                                isSelected = state.appInfoSortType == FabFilter.INSTALLED,
                                onClick = {
                                    onFabFilter(FabFilter.INSTALLED)
                                    fabState.close()
                                },
                                content = { Icon(Icons.Filled.InstallMobile, "Installed") },
                            )
                            FloatingActionMenuItem(
                                labelText = "Alphabetic",
                                isSelected = state.appInfoSortType == FabFilter.ALPHABETIC,
                                onClick = {
                                    onFabFilter(FabFilter.ALPHABETIC)
                                    fabState.close()
                                },
                                content = { Icon(Icons.Filled.SortByAlpha, "Alphabetic") },
                            )
                        }
                    },
                ) { paddingValues ->
                    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    LibraryListPane(
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
                        list = state.appInfoList,
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

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
private fun LibrarySearchBar(
    state: LibraryState,
    onIsSearching: (Boolean) -> Unit,
    onSearchQuery: (String) -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
    onItemClick: (Int) -> Unit,
) {
    val listState = rememberLazyListState()

    // Debouncer: Scroll to the top after a short amount of time after typing quickly
    val internalSearchText = remember { MutableStateFlow(state.searchQuery) }
    LaunchedEffect(Unit) {
        internalSearchText.debounce(500).collect {
            Timber.d("Debounced: Scrolling to top")
            listState.scrollToItem(0)
        }
    }

    // Lambda function to provide new test to both onSearchQuery and internalSearchText
    val onSearchText: (String) -> Unit = {
        onSearchQuery(it)
        internalSearchText.value = it
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        SearchBar(
            modifier = Modifier
                .semantics { traversalIndex = 0f },
            expanded = state.isSearching,
            onExpandedChange = onIsSearching,
            inputField = {
                SearchBarDefaults.InputField(
                    query = state.searchQuery,
                    onSearch = {
                        // This is invoked when IME search is pressed.
                        // But we have nothing to utilize this for.
                    },
                    expanded = state.isSearching,
                    onExpandedChange = onIsSearching,
                    placeholder = { Text(text = "Search for games") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        Crossfade(state.isSearching) { cfState ->
                            if (cfState) {
                                IconButton(
                                    onClick = {
                                        if (state.searchQuery.isEmpty()) {
                                            onIsSearching(false)
                                        } else {
                                            onSearchText("")
                                        }
                                    },
                                    content = {
                                        Icon(Icons.Default.Clear, "Clear search query")
                                    },
                                )
                            } else {
                                AccountButton(
                                    onSettings = onSettings,
                                    onLogout = onLogout,
                                )
                            }
                        }
                    },
                    onQueryChange = onSearchText,
                )
            },
            content = {
                if (state.isSearching) {
                    LibraryListPane(
                        paddingValues = PaddingValues(),
                        contentPaddingValues = PaddingValues(bottom = 72.dp),
                        listState = listState,
                        list = state.appInfoList,
                        onItemClick = onItemClick,
                    )
                }
            },
        )
    }
}

@Composable
private fun LibraryListPane(
    paddingValues: PaddingValues,
    contentPaddingValues: PaddingValues,
    listState: LazyListState = rememberLazyListState(),
    list: List<AppInfo>,
    onItemClick: (Int) -> Unit,
) {
    if (list.isEmpty()) {
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 8.dp,
            ) {
                Text(
                    modifier = Modifier.padding(24.dp),
                    text = "No items listed with selection",
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            state = listState,
            contentPadding = contentPaddingValues,
        ) {
            itemsIndexed(list, key = { _, item -> item.appId }) { idx, item ->
                AppItem(
                    modifier = Modifier.animateItem(),
                    appInfo = item,
                    onClick = { onItemClick(item.appId) },
                )

                if (idx < list.lastIndex) {
                    HorizontalDivider()
                }
            }
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
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 8.dp,
                ) {
                    Text(
                        modifier = Modifier.padding(24.dp),
                        text = "Select an item in the list to view game info",
                    )
                }
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
            state = LibraryState(
                appInfoList = List(15) { fakeAppInfo(it).copy(appId = it) },
            ),
            fabState = rememberFloatingActionMenuState(FloatingActionMenuValue.Open),
            onIsSearching = {},
            onSearchQuery = {},
            onFabFilter = {},
            onClickPlay = { _, _ -> },
            onSettings = {},
            onLogout = {},
        )
    }
}
