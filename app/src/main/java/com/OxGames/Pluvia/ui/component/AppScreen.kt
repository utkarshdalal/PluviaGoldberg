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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import coil3.compose.AsyncImage
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.R
import com.OxGames.Pluvia.data.DownloadInfo

@Composable
fun AppScreen(
    appId: Int,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    var downloadInfo by remember { mutableStateOf<DownloadInfo?>(SteamService.getAppDownloadInfo(appId)) }
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

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AsyncImage(
            model = SteamService.getAppInfoOf(appId)?.getHeroUrl(),
            contentDescription = stringResource(R.string.header_img_desc),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .wrapContentHeight()
        ) {
            Button(
                shape = RoundedCornerShape(8.dp),
                enabled = !isDownloading(),
                onClick = {
                    if (!isInstalled) {
                        downloadProgress = 0f
                        downloadInfo = SteamService.downloadApp(appId)
                    } else {
                        // TODO: run the app
                    }
                }
            ) {
                Text(
                    if (isInstalled)
                        stringResource(R.string.run_app)
                    else
                        stringResource(R.string.download_app)
                )
            }
            if (isDownloading()) {
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
            IconButton(onClick = {
                // TODO: add options menu
            }) { Icon(Icons.Filled.MoreVert, "Options") }
        }
    }
}