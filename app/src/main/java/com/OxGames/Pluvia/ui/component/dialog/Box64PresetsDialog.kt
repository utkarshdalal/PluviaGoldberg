package com.OxGames.Pluvia.ui.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.OxGames.Pluvia.ui.component.settings.SettingsEnvVars
import com.winlator.box86_64.Box86_64PresetManager
import com.winlator.core.envvars.EnvVarInfo

@Composable
fun Box64PresetsDialog(
    visible: Boolean = true,
    onDismissRequest: () -> Unit,
) {
    if (visible) {
        val context = LocalContext.current
        val prefix = "box64"
        val scrollState = rememberScrollState()

        AlertDialog(
            title = { Text("Box64 Presets") },
            onDismissRequest = onDismissRequest,
            dismissButton = {
                TextButton(
                    onClick = onDismissRequest,
                    content = { Text("Cancel") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    },
                    content = { Text("OK") }
                )
            },
            text = {
                val presets = Box86_64PresetManager.getPresets(prefix, context)
                var presetIndex by rememberSaveable { mutableIntStateOf(0) }

                val isCustom = presets[presetIndex].isCustom
                val envVars = Box86_64PresetManager.getEnvVars(prefix, context, presets[presetIndex].id)

                Column {
                    OutlinedTextField(
                        value = presets[presetIndex].name,
                        onValueChange = {},
                        label = { Text("Preset name") },
                        trailingIcon = {
                            IconButton(
                                onClick = {},
                                content = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ViewList,
                                        contentDescription = "Preset list"
                                    )
                                },
                            )
                        }
                    )
                    // SettingsGroup(title = { Text("Environment Variables") }) {
                    Text("Environment Variables")

                    Column(
                        modifier = Modifier.fillMaxSize()
                            .verticalScroll(scrollState),
                    ) {
                        SettingsEnvVars(
                            envVars = envVars,
                            onEnvVarsChange = {
                                // Box86_64PresetManager.getNextPresetId(context, prefix)
                                // Box86_64PresetManager.editPreset(prefix, context, presets[presetIndex].id, presets[presetIndex].name, it)
                            },
                            knownEnvVars = EnvVarInfo.KNOWN_BOX64_VARS,
                            onEnvVarAction = {

                            },
                            envVarActionIcon = {
                                Icon(Icons.Outlined.Info, contentDescription = "Variable info")
                            },
                        )
                    }
                    // }
                }
            },
        )
    }
}
