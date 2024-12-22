package com.OxGames.Pluvia.ui.component.dialog.state

data class MessageDialogState(
    val visible: Boolean,
    val onDismissRequest: (() -> Unit)? = null,
    val onConfirmClick: (() -> Unit)? = null,
    val onDismissClick: (() -> Unit)? = null,
    val confirmBtnText: String = "Confirm",
    val dismissBtnText: String = "Dismiss",
    val title: String? = null,
    val message: String? = null,
)