package com.OxGames.Pluvia.ui.screen.settings

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.OxGames.Pluvia.BuildConfig
import com.OxGames.Pluvia.PrefManager
import com.OxGames.Pluvia.ui.component.dialog.OrientationDialog
import com.OxGames.Pluvia.ui.component.topbar.BackButton
import com.OxGames.Pluvia.ui.theme.PluviaTheme
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import kotlinx.coroutines.launch

// See link for implementation
// https://github.com/alorma/Compose-Settings

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
) {
    SettingsScreenContent(
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)
@Composable
private fun SettingsScreenContent(
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    BackButton {
                        onBack()
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            // Once there are enough settings, they should be split into
            // seperate (composable) classes, organized by group.

            SettingsGroup(
                title = { Text(text = "XServer") },
            ) {
                var orientationDialog by remember { mutableStateOf(false) }

                OrientationDialog(
                    openDialog = orientationDialog,
                    onDismiss = { orientationDialog = false },
                )

                SettingsMenuLink(
                    title = { Text(text = "Allowed Orientations") },
                    subtitle = { Text(text = "Choose which orientations XServer can rotate.") },
                    onClick = { orientationDialog = true },
                )
            }

            SettingsGroup(title = { Text(text = "Info") }) {
                val uriHandler = LocalUriHandler.current

                SettingsMenuLink(
                    title = { Text(text = "Source code") },
                    subtitle = { Text(text = "github.com/oxters168/Pluvia") },
                    onClick = {
                        uriHandler.openUri("https://github.com/oxters168/Pluvia")
                    },
                )

                SettingsMenuLink(
                    enabled = false,
                    title = { Text(text = "Technologies Used") },
                    subtitle = {
                        Column(
                            modifier = Modifier.padding(start = 12.dp),
                        ) {
                            Text(text = "JavaSteam - github.com/Longi94/JavaSteam")
                            Text(text = "Ubuntu RootFs - releases.ubuntu.com/focal")
                            Text(text = "Wine - winehq.org")
                            Text(text = "Box86/Box64 - box86.org")
                            Text(text = "PRoot - proot-me.github.io")
                            Text(text = "Mesa (Turnip/Zink/VirGL) - mesa3d.org")
                            Text(text = "DXVK - github.com/doitsujin/dxvk")
                            Text(text = "VKD3D - gitlab.winehq.org/wine/vkd3d")
                            Text(text = "D8VK - github.com/AlpyneDreams/d8vk")
                            Text(text = "CNC DDraw - github.com/FunkyFr3sh/cnc-ddraw")
                        }
                    },
                    onClick = {
                        /* Could link to pluvia repo to credits page? */
                    },
                )
            }

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
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview_SettingsScreen() {
    PluviaTheme {
        SettingsScreenContent(
            onBack = {},
        )
    }
}
