package com.OxGames.Pluvia.ui.component

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
    val isDownloading: () -> Boolean = { downloadInfo != null && downloadProgress < 1f }

    DisposableEffect(downloadInfo) {
        Log.d("AppScreen", "Found download info $downloadInfo")
        val onDownloadProgress: (Float) -> Unit = {
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
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Button(
                shape = RoundedCornerShape(8.dp),
                enabled = !isDownloading(),
                onClick = {
                    downloadProgress = 0f
                    downloadInfo = SteamService.downloadApp(appId)
                    Log.d("AppScreen", "Started app download $downloadInfo")
                }
            ) { Text(stringResource(R.string.download_app)) }
            if (isDownloading()) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .align(Alignment.CenterVertically),
                    progress = { downloadProgress }
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
            IconButton(onClick = {

            }) { Icon(Icons.Filled.MoreVert, "Options") }
        }
    }
}