package com.OxGames.Pluvia.ui.component.fabmenu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
    content: @Composable (() -> Unit),
) {
    // Modifier: Padding (start) fix to stop rendering clip,
    // FAB has an interactable padding which takes care of the end side.
    Row(
        modifier = modifier.padding(start = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FloatingActionMenuLabel(label = labelText)

        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = containerColor,
            shape = shape,
            contentColor = contentColor,
            elevation = elevation,
            interactionSource = interactionSource,
            content = content,
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
                content = { Icon(Icons.Filled.SortByAlpha, null) },
            )
        }
    }
}
