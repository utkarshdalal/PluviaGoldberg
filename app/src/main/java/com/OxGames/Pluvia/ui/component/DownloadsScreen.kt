package com.OxGames.Pluvia.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LifecycleOwner
import com.OxGames.Pluvia.ui.theme.PluviaTheme

@Composable
fun DownloadsScreen(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    DownloadsScreenContent()
}

@Composable
private fun DownloadsScreenContent() {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Downloads View")
    }
}

@Preview
@Composable
private fun Preview_DownloadsScreen() {
    PluviaTheme {
        Surface {
            DownloadsScreenContent()
        }
    }
}