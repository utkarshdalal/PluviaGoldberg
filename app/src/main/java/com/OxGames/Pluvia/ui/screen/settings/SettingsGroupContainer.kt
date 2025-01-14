package com.OxGames.Pluvia.ui.screen.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.OxGames.Pluvia.ui.component.dialog.ContainerConfigDialog
import com.OxGames.Pluvia.utils.ContainerUtils

@Composable
fun SettingsGroupContainer() {
    var showConfigDialog by rememberSaveable { mutableStateOf(false) }

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

    SettingsGroup(title = { Text(text = "Container") }) {
        SettingsMenuLink(
            title = { Text(text = "Modify Default Config") },
            subtitle = { Text(text = "The initial container settings for each game (does not affect already installed games)") },
            onClick = { showConfigDialog = true },
        )
    }
}
