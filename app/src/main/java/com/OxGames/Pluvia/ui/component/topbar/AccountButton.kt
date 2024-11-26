package com.OxGames.Pluvia.ui.component.topbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

@Composable
fun AccountButton(
    contentDescription: String? = null,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        content = {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = contentDescription ?: "User Account"
            )
        }
    )
}