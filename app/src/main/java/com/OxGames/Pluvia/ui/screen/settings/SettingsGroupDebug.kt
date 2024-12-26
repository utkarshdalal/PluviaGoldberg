package com.OxGames.Pluvia.ui.screen.settings

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.OxGames.Pluvia.BuildConfig
import com.OxGames.Pluvia.PrefManager
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoilApi::class)
@Composable
fun SettingsGroupDebug() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (BuildConfig.DEBUG) {
        SettingsGroup(title = { Text(text = "Debug") }) {
            SettingsMenuLink(
                title = { Text(text = "Clear Preferences") },
                onClick = {
                    scope.launch {
                        PrefManager.clearPreferences()
                        (context as ComponentActivity).finishAffinity()
                    }
                },
            )

            SettingsMenuLink(
                title = { Text(text = "Clear Image Cache") },
                onClick = {
                    context.imageLoader.diskCache?.clear()
                    context.imageLoader.memoryCache?.clear()
                },
            )
        }
    }
}
