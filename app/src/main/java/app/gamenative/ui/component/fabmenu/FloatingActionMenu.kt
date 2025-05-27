package app.gamenative.ui.component.fabmenu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.gamenative.ui.component.fabmenu.state.FloatingActionMenuState
import app.gamenative.ui.component.fabmenu.state.FloatingActionMenuValue
import app.gamenative.ui.component.fabmenu.state.rememberFloatingActionMenuState
import app.gamenative.ui.theme.PluviaTheme
import kotlinx.coroutines.launch

@Composable
fun FloatingActionMenu(
    modifier: Modifier = Modifier,
    shape: Shape = FloatingActionButtonDefaults.shape,
    imageVector: ImageVector,
    closeImageVector: ImageVector? = null,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    interactionSource: MutableInteractionSource? = null,
    state: FloatingActionMenuState = rememberFloatingActionMenuState(),
    content: @Composable (ColumnScope.() -> Unit),
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AnimatedVisibility(
            visible = state.isOpen,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
        ) {
            Column(
                modifier = Modifier.offset(x = (-4).dp).padding(start = 4.dp),
                content = content,
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(0.dp),
            )
        }
        FloatingActionButton(
            shape = shape,
            containerColor = containerColor,
            contentColor = contentColor,
            elevation = elevation,
            interactionSource = interactionSource,
            onClick = {
                scope.launch {
                    if (state.isOpen) {
                        state.close()
                    } else {
                        state.open()
                    }
                }
            },
        ) {
            Icon(
                imageVector = if (state.isOpen && closeImageVector != null) closeImageVector else imageVector,
                contentDescription = if (state.isOpen) "Close menu" else "Open menu",
            )
        }
    }
}

@Preview
@Composable
private fun Preview_FloatingActionMenu() {
    val state = rememberFloatingActionMenuState(initialValue = FloatingActionMenuValue.Open)
    PluviaTheme {
        Surface {
            FloatingActionMenu(
                state = state,
                imageVector = Icons.Filled.FilterList,
                closeImageVector = Icons.Filled.Close,
                content = {
                    FloatingActionMenuItem(
                        labelText = "Search",
                        onClick = { },
                    ) { Icon(Icons.Filled.Search, "Search") }
                    FloatingActionMenuItem(
                        labelText = "Installed",
                        onClick = { },
                    ) { Icon(Icons.Filled.InstallMobile, "Installed") }
                    FloatingActionMenuItem(
                        labelText = "Alphabetic",
                        onClick = { },
                    ) { Icon(Icons.Filled.SortByAlpha, "Alphabetic") }
                },
            )
        }
    }
}
