package com.OxGames.Pluvia.ui.component.dialog

import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.OxGames.Pluvia.ui.theme.PluviaTheme

@Composable
fun MessageDialog(
    visible: Boolean,
    onDismissRequest: (() -> Unit)? = null,
    onConfirmClick: (() -> Unit)? = null,
    onDismissClick: (() -> Unit)? = null,
    confirmBtnText: String = "Confirm",
    dismissBtnText: String = "Dismiss",
    icon: ImageVector? = null,
    title: String? = null,
    message: String? = null,
) {
    when {
        visible -> {
            AlertDialog(
                icon = icon?.let { { Icon(imageVector = icon, contentDescription = null) } },
                title = title?.let { { Text(it) } },
                text = message?.let { { Text(it) } },
                onDismissRequest = { onDismissRequest?.invoke() },
                dismissButton = onDismissClick?.let {
                    {
                        TextButton(onClick = it) {
                            Text(dismissBtnText)
                        }
                    }
                },
                confirmButton = {
                    onConfirmClick?.let {
                        TextButton(onClick = it) {
                            Text(confirmBtnText)
                        }
                    }
                },
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview_MessageDialog() {
    PluviaTheme {
        MessageDialog(
            visible = true,
            icon = Icons.Default.Gamepad,
            title = "Title",
            message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed " +
                "do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco " +
                "laboris nisi ut aliquip ex ea commodo consequat. Duis aute " +
                "irure dolor in reprehenderit in voluptate velit esse cillum " +
                "dolore eu fugiat nulla pariatur. Excepteur sint occaecat " +
                "cupidatat non proident, sunt in culpa qui officia deserunt " +
                "mollit anim id est laborum.",
            onDismissRequest = {},
            onDismissClick = {},
            onConfirmClick = {},
        )
    }
}
