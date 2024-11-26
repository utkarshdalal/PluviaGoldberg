package com.OxGames.Pluvia.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.ui.component.topbar.AccountButton
import com.OxGames.Pluvia.ui.component.topbar.BackButton
import kotlinx.coroutines.launch

@Composable
fun HomeDownloadsScreen() {
    DownloadsScreenContent()
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DownloadsScreenContent() {
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val navigator = rememberListDetailPaneScaffoldNavigator<Long>()

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
                            title = { Text(text = "Library") },
                            actions = {
                                AccountButton {
                                    scope.launch {
                                        snackbarHost.showSnackbar("TODO")
                                    }
                                }
                            },
                            navigationIcon = {
                                BackButton {
                                    scope.launch {
                                        snackbarHost.showSnackbar("TODO")
                                    }
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        content = {
                            Text("TODO Downloads View!")
                            OutlinedButton(
                                onClick = {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, 1L)
                                },
                                content = {
                                    Text("Click me!")
                                }
                            )
                        }
                    )
                }
            }
        },
        detailPane = {
            val value = (navigator.currentDestination?.content ?: SteamService.INVALID_APP_ID)
            AnimatedPane {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                    content = {
                        if (value == SteamService.INVALID_APP_ID) {
                            Text("Choose something from Download")
                        } else {
                            Text("Hi Download $value")
                        }
                    }
                )
            }
        }
    )
}