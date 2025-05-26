package com.utkarshdalal.PluviaGoldberg.ui.screen

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.toSize
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.utkarshdalal.PluviaGoldberg.Constants
import com.utkarshdalal.PluviaGoldberg.PrefManager
import com.utkarshdalal.PluviaGoldberg.ui.component.dialog.MessageDialog
import com.utkarshdalal.PluviaGoldberg.ui.enums.HomeDestination
import com.utkarshdalal.PluviaGoldberg.ui.model.HomeViewModel
import com.utkarshdalal.PluviaGoldberg.ui.screen.downloads.HomeDownloadsScreen
import com.utkarshdalal.PluviaGoldberg.ui.screen.friends.FriendsScreen
import com.utkarshdalal.PluviaGoldberg.ui.screen.library.HomeLibraryScreen
import com.utkarshdalal.PluviaGoldberg.ui.theme.PluviaTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onChat: (Long) -> Unit,
    onClickExit: () -> Unit,
    onClickPlay: (Int, Boolean) -> Unit,
    onLogout: () -> Unit,
    onSettings: () -> Unit,
) {
    val homeState by viewModel.homeState.collectAsStateWithLifecycle()

    // When in Downloads or Friends, pressing back brings us back to default screen from preference (Default: Library)
    BackHandler(enabled = homeState.currentDestination != PrefManager.startScreen) {
        viewModel.onDestination(PrefManager.startScreen)
    }
    // Pressing back again; while logged in, confirm we want to close the app.
    BackHandler(enabled = homeState.currentDestination == PrefManager.startScreen) {
        viewModel.onConfirmExit(false)
    }

    HomeScreenContent(
        destination = homeState.currentDestination,
        onDestination = viewModel::onDestination,
        onChat = onChat,
        onClickPlay = onClickPlay,
        onLogout = onLogout,
        onSettings = onSettings,
    )
}

@Composable
private fun HomeScreenContent(
    destination: HomeDestination,
    onDestination: (HomeDestination) -> Unit,
    onChat: (Long) -> Unit,
    onClickPlay: (Int, Boolean) -> Unit,
    onLogout: () -> Unit,
    onSettings: () -> Unit,
) {
    HomeNavigationWrapperUI(
        destination = destination,
        onDestination = onDestination,
    ) {
        when (destination) {
            HomeDestination.Library -> HomeLibraryScreen(
                onClickPlay = onClickPlay,
                onSettings = onSettings,
                onLogout = onLogout,
            )

            HomeDestination.Downloads -> HomeDownloadsScreen(
                onSettings = onSettings,
                onLogout = onLogout,
            )

            HomeDestination.Friends -> FriendsScreen(
                onSettings = onSettings,
                onChat = onChat,
                onLogout = onLogout,
            )
        }
    }
}

@Composable
internal fun HomeNavigationWrapperUI(
    destination: HomeDestination,
    onDestination: (HomeDestination) -> Unit,
    content: @Composable () -> Unit = {},
) {
    val windowSize = with(LocalDensity.current) {
        currentWindowSize().toSize().toDpSize()
    }
    val navLayoutType = if (windowSize.width >= Constants.Composables.WINDOW_WIDTH_LARGE) {
        // Show a permanent drawer when window width is large.
        NavigationSuiteType.NavigationDrawer
    } else {
        // Otherwise use the default from NavigationSuiteScaffold.
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            HomeDestination.entries.forEach {
                item(
                    label = { Text(stringResource(it.title)) },
                    icon = { Icon(it.icon, stringResource(it.title)) },
                    selected = (it == destination),
                    onClick = { onDestination(it) },
                )
            }
        },
        layoutType = navLayoutType,
        content = content,
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
        var destination: HomeDestination by remember {
            mutableStateOf(HomeDestination.Library)
        }
        HomeScreenContent(
            destination = destination,
            onDestination = { destination = it },
            onChat = {},
            onClickPlay = { _, _ -> },
            onLogout = {},
            onSettings = {},
        )
    }
}
