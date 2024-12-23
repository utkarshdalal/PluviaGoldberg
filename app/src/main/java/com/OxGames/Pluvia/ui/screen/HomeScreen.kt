package com.OxGames.Pluvia.ui.screen

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowSize
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.OxGames.Pluvia.ui.screen.downloads.HomeDownloadsScreen
import com.OxGames.Pluvia.ui.screen.friends.HomeFriendsScreen
import com.OxGames.Pluvia.ui.enums.PluviaDestination
import com.OxGames.Pluvia.ui.model.HomeViewModel
import com.OxGames.Pluvia.ui.screen.library.HomeLibraryScreen
import com.OxGames.Pluvia.ui.theme.PluviaTheme

private val WINDOW_WIDTH_LARGE = 1200.dp

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onClickPlay: (Int, Boolean) -> Unit,
    onSettings: () -> Unit,
) {
    val homeState by viewModel.homeState.collectAsStateWithLifecycle()

    // When in Downloads or Friends, pressing back brings us back to Library
    BackHandler(enabled = homeState.currentDestination != PluviaDestination.Library) {
        viewModel.onDestination(PluviaDestination.Library)
    }
    // Pressing back again; while logged in, confirm we want to close the app.
    BackHandler(enabled = homeState.currentDestination == PluviaDestination.Library) {
        viewModel.onConfirmExit(true)
    }

    // Confirm to exit
    if (homeState.confirmExit) {
        AlertDialog(
            onDismissRequest = {
                viewModel.onConfirmExit(false)
            },
            icon = {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
            },
            title = {
                Text(text = "Are you sure you want to close Pluvia?")
            },
            confirmButton = {
                // TODO close app
                TextButton(
                    onClick = { viewModel.onConfirmExit(false) },
                    content = { Text(text = "Close") }
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.onConfirmExit(false) },
                    content = { Text(text = "Cancel") }
                )
            }
        )
    }

    HomeScreenContent(
        destination = homeState.currentDestination,
        onDestination = viewModel::onDestination,
        onClickPlay = onClickPlay,
        onSettings = onSettings,
    )
}

@Composable
private fun HomeScreenContent(
    destination: PluviaDestination,
    onDestination: (PluviaDestination) -> Unit,
    onClickPlay: (Int, Boolean) -> Unit,
    onSettings: () -> Unit,
) {
    HomeNavigationWrapperUI(
        destination = destination,
        onDestination = onDestination,
    ) {
        when (destination) {
            PluviaDestination.Library -> HomeLibraryScreen(
                onClickPlay = onClickPlay,
                onSettings = onSettings,
            )

            PluviaDestination.Downloads -> HomeDownloadsScreen(
                onSettings = onSettings,
            )

            PluviaDestination.Friends -> HomeFriendsScreen(
                onSettings = onSettings,
            )
        }
    }
}

@Composable
internal fun HomeNavigationWrapperUI(
    destination: PluviaDestination,
    onDestination: (PluviaDestination) -> Unit,
    content: @Composable () -> Unit = {}
) {
    val windowSize = with(LocalDensity.current) {
        currentWindowSize().toSize().toDpSize()
    }
    val navLayoutType = if (windowSize.width >= WINDOW_WIDTH_LARGE) {
        // Show a permanent drawer when window width is large.
        NavigationSuiteType.NavigationDrawer
    } else {
        // Otherwise use the default from NavigationSuiteScaffold.
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
    }

    // TODO play nice with oxters nav,
    //  but also handle our nav first!
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            PluviaDestination.entries.forEach {
                item(
                    label = { Text(stringResource(it.title)) },
                    icon = { Icon(it.icon, stringResource(it.title)) },
                    selected = it == destination,
                    onClick = { onDestination(it) },
                )
            }
        },
        layoutType = navLayoutType,
        content = content
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    device = "spec:width=1080px,height=1920px,dpi=440,orientation=landscape",
)
@Composable
private fun Preview_HomeScreenContent() {
    PluviaTheme {
        var destination: PluviaDestination by remember {
            mutableStateOf(PluviaDestination.Library)
        }
        HomeScreenContent(
            destination = destination,
            onDestination = { destination = it },
            onClickPlay = { appId, asContainer -> },
            onSettings = {},
        )
    }
}