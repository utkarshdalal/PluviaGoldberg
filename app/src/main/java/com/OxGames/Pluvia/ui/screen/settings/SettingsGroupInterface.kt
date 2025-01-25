package com.OxGames.Pluvia.ui.screen.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.OxGames.Pluvia.enums.AppTheme
import com.OxGames.Pluvia.ui.component.dialog.AppThemeDialog
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink

@Composable
fun SettingsGroupInterface(
    appTheme: AppTheme,
    onAppTheme: (AppTheme) -> Unit,
) {
    var openAppThemeDialog by rememberSaveable { mutableStateOf(false) }

    AppThemeDialog(
        openDialog = openAppThemeDialog,
        appTheme = appTheme,
        onSelected = onAppTheme,
        onDismiss = {
            openAppThemeDialog = false
        },
    )

    SettingsGroup(title = { Text(text = "Interface") }) {
        SettingsMenuLink(
            title = { Text(text = "App Theme") },
            subtitle = { Text(text = "Choose between Day, Night, or Auto") },
            onClick = {
                openAppThemeDialog = true
            },
        )
    }
}
