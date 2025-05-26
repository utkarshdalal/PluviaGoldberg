package com.utkarshdalal.PluviaGoldberg.ui.screen

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowSize
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

    // Pressing back while logged in, confirm we want to close the app.
    BackHandler {
        onClickExit()
    }

    // Always show the Library screen
    HomeLibraryScreen(
        onClickPlay = onClickPlay,
        onSettings = onSettings,
        onLogout = onLogout,
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
        HomeScreen(
            onChat = {},
            onClickPlay = { _, _ -> },
            onLogout = {},
            onSettings = {},
            onClickExit = {}
        )
    }
}
