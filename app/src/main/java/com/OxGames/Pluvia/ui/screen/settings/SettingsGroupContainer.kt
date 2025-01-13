package com.OxGames.Pluvia.ui.screen.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.OxGames.Pluvia.PrefManager
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.OxGames.Pluvia.ui.component.dialog.ContainerConfigDialog
import com.winlator.container.ContainerData

@Composable
fun SettingsGroupContainer() {
    var showConfigDialog by rememberSaveable { mutableStateOf(false) }

    ContainerConfigDialog(
        visible = showConfigDialog,
        title = "Default Container Config",
        initialConfig = ContainerData(
            screenSize = PrefManager.screenSize,
            envVars = PrefManager.envVars,
            graphicsDriver = PrefManager.graphicsDriver,
            dxwrapper = PrefManager.dxWrapper,
            dxwrapperConfig = PrefManager.dxWrapperConfig,
            audioDriver = PrefManager.audioDriver,
            wincomponents = PrefManager.winComponents,
            drives = PrefManager.drives,
            showFPS = PrefManager.showFps,
            cpuList = PrefManager.cpuList,
            cpuListWoW64 = PrefManager.cpuListWoW64,
            wow64Mode = PrefManager.wow64Mode,
            startupSelection = PrefManager.startupSelection.toByte(),
            box86Preset = PrefManager.box86Preset,
            box64Preset = PrefManager.box64Preset,
        ),
        onDismissRequest = { showConfigDialog = false },
        onSave = {
            showConfigDialog = false

            PrefManager.screenSize = it.screenSize
            PrefManager.envVars = it.envVars
            PrefManager.graphicsDriver = it.graphicsDriver
            PrefManager.dxWrapper = it.dxwrapper
            PrefManager.dxWrapperConfig = it.dxwrapperConfig
            PrefManager.audioDriver = it.audioDriver
            PrefManager.winComponents = it.wincomponents
            PrefManager.drives = it.drives
            PrefManager.showFps = it.showFPS
            PrefManager.cpuList = it.cpuList
            PrefManager.cpuListWoW64 = it.cpuListWoW64
            PrefManager.wow64Mode = it.wow64Mode
            PrefManager.startupSelection = it.startupSelection.toInt()
            PrefManager.box86Preset = it.box86Preset
            PrefManager.box64Preset = it.box64Preset
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
