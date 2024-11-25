package com.OxGames.Pluvia.ui.util

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.test.FakeImage
import com.OxGames.Pluvia.ui.theme.PluviaTheme

@OptIn(ExperimentalCoilApi::class)
@Composable
internal fun CoilAsyncImage(
    modifier: Modifier = Modifier,
    url: String?,
    size: DpSize? = null,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit
) {
    val previewHandler = AsyncImagePreviewHandler {
        FakeImage(color = Color(0xFF660099).toArgb())
    }

    var isLoading by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        Box(modifier = modifier) {
            AsyncImage(
                modifier = if (size != null) Modifier.size(size) else Modifier,
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                onState = { state ->
                    when (state) {
                        AsyncImagePainter.State.Empty,
                        is AsyncImagePainter.State.Error -> {
                            isLoading = false
                            isError = true
                        }

                        is AsyncImagePainter.State.Loading -> {
                            isLoading = true
                            isError = false
                        }

                        is AsyncImagePainter.State.Success -> {
                            isLoading = false
                            isError = false
                        }
                    }
                }
            )
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
                content = {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp)
                    )
                }
            )
            AnimatedVisibility(
                visible = isError,
                enter = fadeIn(),
                exit = fadeOut(),
                content = {
                    Image(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.onSurface),
                        imageVector = Icons.Default.QuestionMark,
                        contentDescription = null
                    )
                }
            )
        }
    }
}

@Preview
@Composable
private fun Preview_CoilAsyncImage() {
    PluviaTheme {
        CoilAsyncImage(url = "", size = DpSize(40.dp, 40.dp))
    }
}