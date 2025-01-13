package com.OxGames.Pluvia.ui.screen.library

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import com.OxGames.Pluvia.PrefManager
import com.OxGames.Pluvia.R
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.data.AppInfo
import com.OxGames.Pluvia.ui.component.dialog.ContainerConfigDialog
import com.OxGames.Pluvia.ui.component.dialog.MessageDialog
import com.OxGames.Pluvia.ui.component.dialog.state.MessageDialogState
import com.OxGames.Pluvia.ui.component.topbar.BackButton
import com.OxGames.Pluvia.ui.data.AppMenuOption
import com.OxGames.Pluvia.ui.enums.AppOptionMenuType
import com.OxGames.Pluvia.ui.enums.DialogType
import com.OxGames.Pluvia.ui.theme.PluviaTheme
import com.OxGames.Pluvia.utils.StorageUtils
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import com.winlator.container.ContainerData
import com.winlator.container.ContainerManager
import com.winlator.core.WineThemeManager
import org.json.JSONObject
import timber.log.Timber

// https://partner.steamgames.com/doc/store/assets/libraryassets#4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    appId: Int,
    onClickPlay: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var downloadInfo by remember {
        mutableStateOf(SteamService.getAppDownloadInfo(appId))
    }
    var downloadProgress by remember { mutableFloatStateOf(downloadInfo?.getProgress() ?: 0f) }
    var isInstalled by remember { mutableStateOf(SteamService.isAppInstalled(appId)) }
    val isDownloading: () -> Boolean = { downloadInfo != null && downloadProgress < 1f }

    val appInfo by remember(appId) {
        mutableStateOf(SteamService.getAppInfoOf(appId))
    }

    var msgDialogState by rememberSaveable(stateSaver = MessageDialogState.Saver) {
        mutableStateOf(MessageDialogState(false))
    }

    var showConfigDialog by rememberSaveable { mutableStateOf(false) }

    var containerData by rememberSaveable(stateSaver = ContainerData.Saver) {
        mutableStateOf(ContainerData())
    }

    DisposableEffect(downloadInfo) {
        val onDownloadProgress: (Float) -> Unit = {
            if (it >= 1f) {
                isInstalled = SteamService.isAppInstalled(appId)
            }
            downloadProgress = it
        }

        downloadInfo?.addProgressListener(onDownloadProgress)

        onDispose {
            downloadInfo?.removeProgressListener(onDownloadProgress)
        }
    }

    val windowWidth = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass

    val onDismissRequest: (() -> Unit)?
    val onDismissClick: (() -> Unit)?
    val onConfirmClick: (() -> Unit)?
    when (msgDialogState.type) {
        DialogType.CANCEL_APP_DOWNLOAD -> {
            onConfirmClick = {
                downloadInfo?.cancel()
                SteamService.deleteApp(appId)
                downloadInfo = null
                downloadProgress = 0f
                isInstalled = SteamService.isAppInstalled(appId)
                msgDialogState = MessageDialogState(false)
            }
            onDismissRequest = { msgDialogState = MessageDialogState(false) }
            onDismissClick = { msgDialogState = MessageDialogState(false) }
        }
        DialogType.NOT_ENOUGH_SPACE -> {
            onDismissRequest = { msgDialogState = MessageDialogState(false) }
            onConfirmClick = { msgDialogState = MessageDialogState(false) }
            onDismissClick = null
        }
        DialogType.INSTALL_APP -> {
            onDismissRequest = { msgDialogState = MessageDialogState(false) }
            onConfirmClick = {
                downloadProgress = 0f
                downloadInfo = SteamService.downloadApp(appId)
                msgDialogState = MessageDialogState(false)
            }
            onDismissClick = { msgDialogState = MessageDialogState(false) }
        }
        DialogType.DELETE_APP -> {
            onConfirmClick = {
                SteamService.deleteApp(appId)
                msgDialogState = MessageDialogState(false)

                isInstalled = SteamService.isAppInstalled(appId)
            }
            onDismissRequest = { msgDialogState = MessageDialogState(false) }
            onDismissClick = { msgDialogState = MessageDialogState(false) }
        }
        else -> {
            onDismissRequest = null
            onDismissClick = null
            onConfirmClick = null
        }
    }

    Scaffold(
        topBar = {
            // Show Top App Bar when in Compact or Medium screen space.
            if (windowWidth == WindowWidthSizeClass.COMPACT || windowWidth == WindowWidthSizeClass.MEDIUM) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = appInfo?.name.orEmpty(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    navigationIcon = {
                        BackButton(onClick = onBack)
                    },
                )
            }
        },
    ) { paddingValues ->
        MessageDialog(
            visible = msgDialogState.visible,
            onDismissRequest = onDismissRequest,
            onConfirmClick = onConfirmClick,
            confirmBtnText = msgDialogState.confirmBtnText,
            onDismissClick = onDismissClick,
            dismissBtnText = msgDialogState.dismissBtnText,
            icon = msgDialogState.icon,
            title = msgDialogState.title,
            message = msgDialogState.message,
        )
        ContainerConfigDialog(
            visible = showConfigDialog,
            title = "${appInfo?.name} Config",
            initialConfig = containerData,
            onDismissRequest = { showConfigDialog = false },
            onSave = {
                showConfigDialog = false

                val containerManager = ContainerManager(context)
                if (containerManager.hasContainer(appId)) {
                    val container = containerManager.getContainerById(appId)
                    container.name = it.name
                    container.screenSize = it.screenSize
                    container.envVars = it.envVars
                    container.graphicsDriver = it.graphicsDriver
                    container.dxWrapper = it.dxwrapper
                    container.dxWrapperConfig = it.dxwrapperConfig
                    container.audioDriver = it.audioDriver
                    container.winComponents = it.wincomponents
                    container.drives = it.drives
                    container.isShowFPS = it.showFPS
                    container.cpuList = it.cpuList
                    container.cpuListWoW64 = it.cpuListWoW64
                    container.isWoW64Mode = it.wow64Mode
                    container.startupSelection = it.startupSelection
                    container.box86Preset = it.box86Preset
                    container.box64Preset = it.box64Preset
                    container.desktopTheme = it.desktopTheme
                    container.saveData()
                } else {
                    throw Exception("Container does not exist for $appId")
                }
            },
        )
        AppScreenContent(
            modifier = Modifier.padding(paddingValues),
            appInfo = appInfo,
            isInstalled = isInstalled,
            isDownloading = isDownloading(),
            downloadProgress = downloadProgress,
            onDownloadBtnClick = {
                if (isDownloading()) {
                    msgDialogState = MessageDialogState(
                        visible = true,
                        type = DialogType.CANCEL_APP_DOWNLOAD,
                        title = context.getString(R.string.cancel_download_prompt_title),
                        message = "Are you sure you want to cancel the download of the app?",
                        confirmBtnText = context.getString(R.string.yes),
                        dismissBtnText = context.getString(R.string.no),
                    )
                } else if (!isInstalled) {
                    val depots = SteamService.getDownloadableDepots(appId)
                    // TODO: get space available based on where user wants to install
                    val availableBytes = StorageUtils.getAvailableSpace(context.filesDir.absolutePath)
                    val availableSpace = StorageUtils.formatBinarySize(availableBytes)
                    // TODO: un-hardcode "public" branch
                    val downloadSize = StorageUtils.formatBinarySize(depots.values.map { it.manifests["public"]?.download ?: 0 }.sum())
                    val installBytes = depots.values.map { it.manifests["public"]?.size ?: 0 }.sum()
                    val installSize = StorageUtils.formatBinarySize(installBytes)
                    if (availableBytes < installBytes) {
                        msgDialogState = MessageDialogState(
                            visible = true,
                            type = DialogType.NOT_ENOUGH_SPACE,
                            title = context.getString(R.string.not_enough_space),
                            message = "The app being installed needs $installSize of space but " +
                                "there is only $availableSpace left on this device",
                            confirmBtnText = context.getString(R.string.acknowledge),
                        )
                    } else {
                        msgDialogState = MessageDialogState(
                            visible = true,
                            type = DialogType.INSTALL_APP,
                            title = context.getString(R.string.download_prompt_title),
                            message = "The app being installed has the following space requirements. Would you like to proceed?" +
                                "\n\n\tDownload Size: $downloadSize" +
                                "\n\tSize on Disk: $installSize" +
                                "\n\tAvailable Space: $availableSpace",
                            confirmBtnText = context.getString(R.string.proceed),
                            dismissBtnText = context.getString(R.string.cancel),
                        )
                    }
                } else {
                    onClickPlay(false)
                }
            },
            optionsMenu = arrayOf(
                AppMenuOption(
                    AppOptionMenuType.StorePage,
                    onClick = {
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://store.steampowered.com/app/$appId/"),
                        )
                        context.startActivity(browserIntent)
                    },
                ),
                *(
                    if (isInstalled) {
                        arrayOf(
                            AppMenuOption(
                                AppOptionMenuType.RunContainer,
                                onClick = {
                                    onClickPlay(true)
                                },
                            ),
                            AppMenuOption(
                                AppOptionMenuType.EditContainer,
                                onClick = {
                                    val containerManager = ContainerManager(context)
                                    containerData = if (containerManager.hasContainer(appId)) {
                                        val container = containerManager.getContainerById(appId)
                                        ContainerData(
                                            name = container.name,
                                            screenSize = container.screenSize,
                                            envVars = container.envVars,
                                            graphicsDriver = container.graphicsDriver,
                                            dxwrapper = container.dxWrapper,
                                            dxwrapperConfig = container.dxWrapperConfig,
                                            audioDriver = container.audioDriver,
                                            wincomponents = container.winComponents,
                                            drives = container.drives,
                                            showFPS = container.isShowFPS,
                                            cpuList = container.cpuList,
                                            cpuListWoW64 = container.cpuListWoW64,
                                            wow64Mode = container.isWoW64Mode,
                                            startupSelection = container.startupSelection.toByte(),
                                            box86Preset = container.box86Preset,
                                            box64Preset = container.box64Preset,
                                            desktopTheme = container.desktopTheme,
                                        )
                                    } else {
                                        // TODO: either change imagefs installation to the beginning of the
                                        // app to ensure this works properly, or only allow game config
                                        // editing when container exists
                                        // TODO: combine somehow with container creation in PluviaMain
                                        val data = JSONObject()
                                        data.put("name", "container_$appId")
                                        data.put("screenSize", PrefManager.screenSize)
                                        data.put("envVars", PrefManager.envVars)
                                        data.put("cpuList", PrefManager.cpuList)
                                        data.put("cpuListWoW64", PrefManager.cpuListWoW64)
                                        data.put("graphicsDriver", PrefManager.graphicsDriver)
                                        data.put("dxwrapper", PrefManager.dxWrapper)
                                        data.put("dxwrapperConfig", PrefManager.dxWrapperConfig)
                                        data.put("audioDriver", PrefManager.audioDriver)
                                        data.put("wincomponents", PrefManager.winComponents)
                                        data.put("drives", PrefManager.drives)
                                        data.put("showFPS", PrefManager.showFps)
                                        data.put("wow64Mode", PrefManager.wow64Mode)
                                        data.put("startupSelection", PrefManager.startupSelection)
                                        data.put("box86Preset", PrefManager.box86Preset)
                                        data.put("box64Preset", PrefManager.box64Preset)
                                        data.put("desktopTheme", WineThemeManager.DEFAULT_DESKTOP_THEME)
                                        val container = containerManager.createContainerFuture(appId, data).get()
                                        ContainerData(
                                            name = container.name,
                                            screenSize = container.screenSize,
                                            envVars = container.envVars,
                                            graphicsDriver = container.graphicsDriver,
                                            dxwrapper = container.dxWrapper,
                                            dxwrapperConfig = container.dxWrapperConfig,
                                            audioDriver = container.audioDriver,
                                            wincomponents = container.winComponents,
                                            drives = container.drives,
                                            showFPS = container.isShowFPS,
                                            cpuList = container.cpuList,
                                            cpuListWoW64 = container.cpuListWoW64,
                                            wow64Mode = container.isWoW64Mode,
                                            startupSelection = container.startupSelection.toByte(),
                                            box86Preset = container.box86Preset,
                                            box64Preset = container.box64Preset,
                                            desktopTheme = container.desktopTheme,
                                        )
                                    }

                                    showConfigDialog = true
                                },
                            ),
                            AppMenuOption(
                                AppOptionMenuType.Uninstall,
                                onClick = {
                                    val sizeOnDisk = StorageUtils.formatBinarySize(
                                        StorageUtils.getFolderSize(SteamService.getAppDirPath(appId)),
                                    )
                                    // TODO: show loading screen of delete progress
                                    msgDialogState = MessageDialogState(
                                        visible = true,
                                        type = DialogType.DELETE_APP,
                                        title = context.getString(R.string.delete_prompt_title),
                                        message = "Are you sure you want to delete this app?\n\n\tSize on Disk: $sizeOnDisk",
                                        confirmBtnText = context.getString(R.string.delete_app),
                                        dismissBtnText = context.getString(R.string.cancel),
                                    )
                                },
                            ),
                        )
                    } else {
                        emptyArray()
                    }
                    ),
            ),
        )
    }
}

@Composable
private fun AppScreenContent(
    modifier: Modifier = Modifier,
    appInfo: AppInfo?,
    isInstalled: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float,
    onDownloadBtnClick: () -> Unit,
    vararg optionsMenu: AppMenuOption,
) {
    val scrollState = rememberScrollState()

    var optionsMenuVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        Box {
            // Hero Logo
            CoilImage(
                modifier = Modifier.fillMaxWidth(),
                imageModel = { appInfo?.getHeroUrl() },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                ),
                loading = {
                    CircularProgressIndicator()
                },
                failure = {
                    Icon(Icons.Filled.QuestionMark, null)
                },
                previewPlaceholder = painterResource(R.drawable.testhero),
            )

            // Library Logo
            CoilImage(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 8.dp, bottom = 8.dp),
                imageModel = { appInfo?.getLogoUrl() },
                imageOptions = ImageOptions(
                    contentScale = FixedScale(1f),
                    requestSize = IntSize(640, 360),
                ),
                loading = {
                    CircularProgressIndicator()
                },
                failure = {
                    Icon(Icons.Filled.QuestionMark, null)
                },
                previewPlaceholder = painterResource(R.drawable.testliblogo),
            )
        }

        // Controls Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .wrapContentHeight(),
        ) {
            Button(
                shape = RoundedCornerShape(8.dp),
                onClick = onDownloadBtnClick,
            ) {
                Text(
                    if (isInstalled) {
                        stringResource(R.string.run_app)
                    } else if (isDownloading) {
                        stringResource(R.string.cancel)
                    } else {
                        stringResource(R.string.install_app)
                    },
                )
            }

            if (isDownloading) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f)
                        .padding(4.dp),
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.End),
                        text = "${(downloadProgress * 100f).toInt()}%",
                    )
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        progress = { downloadProgress },
                    )
                }
            } else {
                Spacer(Modifier.weight(1f))
            }

            Box {
                IconButton(
                    onClick = {
                        optionsMenuVisible = !optionsMenuVisible
                    },
                    content = {
                        Icon(Icons.Filled.MoreVert, "Options")
                    },
                )
                DropdownMenu(
                    expanded = optionsMenuVisible,
                    onDismissRequest = { optionsMenuVisible = false },
                ) {
                    optionsMenu.forEach { menuOption ->
                        DropdownMenuItem(
                            text = { Text(menuOption.optionType.text) },
                            onClick = {
                                menuOption.onClick()
                                optionsMenuVisible = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(
    device = "spec:width=1920px,height=1080px,dpi=440",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
) // Odin2 Mini
@Composable
private fun Preview_AppScreen() {
    val context = LocalContext.current
    val intent = Intent(context, SteamService::class.java)
    context.startForegroundService(intent)
    PluviaTheme {
        Surface {
            AppScreenContent(
                appInfo = null,
                isInstalled = false,
                isDownloading = true,
                downloadProgress = .50f,
                onDownloadBtnClick = { },
            )
        }
    }
}
