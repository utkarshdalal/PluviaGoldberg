package com.utkarshdalal.PluviaGoldberg.ui.screen.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.utkarshdalal.PluviaGoldberg.ui.component.dialog.Box64PresetsDialog
import com.utkarshdalal.PluviaGoldberg.ui.component.dialog.ContainerConfigDialog
import com.utkarshdalal.PluviaGoldberg.ui.component.dialog.OrientationDialog
import com.utkarshdalal.PluviaGoldberg.ui.theme.settingsTileColors
import com.utkarshdalal.PluviaGoldberg.utils.ContainerUtils
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink

@Composable
fun SettingsGroupEmulation() {
    SettingsGroup(title = { Text(text = "Emulation") }) {
        var showConfigDialog by rememberSaveable { mutableStateOf(false) }
        var showOrientationDialog by rememberSaveable { mutableStateOf(false) }
        var showBox64PresetsDialog by rememberSaveable { mutableStateOf(false) }

        OrientationDialog(
            openDialog = showOrientationDialog,
            onDismiss = { showOrientationDialog = false },
        )

        ContainerConfigDialog(
            visible = showConfigDialog,
            title = "Default Container Config",
            initialConfig = ContainerUtils.getDefaultContainerData(),
            onDismissRequest = { showConfigDialog = false },
            onSave = {
                showConfigDialog = false
                ContainerUtils.setDefaultContainerData(it)
            },
        )

        Box64PresetsDialog(
            visible = showBox64PresetsDialog,
            onDismissRequest = { showBox64PresetsDialog = false },
        )

        SettingsMenuLink(
            colors = settingsTileColors(),
            title = { Text(text = "Allowed Orientations") },
            subtitle = { Text(text = "Choose which orientations can be rotated to when in-game") },
            onClick = { showOrientationDialog = true },
        )
        SettingsMenuLink(
            colors = settingsTileColors(),
            title = { Text(text = "Modify Default Config") },
            subtitle = { Text(text = "The initial container settings for each game (does not affect already installed games)") },
            onClick = { showConfigDialog = true },
        )
        SettingsMenuLink(
            colors = settingsTileColors(),
            title = { Text(text = "Box64 Presets") },
            subtitle = { Text("View, modify, and create Box64 presets") },
            onClick = { showBox64PresetsDialog = true },
        )
    }
}
