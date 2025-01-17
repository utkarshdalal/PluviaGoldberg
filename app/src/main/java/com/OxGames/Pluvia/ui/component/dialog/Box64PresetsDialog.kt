package com.OxGames.Pluvia.ui.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.OxGames.Pluvia.ui.component.settings.SettingsEnvVars
import com.winlator.box86_64.Box86_64Preset
import com.winlator.box86_64.Box86_64PresetManager
import com.winlator.core.StringUtils
import com.winlator.core.envvars.EnvVarInfo
import com.winlator.core.envvars.EnvVars
import timber.log.Timber

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
            confirmButton = {
                TextButton(
                    onClick = { onDismissRequest() },
                    content = { Text("Done") }
                )
            },
            text = {
                val getPresets: () -> ArrayList<Box86_64Preset> = { Box86_64PresetManager.getPresets(prefix, context) }
                val getPreset: (String) -> Box86_64Preset = { id -> getPresets().first { it.id == id } }

                var showPresets by rememberSaveable { mutableStateOf(false) }
                var presetId by rememberSaveable { mutableStateOf(getPresets().first().id) }
                var presetName by rememberSaveable { mutableStateOf(getPreset(presetId).name) }
                var envVars by rememberSaveable {
                    mutableStateOf(Box86_64PresetManager.getEnvVars(
                        prefix,
                        context,
                        getPreset(presetId).id
                    ).toString())
                }

                val isCustom: () -> Boolean = { getPreset(presetId).isCustom }

                Column {
                    OutlinedTextField(
                        value = presetName,
                        enabled = isCustom(),
                        onValueChange = {
                            presetName = it.replace("|", "")
                            Box86_64PresetManager.editPreset(
                                prefix,
                                context,
                                presetId,
                                presetName,
                                EnvVars(envVars),
                            )
                        },
                        label = { Text("Preset name") },
                        trailingIcon = {
                            IconButton(
                                onClick = { showPresets = true },
                                content = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ViewList,
                                        contentDescription = "Preset list"
                                    )
                                },
                            )
                            DropdownMenu(
                                expanded = showPresets,
                                onDismissRequest = { showPresets = false },
                                content = {
                                    for (preset in getPresets()) {
                                        DropdownMenuItem(
                                            text = { Text(preset.name) },
                                            onClick = {
                                                presetId = preset.id
                                                presetName = getPreset(presetId).name
                                                envVars = Box86_64PresetManager.getEnvVars(
                                                        prefix,
                                                        context,
                                                        getPreset(presetId).id
                                                    ).toString()
                                                showPresets = false
                                            },
                                        )
                                    }
                                },
                            )
                        }
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Environment Variables")
                        Row {
                            IconButton(
                                onClick = {
                                    presetId = Box86_64PresetManager.duplicatePreset(
                                        prefix,
                                        context,
                                        presetId,
                                    )
                                    presetName = getPreset(presetId).name
                                    envVars = Box86_64PresetManager.getEnvVars(
                                        prefix,
                                        context,
                                        getPreset(presetId).id
                                    ).toString()
                                },
                                content = {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = "Duplicate preset",
                                    )
                                },
                            )
                            IconButton(
                                onClick = {
                                    val defaultEnvVars = EnvVarInfo.KNOWN_BOX64_VARS.values.map {
                                        "${it.identifier}=${it.possibleValues.first()}"
                                    }.joinToString(" ")
                                    presetId = Box86_64PresetManager.editPreset(
                                        prefix,
                                        context,
                                        null,
                                        "Unnamed",
                                        EnvVars(defaultEnvVars),
                                    )
                                    presetName = getPreset(presetId).name
                                    envVars = Box86_64PresetManager.getEnvVars(
                                        prefix,
                                        context,
                                        getPreset(presetId).id
                                    ).toString()
                                },
                                content = {
                                    Icon(
                                        imageVector = Icons.Outlined.AddCircle,
                                        contentDescription = "Create preset",
                                    )
                                },
                            )
                            IconButton(
                                enabled = isCustom(),
                                onClick = {
                                    val idToBeDeleted = presetId
                                    presetId = getPresets().first().id
                                    presetName = getPreset(presetId).name
                                    envVars = Box86_64PresetManager.getEnvVars(
                                        prefix,
                                        context,
                                        getPreset(presetId).id
                                    ).toString()
                                    Box86_64PresetManager.removePreset(prefix, context, idToBeDeleted)
                                },
                                content = {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete preset",
                                    )
                                },
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.verticalScroll(scrollState),
                    ) {
                        var infoMsg by rememberSaveable { mutableStateOf("") }

                        MessageDialog(
                            visible = infoMsg.isNotEmpty(),
                            onDismissRequest = { infoMsg = "" },
                            message = infoMsg,
                            useHtmlInMsg = true,
                        )
                        SettingsEnvVars(
                            enabled = isCustom(),
                            envVars = EnvVars(envVars),
                            onEnvVarsChange = {
                                envVars = it.toString()
                                Box86_64PresetManager.editPreset(
                                    prefix,
                                    context,
                                    presetId,
                                    presetName,
                                    it
                                )
                            },
                            knownEnvVars = EnvVarInfo.KNOWN_BOX64_VARS,
                            envVarAction = { varName ->
                                IconButton(
                                    onClick = {
                                        val resName = varName.replace(prefix.uppercase(), "box86_64_env_var_help_").lowercase()
                                        StringUtils.getString(context, resName)?.let {
                                            infoMsg = it
                                        } ?: Timber.w("Could not find string resource of $resName")
                                    },
                                    content = {
                                        Icon(Icons.Outlined.Info, contentDescription = "Variable info")
                                    },
                                )
                            },
                        )
                    }
                }
            },
        )
    }
}
