package com.OxGames.Pluvia.ui.screen.home

import android.content.Intent
import android.content.res.Configuration
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.OxGames.Pluvia.R
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.ui.theme.PluviaTheme
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths

// https://partner.steamgames.com/doc/store/assets/libraryassets#4

@Composable
fun AppScreen(
    appId: Int,
    onClickPlay: () -> Unit,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    var downloadInfo by remember {
        mutableStateOf(SteamService.getAppDownloadInfo(appId))
    }
    var downloadProgress by remember { mutableFloatStateOf(downloadInfo?.getProgress() ?: 0f) }
    var isInstalled by remember { mutableStateOf(SteamService.isAppInstalled(appId)) }
    val isDownloading: () -> Boolean = { downloadInfo != null && downloadProgress < 1f }
    val context = LocalContext.current

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

    Scaffold { paddingValues ->
        AppScreenContent(
            modifier = Modifier.padding(paddingValues),
            appId = appId,
            isInstalled = isInstalled,
            isDownloading = isDownloading(),
            downloadProgress = downloadProgress,
            onDownloadClick = {
                if (!isInstalled) {
                    downloadProgress = 0f
                    downloadInfo = SteamService.downloadApp(appId)
                } else {
                    val steamApiPath = Paths.get(SteamService.getAppDirPath(appId), "steam_api.dll")
                    // delete existing steam api
                    if (Files.exists(steamApiPath)) {
                        Files.delete(steamApiPath)
                    }
                    Files.createFile(steamApiPath)
                    FileOutputStream(steamApiPath.toString()).use { fos ->
                        context.assets.open("steampipe/steam_api.dll").use { fs ->
                            fs.copyTo(fos)
                        }
                    }
                    onClickPlay()
                }
            }
        )
    }
}

@Composable
private fun AppScreenContent(
    modifier: Modifier = Modifier,
    appId: Int,
    isInstalled: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float,
    onDownloadClick: () -> Unit,
) {
    val scrollState = rememberScrollState()

    val appInfo by remember(appId) {
        mutableStateOf(SteamService.getAppInfoOf(appId))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box {
            // Hero Logo
            CoilImage(
                modifier = Modifier.fillMaxWidth(),
                imageModel = { appInfo?.getHeroUrl() },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop
                ),
                loading = {
                    CircularProgressIndicator()
                },
                failure = {
                    Icon(Icons.Filled.QuestionMark, null)
                },
                previewPlaceholder = painterResource(R.drawable.testhero)
            )

            // Library Logo
            CoilImage(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 8.dp, bottom = 8.dp),
                imageModel = { appInfo?.getLogoUrl() },
                imageOptions = ImageOptions(
                    contentScale = FixedScale(1f),
                    requestSize = IntSize(640, 360)
                ),
                loading = {
                    CircularProgressIndicator()
                },
                failure = {
                    Icon(Icons.Filled.QuestionMark, null)
                },
                previewPlaceholder = painterResource(R.drawable.testliblogo)
            )
        }

        // Controls Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .wrapContentHeight()
        ) {
            Button(
                shape = RoundedCornerShape(8.dp),
                enabled = !isDownloading,
                onClick = onDownloadClick
            ) {
                Text(
                    if (isInstalled)
                        stringResource(R.string.run_app)
                    else
                        stringResource(R.string.download_app)
                )
            }

            if (isDownloading) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f)
                        .padding(4.dp)
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.End),
                        text = "${(downloadProgress * 100f).toInt()}%"
                    )
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        progress = { downloadProgress })
                }
            } else {
                Spacer(Modifier.weight(1f))
            }

            IconButton(
                onClick = {
                    // TODO: add options menu
                },
                content = {
                    Icon(Icons.Filled.MoreVert, "Options")
                }
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(
    device = "spec:width=1920px,height=1080px,dpi=440",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
) // Odin2 Mini
@Composable
private fun Preview_AppScreen() {
    val context = LocalContext.current
    val intent = Intent(context, SteamService::class.java)
    context.startService(intent)
    PluviaTheme {
        Surface {
            AppScreenContent(
                appId = 736260,
                isInstalled = false,
                isDownloading = true,
                downloadProgress = .50f,
                onDownloadClick = { },
            )
        }
    }
}