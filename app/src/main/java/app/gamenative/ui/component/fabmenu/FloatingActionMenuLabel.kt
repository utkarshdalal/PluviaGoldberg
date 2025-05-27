package app.gamenative.ui.component.fabmenu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gamenative.ui.theme.PluviaTheme

@Composable
fun FloatingActionMenuLabel(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondary,
    isSelected: Boolean,
    label: String,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = color,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp),
            text = buildAnnotatedString {
                if (isSelected) {
                    appendInlineContent("selected", "[icon]")
                    append(" ")
                }
                append(label)
            },
            inlineContent = mapOf(
                "selected" to InlineTextContent(
                    Placeholder(
                        width = 14.sp,
                        height = 14.sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                    ),
                    children = {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                        )
                    },
                ),
            ),
            fontSize = 14.sp,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun Preview_FloatingActionMenuLabel() {
    PluviaTheme {
        Surface {
            Column {
                FloatingActionMenuLabel(label = "Hello World", isSelected = true)
                FloatingActionMenuLabel(label = "Hello World", isSelected = false)
            }
        }
    }
}
