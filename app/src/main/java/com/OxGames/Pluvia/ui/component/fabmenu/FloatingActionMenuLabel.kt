package com.OxGames.Pluvia.ui.component.fabmenu

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.OxGames.Pluvia.ui.theme.PluviaTheme

@Composable
fun FloatingActionMenuLabel(
    modifier: Modifier = Modifier,
    label: String,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = Color.Black.copy(alpha = 0.8f),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp),
            text = label,
            color = Color.White,
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
            FloatingActionMenuLabel(label = "Hello World")
        }
    }
}
