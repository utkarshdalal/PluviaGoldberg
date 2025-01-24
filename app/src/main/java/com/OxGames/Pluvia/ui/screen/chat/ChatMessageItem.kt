package com.OxGames.Pluvia.ui.screen.chat

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.OxGames.Pluvia.ui.theme.PluviaTheme

@Composable
fun ChatBubble(
    message: String,
    timestamp: String,
    fromLocal: Boolean,
    modifier: Modifier = Modifier,
    bubbleColor: Color = if (fromLocal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
    textColor: Color = if (fromLocal) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
    timestampColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    bubbleShape: Shape = RoundedCornerShape(16.dp),
    maxWidth: Dp = 280.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = if (fromLocal) Alignment.End else Alignment.Start,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .background(
                    color = bubbleColor,
                    shape = bubbleShape,
                ),
        ) {
            Column(modifier = Modifier.padding(contentPadding)) {
                // The message
                Text(
                    modifier = Modifier.align(if (fromLocal) Alignment.End else Alignment.Start),
                    text = message,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge,
                )

                // The time
                Text(
                    text = timestamp,
                    color = timestampColor,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 6.dp),
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun ChatBubblePreview() {
    PluviaTheme {
        Surface {
            Column {
                ChatBubble(
                    message = "Hey",
                    timestamp = "Jan 00 - 00:00 PM",
                    fromLocal = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                ChatBubble(
                    message = ":O!!!",
                    timestamp = "Jan 00 - 00:00 PM",
                    fromLocal = false,
                )

                ChatBubble(
                    message = "Wow very cool, we should play a game together sometime! How does Team Fortress 2 sound?",
                    timestamp = "Jan 00 - 00:00 PM",
                    fromLocal = false,
                )
            }
        }
    }
}
