package com.OxGames.Pluvia.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.OxGames.Pluvia.Constants
import com.OxGames.Pluvia.R
import com.OxGames.Pluvia.ui.theme.PluviaTheme
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage

/**
 * A Custom wrapper that should be able to handle most bb code formating steam acknowledges.
 * This also includes emoticon rendering like steam does in chat or in profiles.
 * See: https://steamcommunity.com/comment/ForumTopic/formattinghelp
 */

// TODO web rich previews?

// private val noParsePattern = "\\[noparse]([^\\[]+)\\[/noparse]".toRegex()
private val colonPattern = "\u02D0([^\u02D0]+)\u02D0".toRegex()
private val emoticonPattern = "\\[emoticon]([^\\[]+)\\[/emoticon]".toRegex()
private val h1Pattern = "\\[h1]([^\\[]+)\\[/h1]".toRegex()
private val h2Pattern = "\\[h2]([^\\[]+)\\[/h2]".toRegex()
private val h3Pattern = "\\[h3]([^\\[]+)\\[/h3]".toRegex()
private val boldPattern = "\\[b]([^\\[]+)\\[/b]".toRegex()
private val italicPattern = "\\[i]([^\\[]+)\\[/i]".toRegex()
private val underlinePattern = "\\[u]([^\\[]+)\\[/u]".toRegex()
private val strikePattern = "\\[strike]([^\\[]+)\\[/strike]".toRegex()
private val spoilerPattern = "\\[spoiler]([^\\[]+)\\[/spoiler]".toRegex()
private val urlPattern = "\\[url=([^]]+)]([^\\[]+)\\[/url]".toRegex()
private val plainUrlPattern = "(https?://\\S+)".toRegex()
private val hrPattern = "\\[hr]([^\\[]*?)\\[/hr]".toRegex()
private val codePattern = "\\[code]([^\\[]*?)\\[/code]".toRegex()
private val quotePattern = "\\[quote=([^]]+)]([^\\[]*?)\\[/quote]".toRegex()
private val stickerPattern = "\\[sticker type=\"(.*?)\".*?]\\[/sticker]".toRegex()

private val bbCodePattern = (
    "$colonPattern|$emoticonPattern|$h1Pattern|$h2Pattern|$h3Pattern|$boldPattern|$underlinePattern|" +
        "$italicPattern|$strikePattern|$spoilerPattern|$urlPattern|$plainUrlPattern|$hrPattern|$codePattern|" +
        "$quotePattern|$stickerPattern"
    ).toRegex()

@Composable
fun BBCodeText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
) {
    val revealedSpoilers = remember { mutableStateMapOf<String, Boolean>() }
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    val matches = bbCodePattern.findAll(text).toList()

    val annotatedString = buildAnnotatedString {
        var currentIndex = 0

        matches.forEach { match ->
            if (match.range.first > currentIndex) {
                val textBefore = text.substring(currentIndex, match.range.first)
                append(textBefore)
            }

            when {
                match.groups[1] != null || match.groups[2] != null -> {
                    val emoticonName = match.groupValues
                        .getOrNull(1)
                        ?.takeUnless { it.isEmpty() }
                        ?: match.groupValues.getOrNull(2)

                    emoticonName?.let { emoticon ->
                        appendInlineContent(emoticon, "[emoji]")
                    }
                }
                // H1
                match.groups[3] != null -> {
                    withStyle(
                        style = SpanStyle(
                            fontSize = style.fontSize * 1.5f,
                            fontWeight = FontWeight.Bold,
                            baselineShift = BaselineShift(0.2f),
                        ),
                        block = { append(match.groupValues[3]) },
                    )
                }
                // H2
                match.groups[4] != null -> {
                    withStyle(
                        style = SpanStyle(
                            fontSize = style.fontSize * 1.25f,
                            fontWeight = FontWeight.Bold,
                            baselineShift = BaselineShift(0.2f),
                        ),
                        block = { append(match.groupValues[4]) },
                    )
                }
                // H3
                match.groups[5] != null -> {
                    withStyle(
                        style = SpanStyle(
                            fontSize = style.fontSize * 1.10f,
                            fontWeight = FontWeight.Bold,
                            baselineShift = BaselineShift(0.2f),
                        ),
                        block = { append(match.groupValues[5]) },
                    )
                }
                // Bold
                match.groups[6] != null -> {
                    withStyle(
                        style = SpanStyle(fontWeight = FontWeight.Bold, baselineShift = BaselineShift(0.2f)),
                        block = { append(match.groupValues[6]) },

                    )
                }
                // Underline
                match.groups[7] != null -> {
                    withStyle(
                        style = SpanStyle(textDecoration = TextDecoration.Underline, baselineShift = BaselineShift(0.2f)),
                        block = { append(match.groupValues[7]) },
                    )
                }
                // Italic
                match.groups[8] != null -> {
                    withStyle(
                        style = SpanStyle(fontStyle = FontStyle.Italic, baselineShift = BaselineShift(0.2f)),
                        block = { append(match.groupValues[8]) },
                    )
                }
                // Strike-through
                match.groups[9] != null -> {
                    withStyle(
                        style = SpanStyle(textDecoration = TextDecoration.LineThrough, baselineShift = BaselineShift(0.2f)),
                        block = { append(match.groupValues[9]) },
                    )
                }
                // Spoiler
                match.groups[10] != null -> {
                    val spoilerText = match.groupValues[10]
                    val spoilerId = "spoiler_${match.range.first}"

                    val isRevealed = revealedSpoilers[spoilerId] ?: false
                    pushStringAnnotation("spoiler", spoilerId)

                    withStyle(
                        style = SpanStyle(
                            background = if (isRevealed) Color.Unspecified else MaterialTheme.colorScheme.tertiaryContainer,
                            color = if (isRevealed) Color.Unspecified else MaterialTheme.colorScheme.tertiaryContainer,
                            baselineShift = BaselineShift(0.2f),
                        ),
                        block = { append(spoilerText) },
                    )
                    pop()
                }
                // BBcode URL
                match.groups[11] != null && match.groups[12] != null -> {
                    val url = match.groupValues[11]
                    val linkText = match.groupValues[12].trim()

                    pushStringAnnotation("URL", url)
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                            baselineShift = BaselineShift(0.2f),
                        ),
                        block = { append(linkText) },
                    )
                    pop()
                }
                // Plain URL
                match.groups[13] != null -> {
                    val url = match.groupValues[13]
                    pushStringAnnotation("URL", url)
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                            baselineShift = BaselineShift(0.2f),
                        ),
                        block = { append(url) },
                    )
                    pop()
                }
                // Horizontal Rule
                match.groups[14] != null -> {
                    withStyle(
                        style = SpanStyle(textDecoration = TextDecoration.LineThrough, baselineShift = BaselineShift(0.2f)),
                        block = { append("        ") },
                    )
                }
                // Code
                match.groups[15] != null -> {
                    withStyle(
                        style = SpanStyle(fontFamily = FontFamily.Monospace, baselineShift = BaselineShift(0.2f)),
                        block = { append(match.groupValues[15]) },
                    )
                }
                // Quote
                match.groups[16] != null && match.groups[17] != null -> {
                    withStyle(
                        style = SpanStyle(
                            background = MaterialTheme.colorScheme.surfaceVariant,
                            baselineShift = BaselineShift(0.2f),
                        ),
                    ) {
                        withStyle(
                            style = SpanStyle(fontStyle = FontStyle.Italic, baselineShift = BaselineShift(0.2f)),
                            block = { append("Originally posted by ${match.groupValues[16]}:\n") },
                        )

                        append(match.groupValues[17])
                    }
                }
                // Sticker
                match.groups[18] != null -> {
                    val stickerType = match.groupValues[18]
                    val stickerId = "sticker_$stickerType"
                    appendInlineContent(stickerId, "[sticker]")
                }
            }

            currentIndex = match.range.last + 1
        }

        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }

    // Build inline content map
// Build inline content map
    val inlineContentMap = buildMap {
        matches.forEach { match ->
            when {
                // Handle emoticons
                match.groups[1] != null || match.groups[2] != null -> {
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
                // Handle stickers
                match.groups[18] != null -> {
                    val stickerType = match.groupValues[18]
                    val stickerId = "sticker_$stickerType"
                    put(
                        stickerId,
                        InlineTextContent(
                            placeholder = Placeholder(
                                width = 150.sp,
                                height = 150.sp,
                                placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                            ),
                            children = {
                                CoilImage(
                                    modifier = Modifier.size(150.dp),
                                    imageModel = { Constants.Chat.STICKER_URL + stickerType },
                                    imageOptions = ImageOptions(
                                        contentDescription = stickerType,
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
    }

    Text(
        text = annotatedString,
        color = color,
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                val position = annotatedString
                    .getStringAnnotations("spoiler", start = 0, end = annotatedString.length)
                    .firstOrNull { annotation ->
                        val textLayoutResult = layoutResult.value
                        textLayoutResult?.let { layoutResult ->
                            val bounds = layoutResult.getBoundingBox(annotation.start)
                            val expandedBounds = Rect(
                                bounds.left,
                                bounds.top,
                                bounds.left + layoutResult.size.width,
                                bounds.top + layoutResult.size.height,
                            )
                            expandedBounds.contains(offset)
                        } ?: false
                    }
                position?.let { annotation ->
                    revealedSpoilers[annotation.item] = !(revealedSpoilers[annotation.item] ?: false)
                }
            }
        },
        style = style,
        inlineContent = inlineContentMap,
        onTextLayout = { layoutResult.value = it },
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview
@Composable
private fun Preview_BBCodeText() {
    PluviaTheme {
        Surface {
            Column {
                BBCodeText(
                    text = """
                [h1]Header 1 text[/h1]
                [h2]Header 2 text[/h2]
                [h3]Header 3 text[/h3]
                [b]Bold text [/b]
                [u]Underlined text [/u]
                [i]Italic text [/i]
                [strike]Strikethrough text[/strike]
                [spoiler]Spoiler text[/spoiler]
                [noparse]Doesn't parse [b]tags[/b][/noparse]
                [hr][/hr]
                [url=store.steampowered.com] Website link [/url]
                https://www.youtube.com/watch?v=tax4e4hBBZc
                [quote=author]Quoted text[/quote]
                [code]Fixed-width font, preserves spaces[/code]
                Some ːsteamhappyː for ːsteamsadː testing.
                Hello World! [emoticon]steamhappy[/emoticon]
                    """.trimIndent(),
                )

                Spacer(Modifier.height(14.dp))

                BBCodeText(text = "[sticker type=\"Winter2019JingleIntensifies\" limit=\"0\"][/sticker]")
            }
        }
    }
}
