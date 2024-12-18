package com.OxGames.Pluvia.ui.screen.home

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.OxGames.Pluvia.ui.screen.downloads.HomeDownloadsScreen
import com.OxGames.Pluvia.ui.screen.friends.HomeFriendsScreen
import com.OxGames.Pluvia.ui.enums.PluviaDestination
import com.OxGames.Pluvia.ui.model.HomeViewModel
import com.OxGames.Pluvia.ui.theme.PluviaTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onClickPlay: (Int, Boolean) -> Unit,
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
        onClickPlay = onClickPlay
    )
}

@Composable
private fun HomeScreenContent(
    destination: PluviaDestination,
    onDestination: (PluviaDestination) -> Unit,
    onClickPlay: (Int, Boolean) -> Unit,
) {
    HomeNavigationWrapperUI(
        destination = destination,
        onDestination = onDestination,
    ) {
        when (destination) {
            PluviaDestination.Library -> HomeLibraryScreen(
                onClickPlay = onClickPlay,
            )

            PluviaDestination.Downloads -> HomeDownloadsScreen()
            PluviaDestination.Friends -> HomeFriendsScreen()
        }
    }
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
            onClickPlay = { appId, asContainer -> })
    }
}