package com.OxGames.Pluvia.ui.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.OxGames.Pluvia.R
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.ui.theme.PluviaTheme
import com.OxGames.Pluvia.ui.util.CoilAsyncImage

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
    fun Modifier.parallaxLayoutModifier(scrollState: ScrollState, rate: Int) =
        layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            val height = if (rate > 0) scrollState.value / rate else scrollState.value
            layout(placeable.width, placeable.height) {
                placeable.place(0, height)
            }
        }

    val scrollState = rememberScrollState()

    val appInfo by remember(appId) {
        mutableStateOf(SteamService.getAppInfoOf(appId))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        // TODO 'CoilAsyncImage' loading spinner is top left.
        // TODO: 'CoilAsyncImage' maybe provide 'fake' or `approx` size?
        // TODO: Terrible drop shadow :)
        //  ...Modifier.shadow() doesnt seem dark enough to provide contract between hero and app logo.
        Box(modifier = Modifier.parallaxLayoutModifier(scrollState, 10)) {
            CoilAsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(with(LocalDensity.current) {
                        DpSize(1920.toDp(), 620.toDp())
                    }),
                url = appInfo?.getHeroUrl(),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            CoilAsyncImage(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .width(256.dp)
                    .offset(x = 16.dp, y = (-16).dp)
                    .drawBehind {
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.5f),
                            cornerRadius = CornerRadius(16.dp.toPx()),
                            size = Size(
                                width = size.width + 8.dp.toPx(),
                                height = size.height + 8.dp.toPx()
                            ),
                            topLeft = Offset(-4.dp.toPx(), -4.dp.toPx())
                        )
                    },
                url = appInfo?.getLogoUrl(),
                contentDescription = null,
                contentScale = ContentScale.Fit
            )
        }

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
                content = {
                    Icon(Icons.Filled.MoreVert, "Options")
                }
            )
        }
    }
}

@Preview
@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
private fun Preview_AppScreen() {
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