package com.OxGames.Pluvia.ui.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.OxGames.Pluvia.R
import com.OxGames.Pluvia.ui.component.dialog.state.MessageDialogState
import com.OxGames.Pluvia.ui.component.settings.SettingsListDropdown
import com.alorma.compose.settings.ui.SettingsGroup
import com.winlator.container.ContainerData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerConfigDialog(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    visible: Boolean = true,
    title: String,
    initialConfig: ContainerData = ContainerData(),
    onDismissRequest: () -> Unit,
    onSave: (ContainerData) -> Unit,
) {
    if (visible) {
        var screenSizeIndex by rememberSaveable { mutableIntStateOf(0) }
        var customScreenWidth by rememberSaveable { mutableStateOf("") }
        var customScreenHeight by rememberSaveable { mutableStateOf("") }
        var config by rememberSaveable(stateSaver = ContainerData.Saver) {
            mutableStateOf(initialConfig)
        }

        var dismissDialogState by rememberSaveable(stateSaver = MessageDialogState.Saver) {
            mutableStateOf(MessageDialogState(visible = false))
        }

        val screenSizes = stringArrayResource(R.array.screen_size_entries).toList()

        LaunchedEffect(lifecycleOwner) {
            val searchIndex = screenSizes.indexOfFirst { it.contains(config.screenSize) }
            if (searchIndex > 0) {
                screenSizeIndex = searchIndex
            } else {
                val screenSizeSplit = config.screenSize.split("x")
                customScreenWidth = screenSizeSplit[0]
                customScreenHeight = screenSizeSplit[1]
            }
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
                        }
                        SettingsGroup(title = { Text(text = "Wine Configuration") }) {

                        }
                        SettingsGroup(title = { Text(text = "Win Components") }) {

                        }
                        SettingsGroup(title = { Text(text = "Environment Variables") }) {

                        }
                        SettingsGroup(title = { Text(text = "Drives") }) {

                        }
                        SettingsGroup(title = { Text(text = "Advanced") }) {

                        }
                    }
                }
            },
        )
    }
}
