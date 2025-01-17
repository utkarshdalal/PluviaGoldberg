package com.OxGames.Pluvia.ui.component.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.winlator.core.envvars.EnvVarInfo
import com.winlator.core.envvars.EnvVarSelectionType
import com.winlator.core.envvars.EnvVars
import kotlin.text.split

@Composable
fun SettingsEnvVars(
    envVars: EnvVars,
    onEnvVarsChange: (EnvVars) -> Unit,
    knownEnvVars: Map<String, EnvVarInfo>,
    onEnvVarAction: ((String) -> Unit)? = null,
    envVarActionIcon: (@Composable () -> Unit)? = null,
) {
    for (identifier in envVars) {
        val value = envVars.get(identifier)
        val envVarInfo = knownEnvVars[identifier]
        val infoIcon: (@Composable () -> Unit)? = onEnvVarAction?.let {
            {
                IconButton(
                    onClick = { onEnvVarAction(identifier) },
                    content = envVarActionIcon ?: {},
                )
            }
        }
        when (envVarInfo?.selectionType ?: EnvVarSelectionType.NONE) {
            EnvVarSelectionType.TOGGLE -> {
                SettingsSwitchWithAction(
                    title = { Text(identifier) },
                    state = envVarInfo?.possibleValues?.indexOf(value) != 0,
                    onCheckedChange = {
                        val newValue = envVarInfo!!.possibleValues[if (it) 1 else 0]
                        envVars.put(identifier, newValue)
                        onEnvVarsChange(envVars)
                    },
                    action = infoIcon,
                )
            }
            EnvVarSelectionType.MULTI_SELECT -> {
                val values = value.split(",")
                    .map { envVarInfo!!.possibleValues.indexOf(it) }
                    .filter { it >= 0 && it < envVarInfo!!.possibleValues.size }
                SettingsMultiListDropdown(
                    title = { Text(identifier) },
                    values = values,
                    items = envVarInfo!!.possibleValues,
                    fallbackDisplay = value,
                    onItemSelected = { index ->
                        val newValues = if (values.contains(index)) {
                            values.filter { it != index }
                        } else {
                            values + index
                        }
                        envVars.put(
                            identifier,
                            newValues.map { envVarInfo.possibleValues[it] }.joinToString(",")
                        )
                        onEnvVarsChange(envVars)
                    },
                    action = infoIcon,
                )
            }
            EnvVarSelectionType.NONE -> {
                if (envVarInfo?.possibleValues?.isNotEmpty() == true) {
                    SettingsListDropdown(
                        title = { Text(identifier) },
                        value = envVarInfo.possibleValues.indexOf(value),
                        items = envVarInfo.possibleValues,
                        fallbackDisplay = value,
                        onItemSelected = {
                            envVars.put(identifier, envVarInfo.possibleValues[it])
                            onEnvVarsChange(envVars)
                        },
                        action = infoIcon,
                    )
                } else {
                    SettingsTextField(
                        title = { Text(identifier) },
                        value = value,
                        onValueChange = {
                            envVars.put(identifier, it)
                            onEnvVarsChange(envVars)
                        },
                        action = infoIcon,
                    )
                }
            }
        }
    }
}
