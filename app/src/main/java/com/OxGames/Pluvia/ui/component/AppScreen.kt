package com.OxGames.Pluvia.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.R
import com.OxGames.Pluvia.data.DownloadInfo
import com.OxGames.Pluvia.ui.theme.PluviaTheme

@Composable
fun AppScreen(
    appId: Int,
    onClickPlay: () -> Unit,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    var downloadInfo by remember {
        mutableStateOf<DownloadInfo?>(SteamService.getAppDownloadInfo(appId))
    }
    var downloadProgress by remember { mutableFloatStateOf(downloadInfo?.getProgress() ?: 0f) }
    var isInstalled by remember { mutableStateOf(SteamService.isAppInstalled(appId)) }
    val isDownloading: () -> Boolean = { downloadInfo != null && downloadProgress < 1f }

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

    AppScreenContent(
        appId = appId,
        isInstalled = isInstalled,
        isDownloading = isDownloading(),
        downloadProgress = downloadProgress,
        onDownloadClick = {
            if (!isInstalled) {
                downloadProgress = 0f
                downloadInfo = SteamService.downloadApp(appId)
            } else {
                onClickPlay()
            }
        }
    )
}

@Composable
private fun AppScreenContent(
    appId: Int,
    isInstalled: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float,
    onDownloadClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxWidth(),
            model = SteamService.getAppInfoOf(appId)?.getHeroUrl(),
            contentDescription = stringResource(R.string.header_img_desc),
            error = if (LocalInspectionMode.current) {
                painterResource(R.drawable.ic_launcher_background)
            } else null, // Should have a fallback icon
        )
        Row(
            modifier = Modifier
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
                    LinearProgressIndicator(progress = { downloadProgress })
                }
            } else {
                Spacer(Modifier.weight(1f))
            }
            IconButton(
                onClick = {
                    // TODO: add options menu
                },
                content = { Icon(Icons.Filled.MoreVert, "Options") }
            )
        }
    }
}

@Preview
@Composable
private fun Preview_AppScreen() {
    PluviaTheme {
        Surface {
            AppScreenContent(
                appId = 440,
                isInstalled = false,
                isDownloading = true,
                downloadProgress = .50f,
                onDownloadClick = { },
            )
        }
    }
}