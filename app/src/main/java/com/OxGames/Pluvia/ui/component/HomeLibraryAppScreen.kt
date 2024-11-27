package com.OxGames.Pluvia.ui.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.test.FakeImage
import com.OxGames.Pluvia.R
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.ui.theme.PluviaTheme

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

@OptIn(ExperimentalCoilApi::class)
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
            // Hero image (unchanged)
            val previewHero = AsyncImagePreviewHandler {
                FakeImage(color = Color.DarkGray.toArgb())
            }
            CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHero) {
                AsyncImage(
                    model = appInfo?.getHeroUrl(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 10f)
                )
            }

            // Logo with shadow that follows image content
            val previewLogo = AsyncImagePreviewHandler {
                FakeImage(color = Color.LightGray.toArgb())
            }
            CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewLogo) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    model = appInfo?.getLogoUrl(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit
                )
            }
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