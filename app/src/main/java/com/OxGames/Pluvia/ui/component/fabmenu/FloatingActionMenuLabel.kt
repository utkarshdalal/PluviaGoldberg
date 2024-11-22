package com.OxGames.Pluvia.ui.component.fabmenu

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FloatingActionMenuLabel(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = Color.Black.copy(alpha = 0.8f)
    ) {
        Text(
            text = label, color = Color.White,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp),
            fontSize = 14.sp,
            maxLines = 1
        )
    }

}