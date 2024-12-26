package com.OxGames.Pluvia.ui.screen.friends

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.OxGames.Pluvia.data.SteamFriend
import com.OxGames.Pluvia.ui.component.topbar.AccountButton
import com.OxGames.Pluvia.ui.component.topbar.BackButton
import com.OxGames.Pluvia.ui.data.FriendsState
import com.OxGames.Pluvia.ui.model.FriendsViewModel
import com.OxGames.Pluvia.ui.theme.PluviaTheme
import `in`.dragonbra.javasteam.types.SteamID
import kotlinx.coroutines.launch

@Composable
fun HomeFriendsScreen(
    viewModel: FriendsViewModel = hiltViewModel(),
    onSettings: () -> Unit,
) {
    val state by viewModel.friendsState.collectAsStateWithLifecycle()

    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    FriendsScreenContent(
        state = state,
        onBack = { onBackPressedDispatcher?.onBackPressed() },
        onSettings = onSettings,
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun FriendsScreenContent(
    state: FriendsState,
    onBack: () -> Unit,
    onSettings: () -> Unit,
) {
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val navigator = rememberListDetailPaneScaffoldNavigator<SteamID>()

    // Pretty much the same as 'NavigableListDetailPaneScaffold'
    BackHandler(navigator.canNavigateBack(BackNavigationBehavior.PopUntilContentChange)) {
        navigator.navigateBack(BackNavigationBehavior.PopUntilContentChange)
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHost) },
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text(text = "Friends") },
                            actions = { AccountButton(onSettings = onSettings) },
                            navigationIcon = { BackButton(onClick = onBack) },
                        )
                    },
                ) { paddingValues ->
                    FriendsListPane(
                        paddingValues = paddingValues,
                        list = state.friendsList,
                        onItemClick = {
                            scope.launch {
                                snackbarHost.showSnackbar("TODO Chat")
                                // navigator.navigateTo(
                                //     ListDetailPaneScaffoldRole.Detail,
                                //     SteamID(1L))
                            }
                        },
                    )
                }
            }
        },
        detailPane = {
            val value = navigator.currentDestination?.content ?: SteamID()
            AnimatedPane {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                    content = {
                        if (value.convertToUInt64() == 0L) {
                            Text("Choose something from Friends")
                        } else {
                            Text("Hi Friend $value")
                        }
                    },
                )
            }
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FriendsListPane(
    paddingValues: PaddingValues,
    list: Map<String, List<SteamFriend>>,
    onItemClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        contentPadding = PaddingValues(bottom = 72.dp), // Extra space for fab
    ) {
        list.forEach { (key, value) ->
            stickyHeader {
                StickyHeaderItem(
                    isCollapsed = false,
                    header = key,
                    count = value.size,
                    onHeaderAction = { },
                )
            }

            items(value, key = { it.id }) { item ->
                FriendItem(
                    modifier = Modifier.animateItem(),
                    friend = item,
                    onClick = onItemClick,
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview_FriendsScreenContent() {
    PluviaTheme {
        FriendsScreenContent(
            state = FriendsState(
                friendsList = mapOf(
                    "TEST A" to List(3) { SteamFriend(id = it.toLong()) },
                    "TEST B" to List(3) { SteamFriend(id = it.toLong() + 5) },
                    "TEST C" to List(3) { SteamFriend(id = it.toLong() + 10) },
                ),
            ),
            onBack = { },
            onSettings = { },
        )
    }
}
