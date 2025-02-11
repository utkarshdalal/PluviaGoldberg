package com.OxGames.Pluvia.ui.screen.library.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.OxGames.Pluvia.service.SteamService
import com.OxGames.Pluvia.ui.screen.library.AppScreen
import com.OxGames.Pluvia.ui.theme.PluviaTheme

@Composable
internal fun LibraryDetailPane(
    appId: Int,
    onClickPlay: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    Surface {
        if (appId == SteamService.INVALID_APP_ID) {
            LibraryEmptyDetailPane()
        } else {
            AppScreen(
                appId = appId,
                onClickPlay = onClickPlay,
                onBack = onBack,
            )
        }
    }
}

@Composable
private fun LibraryEmptyDetailPane() {
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
}

/***********
 * PREVIEW *
 ***********/

@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES or android.content.res.Configuration.UI_MODE_TYPE_NORMAL)
@Preview
@Composable
private fun Preview_LibraryDetailPane() {
    PluviaTheme {
        LibraryDetailPane(
            appId = Int.MAX_VALUE,
            onClickPlay = { },
            onBack = { },
        )
    }
}
