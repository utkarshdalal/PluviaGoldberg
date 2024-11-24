package com.OxGames.Pluvia.ui.component.fabmenu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.OxGames.Pluvia.ui.theme.PluviaTheme

// TODO left margin
//  Note: seems to clip left side when rendering with long text. Such as: "Alphabetic"
@Composable
fun FloatingActionMenuItem(
    labelText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = FloatingActionButtonDefaults.smallShape,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable (() -> Unit)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FloatingActionMenuLabel(label = labelText)

        SmallFloatingActionButton(
            modifier = modifier,
            onClick = onClick,
            containerColor = containerColor,
            shape = shape,
            contentColor = contentColor,
            elevation = elevation,
            interactionSource = interactionSource,
            content = content
        )
    }
}

@Preview
@Composable
private fun Preview_FloatingActionMenuItem() {
    PluviaTheme {
        Surface {
            FloatingActionMenuItem(
                labelText = "Hello World",
                onClick = { },
                content = { Icon(Icons.Filled.SortByAlpha, null) }
            )
        }
    }
}