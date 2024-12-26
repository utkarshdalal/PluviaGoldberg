package com.OxGames.Pluvia.ui.component.dialog.state

import androidx.compose.ui.graphics.vector.ImageVector

data class MessageDialogState(
    val visible: Boolean,
    val onDismissRequest: (() -> Unit)? = null,
    val onConfirmClick: (() -> Unit)? = null,
    val onDismissClick: (() -> Unit)? = null,
    val confirmBtnText: String = "Confirm",
    val dismissBtnText: String = "Dismiss",
    val icon: ImageVector? = null,
    val title: String? = null,
    val message: String? = null,
)
