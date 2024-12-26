package com.OxGames.Pluvia.ui.screen.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.OxGames.Pluvia.ui.component.dialog.OrientationDialog
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink

@Composable
fun SettingsGroupXServer() {
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
}
