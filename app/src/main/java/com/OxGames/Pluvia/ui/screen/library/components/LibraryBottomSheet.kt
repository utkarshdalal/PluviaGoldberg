package com.OxGames.Pluvia.ui.screen.library.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.OxGames.Pluvia.ui.enums.AppFilter
import com.OxGames.Pluvia.ui.theme.PluviaTheme
import java.util.EnumSet

@Composable
fun LibraryBottomSheet(
    selectedFilters: EnumSet<AppFilter>,
    onFilterChanged: (AppFilter) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
    ) {
        Text(text = "Filters", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(18.dp))

        FlowRow {
            FilterChip(
                modifier = Modifier.padding(end = 8.dp),
                onClick = { onFilterChanged(AppFilter.INSTALLED) },
                label = { Text(text = "Installed") },
                selected = selectedFilters.contains(AppFilter.INSTALLED),
                leadingIcon = { Icon(imageVector = Icons.Default.InstallMobile, contentDescription = null) },
            )
            FilterChip(
                modifier = Modifier.padding(end = 8.dp),
                onClick = { onFilterChanged(AppFilter.GAME) },
                label = { Text(text = "Game") },
                selected = selectedFilters.contains(AppFilter.GAME),
                leadingIcon = { Icon(imageVector = Icons.Default.VideogameAsset, contentDescription = null) },
            )
            FilterChip(
                modifier = Modifier.padding(end = 8.dp),
                onClick = { onFilterChanged(AppFilter.APPLICATION) },
                label = { Text(text = "Application") },
                selected = selectedFilters.contains(AppFilter.APPLICATION),
                leadingIcon = { Icon(imageVector = Icons.Default.Computer, contentDescription = null) },
            )
            FilterChip(
                modifier = Modifier.padding(end = 8.dp),
                onClick = { onFilterChanged(AppFilter.TOOL) },
                label = { Text(text = "Tool") },
                selected = selectedFilters.contains(AppFilter.TOOL),
                leadingIcon = { Icon(imageVector = Icons.Default.Build, contentDescription = null) },
            )
            FilterChip(
                modifier = Modifier.padding(end = 8.dp),
                onClick = { onFilterChanged(AppFilter.DEMO) },
                label = { Text(text = "Demo") },
                selected = selectedFilters.contains(AppFilter.DEMO),
                leadingIcon = { Icon(imageVector = Icons.Default.AvTimer, contentDescription = null) },
            )
        }

        Spacer(modifier = Modifier.height(32.dp)) // A little extra padding.
    }
}

/***********
 * PREVIEW *
 ***********/

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview
@Composable
private fun Preview_LibraryBottomSheet() {
    PluviaTheme {
        Surface {
            LibraryBottomSheet(
                selectedFilters = EnumSet.of(AppFilter.GAME, AppFilter.DEMO),
                onFilterChanged = { },
            )
        }
    }
}

// Note: Previews seem to be broken for this, run it manually

// @OptIn(ExperimentalMaterial3Api::class)
// @Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
// @Preview
// @Composable
// private fun Preview_LibraryBottomSheet_AsSheet() {
//    PluviaTheme {
//        Scaffold { paddingValues ->
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues),
//            ) {
//                Text(text = "Hello World")
//
//
//                ModalBottomSheet(
//                    onDismissRequest = { },
//                    content = { LibraryBottomSheet() },
//                )
//            }
//        }
//    }
// }
