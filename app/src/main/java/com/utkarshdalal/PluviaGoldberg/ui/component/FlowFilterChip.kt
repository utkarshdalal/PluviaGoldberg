package com.utkarshdalal.PluviaGoldberg.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.utkarshdalal.PluviaGoldberg.ui.theme.PluviaTheme

@Composable
fun FlowFilterChip(
    modifier: Modifier = Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable (() -> Unit),
    leadingIcon: @Composable (() -> Unit),
) {
    FilterChip(
        modifier = Modifier
            .padding(end = 8.dp)
            .then(modifier),
        onClick = onClick,
        label = label,
        selected = selected,
        leadingIcon = leadingIcon,
    )
}

/***********
 * PREVIEW *
 ***********/

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview
@Composable
private fun Preview_FlowFilterChip() {
    PluviaTheme {
        Surface {
            Column {
                FlowFilterChip(
                    selected = true,
                    onClick = { },
                    label = { Text(text = "Chip 1") },
                    leadingIcon = { Icon(imageVector = Icons.Default.FilterList, contentDescription = null) },
                )
                FlowFilterChip(
                    selected = false,
                    onClick = { },
                    label = { Text(text = "Chip 2") },
                    leadingIcon = { Icon(imageVector = Icons.Default.FilterList, contentDescription = null) },
                )
            }
        }
    }
}
