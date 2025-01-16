package com.OxGames.Pluvia.ui.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.OxGames.Pluvia.R
import com.OxGames.Pluvia.ui.component.dialog.state.MessageDialogState
import com.OxGames.Pluvia.ui.component.settings.SettingsListDropdown
import com.OxGames.Pluvia.ui.component.settings.SettingsMultiListDropdown
import com.OxGames.Pluvia.ui.component.settings.SettingsSwitchWithAction
import com.OxGames.Pluvia.ui.component.settings.SettingsTextField
import com.OxGames.Pluvia.utils.ContainerUtils
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import com.winlator.box86_64.Box86_64PresetManager
import com.winlator.container.ContainerData
import com.winlator.core.KeyValueSet
import com.winlator.core.StringUtils
import com.winlator.core.envvars.EnvVarInfo
import com.winlator.core.envvars.EnvVarSelectionType
import com.winlator.core.envvars.EnvVars
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerConfigDialog(
    visible: Boolean = true,
    title: String,
    initialConfig: ContainerData = ContainerData(),
    onDismissRequest: () -> Unit,
    onSave: (ContainerData) -> Unit,
) {
    if (visible) {
        val context = LocalContext.current

        var config by rememberSaveable(stateSaver = ContainerData.Saver) {
            mutableStateOf(initialConfig)
        }

        val screenSizes = stringArrayResource(R.array.screen_size_entries).toList()
        val graphicsDrivers = stringArrayResource(R.array.graphics_driver_entries).toList()
        val dxWrappers = stringArrayResource(R.array.dxwrapper_entries).toList()
        val audioDrivers = stringArrayResource(R.array.audio_driver_entries).toList()
        val gpuCards = ContainerUtils.getGPUCards(context)
        val renderingModes = stringArrayResource(R.array.offscreen_rendering_modes).toList()
        val videoMemSizes = stringArrayResource(R.array.video_memory_size_entries).toList()
        val mouseWarps = stringArrayResource(R.array.mouse_warp_override_entries).toList()
        val winCompOpts = stringArrayResource(R.array.win_component_entries).toList()
        val box64Versions = stringArrayResource(R.array.box64_version_entries).toList()
        val box64Presets = Box86_64PresetManager.getPresets("box64", context)

        var screenSizeIndex by rememberSaveable {
            val searchIndex = screenSizes.indexOfFirst { it.contains(config.screenSize) }
            mutableIntStateOf(if (searchIndex > 0) searchIndex else 0)
        }
        var customScreenWidth by rememberSaveable {
            val searchIndex = screenSizes.indexOfFirst { it.contains(config.screenSize) }
            mutableStateOf(if (searchIndex <= 0) config.screenSize.split("x")[0] else "")
        }
        var customScreenHeight by rememberSaveable {
            val searchIndex = screenSizes.indexOfFirst { it.contains(config.screenSize) }
            mutableStateOf(if (searchIndex <= 0) config.screenSize.split("x")[1] else "")
        }
        var graphicsDriverIndex by rememberSaveable {
            val driverIndex = graphicsDrivers.indexOfFirst { StringUtils.parseIdentifier(it) == config.graphicsDriver }
            mutableIntStateOf(if (driverIndex >= 0) driverIndex else 0)
        }
        var dxWrapperIndex by rememberSaveable {
            val driverIndex = dxWrappers.indexOfFirst { StringUtils.parseIdentifier(it) == config.dxwrapper }
            mutableIntStateOf(if (driverIndex >= 0) driverIndex else 0)
        }
        var audioDriverIndex by rememberSaveable {
            val driverIndex = audioDrivers.indexOfFirst { StringUtils.parseIdentifier(it) == config.audioDriver }
            mutableIntStateOf(if (driverIndex >= 0) driverIndex else 0)
        }
        var gpuNameIndex by rememberSaveable {
            val gpuInfoIndex = gpuCards.values.indexOfFirst { it.deviceId == config.videoPciDeviceID }
            mutableIntStateOf(if (gpuInfoIndex >= 0) gpuInfoIndex else 0)
        }
        var renderingModeIndex by rememberSaveable {
            val index = renderingModes.indexOfFirst { it.lowercase() == config.offScreenRenderingMode }
            mutableIntStateOf(if (index >= 0) index else 0)
        }
        var videoMemIndex by rememberSaveable {
            val index = videoMemSizes.indexOfFirst { StringUtils.parseNumber(it) == config.videoMemorySize }
            mutableIntStateOf(if (index >= 0) index else 0)
        }
        var mouseWarpIndex by rememberSaveable {
            val index = mouseWarps.indexOfFirst { it.lowercase() == config.mouseWarpOverride }
            mutableIntStateOf(if (index >= 0) index else 0)
        }

        var dismissDialogState by rememberSaveable(stateSaver = MessageDialogState.Saver) {
            mutableStateOf(MessageDialogState(visible = false))
        }

        val applyScreenSizeToConfig: () -> Unit = {
            val screenSize = if (screenSizeIndex == 0) {
                if (customScreenWidth.isNotEmpty() && customScreenHeight.isNotEmpty()) {
                    "${customScreenWidth}x${customScreenHeight}"
                } else {
                    config.screenSize
                }
            } else {
                screenSizes[screenSizeIndex].split(" ")[0]
            }
            config = config.copy(screenSize = screenSize)
        }

        val onDismissCheck: () -> Unit = {
            if (initialConfig != config) {
                dismissDialogState = MessageDialogState(
                    visible = true,
                    title = "Unsaved Changes",
                    message = "Are you sure you'd like to discard your changes?",
                    confirmBtnText = "Discard",
                    dismissBtnText = "Cancel",
                )
            } else {
                onDismissRequest()
            }
        }

        MessageDialog(
            visible = dismissDialogState.visible,
            title = dismissDialogState.title,
            message = dismissDialogState.message,
            confirmBtnText = dismissDialogState.confirmBtnText,
            dismissBtnText = dismissDialogState.dismissBtnText,
            onDismissRequest = { dismissDialogState = MessageDialogState(visible = false) },
            onDismissClick = { dismissDialogState = MessageDialogState(visible = false) },
            onConfirmClick = onDismissRequest,
        )

        Dialog(
            onDismissRequest = onDismissCheck,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside = false,
            ),
            content = {
                val scrollState = rememberScrollState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = "$title${if (initialConfig != config) "*" else ""}",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = onDismissCheck,
                                    content = { Icon(Icons.Default.Close, null) },
                                )
                            },
                            actions = {
                                IconButton(
                                    onClick = { onSave(config) },
                                    content = { Icon(Icons.Default.Save, null) },
                                )
                            },
                        )
                    },
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .verticalScroll(scrollState)
                            .fillMaxSize()
                            .padding(
                                top = WindowInsets.statusBars
                                    .asPaddingValues()
                                    .calculateTopPadding()
                            ),
                    ) {
                        SettingsGroup(title = { Text(text = "General") }) {
                            SettingsListDropdown(
                                title = { Text(text = "Screen Size") },
                                value = screenSizeIndex,
                                items = screenSizes,
                                onItemSelected = {
                                    screenSizeIndex = it
                                    applyScreenSizeToConfig()
                                },
                                action = if (screenSizeIndex == 0) {
                                    {
                                        Row {
                                            OutlinedTextField(
                                                modifier = Modifier.width(128.dp),
                                                value = customScreenWidth,
                                                onValueChange = {
                                                    customScreenWidth = it
                                                    applyScreenSizeToConfig()
                                                },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                label = { Text(text = "Width") },
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                modifier = Modifier.align(Alignment.CenterVertically),
                                                text = "x",
                                                style = TextStyle(fontSize = 16.sp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            OutlinedTextField(
                                                modifier = Modifier.width(128.dp),
                                                value = customScreenHeight,
                                                onValueChange = {
                                                    customScreenHeight = it
                                                    applyScreenSizeToConfig()
                                                },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                label = { Text(text = "Height") },
                                            )
                                        }
                                    }
                                } else {
                                    null
                                },
                            )
                            // TODO: add way to pick driver version
                            SettingsListDropdown(
                                title = { Text("Graphics Driver") },
                                value = graphicsDriverIndex,
                                items = graphicsDrivers,
                                onItemSelected = {
                                    graphicsDriverIndex = it
                                    config = config.copy(graphicsDriver = StringUtils.parseIdentifier(graphicsDrivers[it]))
                                },
                            )
                            // TODO: add way to pick DXVK version
                            SettingsListDropdown(
                                title = { Text("DX Wrapper") },
                                value = dxWrapperIndex,
                                items = dxWrappers,
                                onItemSelected = {
                                    dxWrapperIndex = it
                                    config = config.copy(dxwrapper = StringUtils.parseIdentifier(dxWrappers[it]))
                                },
                            )
                            // TODO: add way to configure audio driver
                            SettingsListDropdown(
                                title = { Text("Audio Driver") },
                                value = audioDriverIndex,
                                items = audioDrivers,
                                onItemSelected = {
                                    audioDriverIndex = it
                                    config = config.copy(audioDriver = StringUtils.parseIdentifier(audioDrivers[it]))
                                },
                            )
                            SettingsSwitch(
                                title = { Text("Show FPS") },
                                state = config.showFPS,
                                onCheckedChange = {
                                    config = config.copy(showFPS = it)
                                },
                            )
                        }
                        SettingsGroup(title = { Text(text = "Wine Configuration") }) {
                            // TODO: add desktop settings
                            SettingsListDropdown(
                                title = { Text("GPU Name") },
                                subtitle = { Text("WineD3D") },
                                value = gpuNameIndex,
                                items = gpuCards.values.map { it.name },
                                onItemSelected = {
                                    gpuNameIndex = it
                                    config = config.copy(videoPciDeviceID = gpuCards.values.toList()[it].deviceId)
                                },
                            )
                            SettingsListDropdown(
                                title = { Text("Offscreen Rendering Mode") },
                                subtitle = { Text("WineD3D") },
                                value = renderingModeIndex,
                                items = renderingModes,
                                onItemSelected = {
                                    renderingModeIndex = it
                                    config = config.copy(offScreenRenderingMode = renderingModes[it].lowercase())
                                }
                            )
                            SettingsListDropdown(
                                title = { Text("Video Memory Size") },
                                subtitle = { Text("WineD3D") },
                                value = videoMemIndex,
                                items = videoMemSizes,
                                onItemSelected = {
                                    videoMemIndex = it
                                    config = config.copy(videoMemorySize = StringUtils.parseNumber(videoMemSizes[it]))
                                }
                            )
                            SettingsSwitch(
                                title = { Text("Enable CSMT (Command Stream Multi-Thread)") },
                                subtitle = { Text("WineD3D") },
                                state = config.csmt,
                                onCheckedChange = {
                                    config = config.copy(csmt = it)
                                },
                            )
                            SettingsSwitch(
                                title = { Text("Enable Strict Shader Math") },
                                subtitle = { Text("WineD3D") },
                                state = config.strictShaderMath,
                                onCheckedChange = {
                                    config = config.copy(strictShaderMath = it)
                                },
                            )
                            SettingsListDropdown(
                                title = { Text("Mouse Warp Override") },
                                subtitle = { Text("DirectInput") },
                                value = mouseWarpIndex,
                                items = mouseWarps,
                                onItemSelected = {
                                    mouseWarpIndex = it
                                    config = config.copy(mouseWarpOverride = mouseWarps[it].lowercase())
                                }
                            )
                        }
                        SettingsGroup(title = { Text(text = "Win Components") }) {
                            for (wincomponent in KeyValueSet(config.wincomponents)) {
                                val compId = wincomponent[0]
                                val compName = StringUtils.getString(context, compId)
                                val compValue = wincomponent[1].toInt()
                                SettingsListDropdown(
                                    title = { Text(compName) },
                                    subtitle = { Text(if (compId.startsWith("direct")) "DirectX" else "General") },
                                    value = compValue,
                                    items = winCompOpts,
                                    onItemSelected = {
                                        config = config.copy(
                                            wincomponents = config.wincomponents.replace("$compId=$compValue", "$compId=$it")
                                        )
                                    }
                                )
                            }
                        }
                        SettingsGroup(title = { Text(text = "Environment Variables") }) {
                            val envVars = EnvVars(config.envVars)
                            for (identifier in envVars) {
                                val value = envVars.get(identifier)
                                val envVarInfo = EnvVarInfo.KNOWN_ENV_VARS[identifier]
                                val deleteIcon: @Composable () -> Unit = {
                                    IconButton(
                                        onClick = {
                                            envVars.remove(identifier)
                                            config = config.copy(
                                                envVars = envVars.toString(),
                                            )
                                        },
                                        content = { Icon(Icons.Filled.Delete, contentDescription = "Delete variable") },
                                    )
                                }
                                when (envVarInfo?.selectionType ?: EnvVarSelectionType.NONE) {
                                    EnvVarSelectionType.TOGGLE -> {
                                        SettingsSwitchWithAction(
                                            title = { Text(identifier) },
                                            state = envVarInfo?.possibleValues?.indexOf(value) != 0,
                                            onCheckedChange = {
                                                val newValue = envVarInfo!!.possibleValues[if (it) 1 else 0]
                                                envVars.put(identifier, newValue)
                                                config = config.copy(
                                                    envVars = envVars.toString()
                                                )
                                            },
                                            action = deleteIcon,
                                        )
                                    }
                                    EnvVarSelectionType.MULTI_SELECT -> {
                                        val values = value.split(",").map { envVarInfo!!.possibleValues.indexOf(it) }
                                        SettingsMultiListDropdown(
                                            title = { Text(identifier) },
                                            values = values,
                                            items = envVarInfo!!.possibleValues,
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
                                                config = config.copy(
                                                    envVars = envVars.toString()
                                                )
                                            },
                                            action = deleteIcon,
                                        )
                                    }
                                    EnvVarSelectionType.NONE -> {
                                        if (envVarInfo?.possibleValues?.isNotEmpty() == true) {
                                            SettingsListDropdown(
                                                title = { Text(identifier) },
                                                value = envVarInfo.possibleValues.indexOf(value),
                                                items = envVarInfo.possibleValues,
                                                onItemSelected = {
                                                    envVars.put(identifier, envVarInfo.possibleValues[it])
                                                    config = config.copy(
                                                        envVars = envVars.toString()
                                                    )
                                                },
                                                action = deleteIcon,
                                            )
                                        } else {
                                            SettingsTextField(
                                                title = { Text(identifier) },
                                                value = value,
                                                onValueChange = {
                                                    envVars.put(identifier, it)
                                                    config = config.copy(
                                                        envVars = envVars.toString()
                                                    )
                                                },
                                                action = deleteIcon,
                                            )
                                        }
                                    }
                                }
                            }
                            SettingsMenuLink(
                                title = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.AddCircleOutline,
                                            contentDescription = "Add environment variable",
                                        )
                                    }
                                },
                                onClick = {
                                    // TODO: add way to create new environment variable
                                },
                            )
                        }
                        SettingsGroup(title = { Text(text = "Drives") }) {
                            // TODO: make it possible to add and remove drives
                            SettingsMenuLink(
                                title = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.AddCircleOutline,
                                            contentDescription = "Add environment variable",
                                        )
                                    }
                                },
                                onClick = {
                                    // TODO: add way to create new drive
                                },
                            )
                        }
                        SettingsGroup(title = { Text(text = "Advanced") }) {
                            // TODO: add advanced configuration settings
                            Timber.d("Box64 versions: $box64Versions")
                            Timber.d("Container Box64 version: ${config.box64Version}")
                            Timber.d("Container Box64 preset: ${config.box64Preset}")
                            SettingsListDropdown(
                                title = { Text("Box64 Version") },
                                subtitle = { Text("Box64") },
                                value = box64Versions.indexOfFirst { StringUtils.parseIdentifier(it) == config.box64Version },
                                items = box64Versions,
                                onItemSelected = {
                                    config = config.copy(
                                        box64Version = StringUtils.parseIdentifier(box64Versions[it])
                                    )
                                },
                            )
                            SettingsListDropdown(
                                title = { Text("Box64 Preset") },
                                subtitle = { Text("Box64") },
                                value = box64Presets.indexOfFirst { it.id == config.box64Preset },
                                items = box64Presets.map { it.name },
                                onItemSelected = {
                                    config = config.copy(
                                        box64Preset = box64Presets[it].id
                                    )
                                },
                            )
                        }
                    }
                }
            },
        )
    }
}
