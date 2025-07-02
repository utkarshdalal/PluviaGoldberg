package app.gamenative.ui.screen.library

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import app.gamenative.Constants
import app.gamenative.R
import app.gamenative.data.SteamApp
import app.gamenative.service.SteamService
import app.gamenative.ui.component.LoadingScreen
import app.gamenative.ui.component.dialog.ContainerConfigDialog
import app.gamenative.ui.component.dialog.LoadingDialog
import app.gamenative.ui.component.dialog.MessageDialog
import app.gamenative.ui.component.dialog.state.MessageDialogState
import app.gamenative.ui.component.topbar.BackButton
import app.gamenative.ui.data.AppMenuOption
import app.gamenative.ui.enums.AppOptionMenuType
import app.gamenative.ui.enums.DialogType
import app.gamenative.ui.internal.fakeAppInfo
import app.gamenative.ui.theme.PluviaTheme
import app.gamenative.utils.ContainerUtils
import app.gamenative.utils.StorageUtils
import com.google.android.play.core.splitcompat.SplitCompat
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import app.gamenative.utils.SteamUtils
import com.winlator.container.ContainerData
import com.winlator.xenvironment.ImageFsInstaller
import java.text.SimpleDateFormat
import java.util.Date
import java.util.EnumSet
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import app.gamenative.PluviaApp
import app.gamenative.ui.enums.Orientation
import app.gamenative.events.AndroidEvent
import app.gamenative.service.SteamService.Companion.DOWNLOAD_COMPLETE_MARKER
import app.gamenative.service.SteamService.Companion.getAppDirPath
import com.posthog.PostHog
import java.io.File
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import app.gamenative.PrefManager

// https://partner.steamgames.com/doc/store/assets/libraryassets#4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    appId: Int,
    onClickPlay: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    // Force portrait orientation for this screen
    LaunchedEffect(Unit) {
        PluviaApp.events.emit(AndroidEvent.SetAllowedOrientation(EnumSet.of(Orientation.PORTRAIT)))
    }

    val appInfo by remember(appId) {
        mutableStateOf(SteamService.getAppInfoOf(appId)!!)
    }

    var downloadInfo by remember(appId) {
        mutableStateOf(SteamService.getAppDownloadInfo(appId))
    }
    var downloadProgress by remember(appId) {
        mutableFloatStateOf(downloadInfo?.getProgress() ?: 0f)
    }
    var isInstalled by remember(appId) {
        mutableStateOf(SteamService.isAppInstalled(appId))
    }

    val isValidToDownload by remember(appId) {
        mutableStateOf(appInfo.branches.isNotEmpty() && appInfo.depots.isNotEmpty())
    }

    val isDownloading: () -> Boolean = { downloadInfo != null && downloadProgress < 1f }

    var loadingDialogVisible by rememberSaveable { mutableStateOf(false) }
    var loadingProgress by rememberSaveable { mutableFloatStateOf(0f) }

    var msgDialogState by rememberSaveable(stateSaver = MessageDialogState.Saver) {
        mutableStateOf(MessageDialogState(false))
    }

    var showConfigDialog by rememberSaveable { mutableStateOf(false) }

    var containerData by rememberSaveable(stateSaver = ContainerData.Saver) {
        mutableStateOf(ContainerData())
    }

    val showEditConfigDialog: () -> Unit = {
        val container = ContainerUtils.getOrCreateContainer(context, appId)
        containerData = ContainerUtils.toContainerData(container)
        showConfigDialog = true
    }

    DisposableEffect(downloadInfo) {
        val onDownloadProgress: (Float) -> Unit = {
            if (it >= 1f) {
                isInstalled = SteamService.isAppInstalled(appId)
                downloadInfo = null
                isInstalled = true
                try {
                    val dir = File(getAppDirPath(appId))
                    dir.mkdirs()
                    File(dir, DOWNLOAD_COMPLETE_MARKER).createNewFile()
                    Timber.i("Wrote download complete marker for $appId at $dir")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to write download complete marker for $appId")
                }
            }
            downloadProgress = it
        }

        downloadInfo?.addProgressListener(onDownloadProgress)

        onDispose {
            downloadInfo?.removeProgressListener(onDownloadProgress)
        }
    }

    LaunchedEffect(appId) {
        Timber.d("Selected app $appId")
    }

    val windowWidth = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass

    val onDismissRequest: (() -> Unit)?
    val onDismissClick: (() -> Unit)?
    val onConfirmClick: (() -> Unit)?
    when (msgDialogState.type) {
        DialogType.CANCEL_APP_DOWNLOAD -> {
            onConfirmClick = {
                PostHog.capture(event = "game_install_cancelled",
                    properties = mapOf(
                        "game_name" to appInfo.name
                    ))
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
                PostHog.capture(event = "game_install_started",
                    properties = mapOf(
                        "game_name" to appInfo.name
                    ))
                CoroutineScope(Dispatchers.IO).launch {
                    downloadProgress = 0f
                    downloadInfo = SteamService.downloadApp(appId)
                    msgDialogState = MessageDialogState(false)
                }
            }
            onDismissClick = { msgDialogState = MessageDialogState(false) }
        }

        DialogType.DELETE_APP -> {
            onConfirmClick = {
                // Delete the Steam app data
                SteamService.deleteApp(appId)
                // Also delete the associated container so it will be recreated on next launch
                ContainerUtils.deleteContainer(context, appId)
                msgDialogState = MessageDialogState(false)

                isInstalled = SteamService.isAppInstalled(appId)
            }
            onDismissRequest = { msgDialogState = MessageDialogState(false) }
            onDismissClick = { msgDialogState = MessageDialogState(false) }
        }

        DialogType.INSTALL_IMAGEFS -> {
            onDismissRequest = { msgDialogState = MessageDialogState(false) }
            onDismissClick = { msgDialogState = MessageDialogState(false) }
            onConfirmClick = {
                loadingDialogVisible = true
                msgDialogState = MessageDialogState(false)
                CoroutineScope(Dispatchers.IO).launch {
                    if (!SteamService.isImageFsInstallable(context)) {
                        SteamService.downloadImageFs(
                            onDownloadProgress = { loadingProgress = it },
                            this,
                        ).await()
                    }
                    if (!SteamService.isImageFsInstalled(context)) {
                        SplitCompat.install(context)
                        ImageFsInstaller.installIfNeededFuture(context, context.assets) {
                            // Log.d("XServerScreen", "$progress")
                            loadingProgress = it / 100f
                        }.get()
                    }
                    loadingDialogVisible = false
                    showEditConfigDialog()
                }
            }
        }

        else -> {
            onDismissRequest = null
            onDismissClick = null
            onConfirmClick = null
        }
    }

    MessageDialog(
        visible = msgDialogState.visible,
        onDismissRequest = onDismissRequest,
        onConfirmClick = onConfirmClick,
        confirmBtnText = msgDialogState.confirmBtnText,
        onDismissClick = onDismissClick,
        dismissBtnText = msgDialogState.dismissBtnText,
        icon = msgDialogState.type.icon,
        title = msgDialogState.title,
        message = msgDialogState.message,
    )

    ContainerConfigDialog(
        visible = showConfigDialog,
        title = "${appInfo.name} Config",
        initialConfig = containerData,
        onDismissRequest = { showConfigDialog = false },
        onSave = {
            showConfigDialog = false
            ContainerUtils.applyToContainer(context, appId, it)
        },
    )

    LoadingDialog(
        visible = loadingDialogVisible,
        progress = loadingProgress,
    )

    Scaffold {
        AppScreenContent(
            modifier = Modifier.padding(it),
            appInfo = appInfo,
            isInstalled = isInstalled,
            isValidToDownload = isValidToDownload,
            isDownloading = isDownloading(),
            downloadProgress = downloadProgress,
            onDownloadInstallClick = {
                if (isDownloading()) {
                    // Prompt to cancel ongoing download
                    msgDialogState = MessageDialogState(
                        visible = true,
                        type = DialogType.CANCEL_APP_DOWNLOAD,
                        title = context.getString(R.string.cancel_download_prompt_title),
                        message = "Are you sure you want to cancel the download of the app?",
                        confirmBtnText = context.getString(R.string.yes),
                        dismissBtnText = context.getString(R.string.no),
                    )
                } else if (SteamService.hasPartialDownload(appId)) {
                    // Resume incomplete download
                    CoroutineScope(Dispatchers.IO).launch {
                        downloadInfo = SteamService.downloadApp(appId)
                    }
                } else if (!isInstalled) {
                    // New install: check available space
                    val depots = SteamService.getDownloadableDepots(appInfo.id)
                    Timber.d("There are ${depots.size} depots belonging to $appId")
                    val availableBytes = StorageUtils.getAvailableSpace(context.filesDir.absolutePath)
                    val availableSpace = StorageUtils.formatBinarySize(availableBytes)
                    val downloadSize = StorageUtils.formatBinarySize(
                        depots.values.sumOf { it.manifests["public"]?.download ?: 0 }
                    )
                    val installBytes = depots.values.sumOf { it.manifests["public"]?.size ?: 0 }
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
                    // Already installed: launch app
                    PostHog.capture(event = "game_launched",
                        properties = mapOf(
                            "game_name" to appInfo.name
                        ))
                    onClickPlay(false)
                }
            },
            onPauseResumeClick = {
                if (isDownloading()) {
                    downloadInfo?.cancel()
                    downloadInfo = null
                } else {
                    downloadInfo = SteamService.downloadApp(appId)
                }
            },
            onDeleteDownloadClick = {
                msgDialogState = MessageDialogState(
                    visible = true,
                    type = DialogType.CANCEL_APP_DOWNLOAD,
                    title = context.getString(R.string.cancel_download_prompt_title),
                    message = "Delete all downloaded data for this game?",
                    confirmBtnText = context.getString(R.string.yes),
                    dismissBtnText = context.getString(R.string.no)
                )
            },
            onBack = onBack,
            optionsMenu = arrayOf(
                AppMenuOption(
                    optionType = AppOptionMenuType.StorePage,
                    onClick = {
                        // TODO add option to view web page externally or internally
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            (Constants.Library.STORE_URL + appInfo.id).toUri(),
                        )
                        context.startActivity(browserIntent)
                    },
                ),
                AppMenuOption(
                    optionType = AppOptionMenuType.EditContainer,
                    onClick = {
                        if (!SteamService.isImageFsInstalled(context)) {
                            if (!SteamService.isImageFsInstallable(context)) {
                                msgDialogState = MessageDialogState(
                                    visible = true,
                                    type = DialogType.INSTALL_IMAGEFS,
                                    title = "Download & Install ImageFS",
                                    message = "The Ubuntu image needs to be downloaded and installed before " +
                                        "being able to edit the configuration. This operation might take " +
                                        "a few minutes. Would you like to continue?",
                                    confirmBtnText = "Proceed",
                                    dismissBtnText = "Cancel",
                                )
                            } else {
                                msgDialogState = MessageDialogState(
                                    visible = true,
                                    type = DialogType.INSTALL_IMAGEFS,
                                    title = "Install ImageFS",
                                    message = "The Ubuntu image needs to be installed before being able to edit " +
                                        "the configuration. This operation might take a few minutes. " +
                                        "Would you like to continue?",
                                    confirmBtnText = "Proceed",
                                    dismissBtnText = "Cancel",
                                )
                            }
                        } else {
                            showEditConfigDialog()
                        }
                    },
                ),
                *(
                    if (isInstalled) {
                        arrayOf(
                            AppMenuOption(
                                AppOptionMenuType.RunContainer,
                                onClick = {
                                    PostHog.capture(event = "container_opened",
                                        properties = mapOf(
                                            "game_name" to appInfo.name
                                        )
                                    )
                                    onClickPlay(true)
                                },
                            ),
                            AppMenuOption(
                                AppOptionMenuType.ResetDrm,
                                onClick = {
                                    val container = ContainerUtils.getOrCreateContainer(context, appId)
                                    container.isNeedsUnpacking = true
                                    container.saveData()
                                },
                            ),
                            AppMenuOption(
                                AppOptionMenuType.Uninstall,
                                onClick = {
                                    val sizeOnDisk = StorageUtils.formatBinarySize(
                                        StorageUtils.getFolderSize(SteamService.getAppDirPath(appInfo.id)),
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
    appInfo: SteamApp,
    isInstalled: Boolean,
    isValidToDownload: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float,
    onDownloadInstallClick: () -> Unit,
    onPauseResumeClick: () -> Unit,
    onDeleteDownloadClick: () -> Unit,
    onBack: () -> Unit = {},
    vararg optionsMenu: AppMenuOption,
) {
    // Determine Wi-Fi connectivity for 'Wi-Fi only' preference
    val context = LocalContext.current
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    val wifiConnected = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    val wifiAllowed = !PrefManager.downloadOnWifiOnly || wifiConnected
    val scrollState = rememberScrollState()

    var optionsMenuVisible by remember { mutableStateOf(false) }

    // Compute last played timestamp from local install folder
    val lastPlayedText by remember(appInfo.id, isInstalled) {
        mutableStateOf(
            if (isInstalled) {
                val path = SteamService.getAppDirPath(appInfo.id)
                val file = java.io.File(path)
                if (file.exists()) {
                    SteamUtils.fromSteamTime((file.lastModified() / 1000).toInt())
                } else {
                    "Never"
                }
            } else {
                "Never"
            }
        )
    }
    // Compute real playtime by fetching owned games
    var playtimeText by remember { mutableStateOf("0 hrs") }
    LaunchedEffect(appInfo.id) {
        val steamID = SteamService.userSteamId?.accountID?.toLong()
        if (steamID != null) {
            val games = SteamService.getOwnedGames(steamID)
            val game = games.firstOrNull { it.appId == appInfo.id }
            playtimeText = if (game != null) {
                SteamUtils.formatPlayTime(game.playtimeForever) + " hrs"
            } else "0 hrs"
        }
    }

    LaunchedEffect(appInfo.id) {
        scrollState.animateScrollTo(0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start,
    ) {
        // Hero Section with Game Image Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            // Hero background image
            CoilImage(
                modifier = Modifier.fillMaxSize(),
                imageModel = { appInfo.getHeroUrl() },
                imageOptions = ImageOptions(contentScale = ContentScale.Crop),
                loading = { LoadingScreen() },
                failure = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        // Gradient background as fallback
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary
                        ) { }
                    }
                },
                previewPlaceholder = painterResource(R.drawable.testhero),
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            // Back button (top left)
            Box(
                modifier = Modifier
                    .padding(20.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                BackButton(onClick = onBack)
            }

            // Settings/options button (top right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
            ) {
                IconButton(
                    modifier = Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    onClick = { optionsMenuVisible = !optionsMenuVisible },
                    content = {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
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

            // Game title and subtitle
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = appInfo.name,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = Offset(0f, 2f),
                            blurRadius = 10f
                        )
                    ),
                    color = Color.White
                )

                Text(
                    text = "${appInfo.developer} • ${remember(appInfo.releaseDate) {
                        SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(appInfo.releaseDate * 1000))
                    }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // Content section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pause/Resume and Delete when downloading or paused
                // Determine if there's a partial download (in-session or from ungraceful close)
                val isPartiallyDownloaded = (downloadProgress > 0f && downloadProgress < 1f) || SteamService.hasPartialDownload(appInfo.id)
                // Disable resume when Wi-Fi only is enabled and there's no Wi-Fi
                val isResume = !isDownloading && isPartiallyDownloaded
                val pauseResumeEnabled = if (isResume) wifiAllowed else true
                if (isDownloading || isPartiallyDownloaded) {
                    // Pause or Resume
                    Button(
                        enabled = pauseResumeEnabled,
                        modifier = Modifier.weight(1f),
                        onClick = onPauseResumeClick,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text(
                            text = if (isDownloading) stringResource(R.string.pause_download)
                                   else stringResource(R.string.resume_download),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    // Delete (Cancel) download data
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDeleteDownloadClick,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text(stringResource(R.string.delete_app), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    }
                } else {
                    // Disable install when Wi-Fi only is enabled and there's no Wi-Fi
                    val isInstall = !isInstalled
                    val installEnabled = if (isInstall) wifiAllowed && hasInternet else true
                    // Install or Play button
                    Button(
                        enabled = installEnabled && isValidToDownload,
                        modifier = Modifier.weight(1f),
                        onClick = onDownloadInstallClick,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        val text = when {
                            isInstalled -> stringResource(R.string.run_app)
                            !hasInternet -> "Need internet to install"
                            !wifiConnected && PrefManager.downloadOnWifiOnly -> "Install over WiFi only enabled"
                            else -> stringResource(R.string.install_app)
                        }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    // Uninstall if already installed
                    if (isInstalled) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { optionsMenu.find { it.optionType == AppOptionMenuType.Uninstall }?.onClick?.invoke() },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.uninstall),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Download progress section
            if (isDownloading) {
                // Track download start time and estimate remaining time
                var downloadStartTime by remember { mutableStateOf<Long?>(null) }
                LaunchedEffect(downloadProgress) {
                    if (downloadProgress > 0f && downloadStartTime == null) {
                        downloadStartTime = System.currentTimeMillis()
                    }
                }
                val timeLeftText = remember(downloadProgress, downloadStartTime) {
                    if (downloadProgress in 0f..1f && downloadStartTime != null && downloadProgress < 1f) {
                        val elapsed = System.currentTimeMillis() - downloadStartTime!!
                        val totalEst = (elapsed / downloadProgress).toLong()
                        val remaining = totalEst - elapsed
                        val secondsLeft = remaining / 1000
                        val minutesLeft = secondsLeft / 60
                        val secondsPart = secondsLeft % 60
                        "${minutesLeft}m ${secondsPart}s left"
                    } else {
                        "Calculating..."
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Installation Progress",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${(downloadProgress * 100f).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.tertiary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // This is placeholder text since we don't have exact size info in the state
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Downloading...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = timeLeftText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Game information card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Colored top border
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            )
                    )

                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Game Information",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            // Setting a fixed height to avoid nested scrolling issues
                            modifier = Modifier.height(220.dp)
                        ) {
                            // Status item
                            item {
                                Column {
                                    Text(
                                        text = "Status",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.tertiary,
                                                        shape = CircleShape
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = when {
                                                    isInstalled -> "Installed"
                                                    isDownloading -> "Installing"
                                                    else -> "Not Installed"
                                                },
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                    }
                                }
                            }

                            // Size item
                            item {
                                Column {
                                    Text(
                                        text = "Size",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isInstalled) {
                                            StorageUtils.formatBinarySize(
                                                StorageUtils.getFolderSize(SteamService.getAppDirPath(appInfo.id))
                                            )
                                        } else {
                                            val depots = SteamService.getDownloadableDepots(appInfo.id)
                                            val downloadBytes = depots.values.sumOf { it.manifests["public"]?.download ?: 0L }
                                            val installBytes = depots.values.sumOf { it.manifests["public"]?.size ?: 0L }
                                            "${StorageUtils.formatBinarySize(installBytes)}"
                                        },
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }

                            // Developer item
                            item {
                                Column {
                                    Text(
                                        text = "Developer",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = appInfo.developer,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }

                            // Release Date item
                            item {
                                Column {
                                    Text(
                                        text = "Release Date",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = remember(appInfo.releaseDate) {
                                            val date = Date(appInfo.releaseDate * 1000)
                                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
                                        },
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/***********
 * PREVIEW *
 ***********/

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
    var isDownloading by remember { mutableStateOf(false) }
    PluviaTheme {
        Surface {
            AppScreenContent(
                appInfo = fakeAppInfo(1),
                isInstalled = false,
                isValidToDownload = true,
                isDownloading = isDownloading,
                downloadProgress = .50f,
                onDownloadInstallClick = { isDownloading = !isDownloading },
                onPauseResumeClick = { },
                onDeleteDownloadClick = { },
                optionsMenu = AppOptionMenuType.entries.map {
                    AppMenuOption(
                        optionType = it,
                        onClick = { },
                    )
                }.toTypedArray(),
            )
        }
    }
}
