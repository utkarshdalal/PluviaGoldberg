package com.OxGames.Pluvia.ui.screen.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.OxGames.Pluvia.PrefManager
import com.OxGames.Pluvia.enums.AppTheme
import com.OxGames.Pluvia.ui.component.dialog.AppPaletteDialog
import com.OxGames.Pluvia.ui.component.dialog.AppThemeDialog
import com.OxGames.Pluvia.ui.component.dialog.StartScreenDialog
import com.OxGames.Pluvia.ui.theme.settingsTileColors
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.materialkolor.PaletteStyle

@Composable
fun SettingsGroupInterface(
    appTheme: AppTheme,
    paletteStyle: PaletteStyle,
    onAppTheme: (AppTheme) -> Unit,
    onPaletteStyle: (PaletteStyle) -> Unit,
) {
    var openAppThemeDialog by rememberSaveable { mutableStateOf(false) }
    var openAppPaletteDialog by rememberSaveable { mutableStateOf(false) }
    var openStartScreenDialog by rememberSaveable { mutableStateOf(false) }
    var startScreenOption by rememberSaveable { mutableStateOf(PrefManager.startScreen) }

    AppThemeDialog(
        openDialog = openAppThemeDialog,
        appTheme = appTheme,
        onSelected = onAppTheme,
        onDismiss = {
            openAppThemeDialog = false
        },
    )

    AppPaletteDialog(
        openDialog = openAppPaletteDialog,
        paletteStyle = paletteStyle,
        onSelected = onPaletteStyle,
        onDismiss = {
            openAppPaletteDialog = false
        },
    )

    StartScreenDialog(
        openDialog = openStartScreenDialog,
        destination = startScreenOption,
        onSelected = {
            startScreenOption = it
            PrefManager.startScreen = it
        },
        onDismiss = {
            openStartScreenDialog = false
        },
    )

    SettingsGroup(title = { Text(text = "Interface") }) {
        SettingsMenuLink(
            colors = settingsTileColors(),
            title = { Text(text = "Start Destination") },
            subtitle = { Text(text = "Choose between Library, Downloads, Friends") },
            onClick = {
                openStartScreenDialog = true
            },
        )
        SettingsMenuLink(
            colors = settingsTileColors(),
            title = { Text(text = "App Theme") },
            subtitle = { Text(text = "Choose between Day, Night, or Auto") },
            onClick = {
                openAppThemeDialog = true
            },
        )
        SettingsMenuLink(
            colors = settingsTileColors(),
            title = { Text(text = "Palette Style") },
            subtitle = { Text(text = "Change the Material Design 3 color palette") },
            onClick = {
                openAppPaletteDialog = true
            },
        )
    }
}
