package com.OxGames.Pluvia.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.OxGames.Pluvia.Constants
import com.OxGames.Pluvia.R
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage

/**
 * A wrapper for [Text] that renders emoticons inline with the text.
 * Example:
 *  1. "Hello world! :steamhappy:
 *  2. "Hello World! \\[emoticon]steamhappy[/emoticon]
 */

private val colonPattern = "\u02D0([^\u02D0]+)\u02D0".toRegex()
private val bbCodePattern = "\\[emoticon\\]([^\\[]+)\\[/emoticon\\]".toRegex()
private val emoticonPattern = "$colonPattern|$bbCodePattern".toRegex()

@Composable
fun EmoticonText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = LocalTextStyle.current,
) {
    val matches = emoticonPattern.findAll(text).toList()

    val annotatedString = buildAnnotatedString {
        var currentIndex = 0

        matches.forEach { match ->
            if (match.range.first > currentIndex) {
                val textBefore = text.substring(currentIndex, match.range.first)
                append(textBefore)
            }

            val emoticonName = match.groupValues
                .getOrNull(1)
                ?.takeUnless { it.isEmpty() }
                ?: match.groupValues.getOrNull(2)

            emoticonName?.let { emoticon ->
                appendInlineContent(emoticon, "[emoji]")
            }

            currentIndex = match.range.last + 1
        }

        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }

    val inlineContentMap = buildMap {
        matches.forEach { match ->
            val emoticonName = match.groupValues
                .getOrNull(1)
                ?.takeUnless { it.isEmpty() }
                ?: match.groupValues.getOrNull(2)

            emoticonName?.let { emoticon ->
                put(
                    emoticon,
                    InlineTextContent(
                        placeholder = Placeholder(
                            width = style.fontSize,
                            height = style.fontSize,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                        ),
                        children = {
                            CoilImage(
                                modifier = Modifier.size(style.fontSize.value.dp),
                                imageModel = { Constants.Chat.EMOTICON_URL + emoticon },
                                imageOptions = ImageOptions(
                                    contentDescription = emoticon,
                                    contentScale = ContentScale.Fit,
                                ),
                                loading = {
                                    CircularProgressIndicator()
                                },
                                failure = {
                                    Icon(Icons.Filled.QuestionMark, null)
                                },
                                previewPlaceholder = painterResource(R.drawable.icon_mono_foreground),
                            )
                        },
                    ),
                )
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        style = style,
        inlineContent = inlineContentMap,
    )
}
