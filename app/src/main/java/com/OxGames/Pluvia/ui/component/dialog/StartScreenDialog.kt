package com.OxGames.Pluvia.ui.component.dialog

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.OxGames.Pluvia.PrefManager
import com.OxGames.Pluvia.ui.enums.HomeDestination
import com.OxGames.Pluvia.ui.theme.PluviaTheme

@Composable
fun StartScreenDialog(
    openDialog: Boolean,
    destination: HomeDestination,
    onSelected: (HomeDestination) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!openDialog) {
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
            )
        },
        title = { Text(text = "Start Destination") },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                HomeDestination.entries.forEach { entry ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = entry == destination,
                                onClick = { onSelected(entry) },
                                role = Role.RadioButton,
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = entry == destination,
                            onClick = null,
                        )
                        Text(
                            text = stringResource(id = entry.title),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close")
            }
        },
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview_StartScreenDialog() {
    val content = LocalContext.current
    PrefManager.init(content)

    var destination by remember { mutableStateOf(HomeDestination.Downloads) }

    PluviaTheme {
        StartScreenDialog(
            openDialog = true,
            destination = destination,
            onSelected = { destination = it },
            onDismiss = { },
        )
    }
}
