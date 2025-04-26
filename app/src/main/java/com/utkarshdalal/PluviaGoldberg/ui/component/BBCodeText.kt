package com.utkarshdalal.PluviaGoldberg.ui.component

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
import com.utkarshdalal.PluviaGoldberg.Constants
import com.utkarshdalal.PluviaGoldberg.ui.theme.PluviaTheme
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import com.utkarshdalal.PluviaGoldberg.R

/**
 * A Custom wrapper that should be able to handle most bb code formating steam acknowledges.
 * This also includes emoticon rendering like steam does in chat or in profiles.
 * See: https://steamcommunity.com/comment/ForumTopic/formattinghelp
 */

// TODO:
//  Slash commands
//  Rich Previews with http links

enum class BBCode(val pattern: String, val groupCount: Int = 1) {
    COLON("\u02D0([^\u02D0]+)\u02D0"),
    EMOTICON("\\[emoticon]([^\\[]+)\\[/emoticon]"),
    H1("\\[h1]([^\\[]+)\\[/h1]"),
    H2("\\[h2]([^\\[]+)\\[/h2]"),
    H3("\\[h3]([^\\[]+)\\[/h3]"),
    BOLD("\\[b]([^\\[]+)\\[/b]"),
    ITALIC("\\[i]([^\\[]+)\\[/i]"),
    UNDERLINE("\\[u]([^\\[]+)\\[/u]"),
    STRIKE_THROUGH("\\[strike]([^\\[]+)\\[/strike]"),
    SPOILER("\\[spoiler]([^\\[]+)\\[/spoiler]"),
    URL("\\[url=([^]]+)]([^\\[]+)\\[/url]", 2),
    PLAIN_URL("(https?://\\S+)"),
    HORIZONTAL_RULE("\\[hr]([^\\[]*?)\\[/hr]"),
    CODE("\\[code]([^\\[]*?)\\[/code]"),
    QUOTE("\\[quote=([^]]+)]([^\\[]*?)\\[/quote]", 2),
    STICKER("\\[sticker type=\"(.*?)\".*?]\\[/sticker]"),
    ;

    fun groupIndex(): Int =
        ordinal + 1 + entries.foldIndexed(
            0,
        ) { index, accum, current ->
            if (index < ordinal) {
                accum + current.groupCount - 1
            } else {
                accum
            }
        }

    companion object {
        fun pattern(): Regex = entries
            .map { it.pattern }
            .joinToString("|")
            .toRegex()
    }
}

@Composable
fun BBCodeText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
) {
    val revealedSpoilers = remember { mutableStateMapOf<String, Boolean>() }
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    val matches = BBCode.pattern().findAll(text).toList()

    val annotatedString = buildAnnotatedString {
        var currentIndex = 0

        matches.forEach { match ->
            if (match.range.first > currentIndex) {
                val textBefore = text.substring(currentIndex, match.range.first)
                append(textBefore)
            }

            when {
                match.groups[BBCode.COLON.groupIndex()] != null ||
                    match.groups[BBCode.EMOTICON.groupIndex()] != null
                -> {
                    val emoticonName = match.groupValues
                        .getOrNull(1)
                        ?.takeUnless { it.isEmpty() }
                        ?: match.groupValues.getOrNull(BBCode.EMOTICON.groupIndex())

                    emoticonName?.let { emoticon ->
                        appendInlineContent(emoticon, "[emoji]")
                    }
                }
                match.groups[BBCode.H1.groupIndex()] != null -> {
                    withStyle(
                        style = SpanStyle(
                            fontSize = style.fontSize * 1.5f,
                            fontWeight = FontWeight.Bold,
                            baselineShift = BaselineShift(0.2f),
                        ),
                        block = { append(match.groupValues[BBCode.H1.groupIndex()]) },
                    )
                }
                match.groups[BBCode.H2.groupIndex()] != null -> {
                    withStyle(
                        style = SpanStyle(
                            fontSize = style.fontSize * 1.25f,
                            fontWeight = FontWeight.Bold,
                            baselineShift = BaselineShift(0.2f),
                        ),
                        block = { append(match.groupValues[BBCode.H2.groupIndex()]) },
                    )
                }
                match.groups[BBCode.H3.groupIndex()] != null -> {
                    withStyle(
                        style = SpanStyle(
                            fontSize = style.fontSize * 1.10f,
                            fontWeight = FontWeight.Bold,
                            baselineShift = BaselineShift(0.2f),
                        ),
                        block = { append(match.groupValues[BBCode.H3.groupIndex()]) },
                    )
                }
                match.groups[BBCode.BOLD.groupIndex()] != null -> {
                    withStyle(
                        style = SpanStyle(fontWeight = FontWeight.Bold, baselineShift = BaselineShift(0.2f)),
                        block = { append(match.groupValues[BBCode.BOLD.groupIndex()]) },

                    )
                }
                match.groups[BBCode.UNDERLINE.groupIndex()] != null -> {
                    withStyle(
                        style = SpanStyle(textDecoration = TextDecoration.Underline, baselineShift = BaselineShift(0.2f)),
                        block = { append(match.groupValues[BBCode.UNDERLINE.groupIndex()]) },
                    )
                }
                match.groups[BBCode.ITALIC.groupIndex()] != null -> {
                    withStyle(
                        style = SpanStyle(fontStyle = FontStyle.Italic, baselineShift = BaselineShift(0.2f)),
                        block = { append(match.groupValues[BBCode.ITALIC.groupIndex()]) },
                    )
                }
                match.groups[BBCode.STRIKE_THROUGH.groupIndex()] != null -> {
                    withStyle(
                        style = SpanStyle(textDecoration = TextDecoration.LineThrough, baselineShift = BaselineShift(0.2f)),
                        block = { append(match.groupValues[BBCode.STRIKE_THROUGH.groupIndex()]) },
                    )
                }
                match.groups[BBCode.SPOILER.groupIndex()] != null -> {
                    val spoilerText = match.groupValues[BBCode.SPOILER.groupIndex()]
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
                match.groups[BBCode.URL.groupIndex()] != null &&
                    match.groups[BBCode.URL.groupIndex() + 1] != null
                -> {
                    val url = match.groupValues[BBCode.URL.groupIndex()]
                    val linkText = match.groupValues[BBCode.URL.groupIndex() + 1].trim()

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
                match.groups[BBCode.PLAIN_URL.groupIndex()] != null -> {
                    val url = match.groupValues[BBCode.PLAIN_URL.groupIndex()]
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
                match.groups[BBCode.HORIZONTAL_RULE.groupIndex()] != null -> {
                    withStyle(
                        style = SpanStyle(textDecoration = TextDecoration.LineThrough, baselineShift = BaselineShift(0.2f)),
                        block = { append("        ") },
                    )
                }
                match.groups[BBCode.CODE.groupIndex()] != null -> {
                    withStyle(
                        style = SpanStyle(fontFamily = FontFamily.Monospace, baselineShift = BaselineShift(0.2f)),
                        block = { append(match.groupValues[BBCode.CODE.groupIndex()]) },
                    )
                }
                match.groups[BBCode.QUOTE.groupIndex()] != null &&
                    match.groups[BBCode.QUOTE.groupIndex() + 1] != null
                -> {
                    withStyle(
                        style = SpanStyle(
                            background = MaterialTheme.colorScheme.surfaceVariant,
                            baselineShift = BaselineShift(0.2f),
                        ),
                    ) {
                        withStyle(
                            style = SpanStyle(fontStyle = FontStyle.Italic, baselineShift = BaselineShift(0.2f)),
                            block = { append("Originally posted by ${match.groupValues[BBCode.QUOTE.groupIndex()]}:\n") },
                        )

                        append(match.groupValues[BBCode.QUOTE.groupIndex() + 1])
                    }
                }
                match.groups[BBCode.STICKER.groupIndex()] != null -> {
                    val stickerType = match.groupValues[BBCode.STICKER.groupIndex()]
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
                                        previewPlaceholder = painterResource(R.drawable.ic_logo_color),
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
                                    previewPlaceholder = painterResource(R.drawable.ic_logo_color),
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
