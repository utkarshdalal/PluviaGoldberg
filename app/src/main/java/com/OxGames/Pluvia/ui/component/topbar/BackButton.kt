package com.OxGames.Pluvia.ui.component.topbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

@Composable
fun BackButton(
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        content = { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Navigate Back") }
    )
}