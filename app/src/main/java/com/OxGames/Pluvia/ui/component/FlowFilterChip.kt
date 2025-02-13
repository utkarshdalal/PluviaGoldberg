package com.OxGames.Pluvia.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FlowFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit),
) {
    FilterChip(
        modifier = Modifier.padding(end = 8.dp).then(modifier),
        onClick = onClick,
        label = label,
        selected = selected,
        leadingIcon = leadingIcon,
    )
}
