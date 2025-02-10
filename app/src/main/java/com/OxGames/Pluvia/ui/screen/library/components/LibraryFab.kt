package com.OxGames.Pluvia.ui.screen.library.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.OxGames.Pluvia.ui.component.fabmenu.FloatingActionMenu
import com.OxGames.Pluvia.ui.component.fabmenu.FloatingActionMenuItem
import com.OxGames.Pluvia.ui.component.fabmenu.state.FloatingActionMenuState
import com.OxGames.Pluvia.ui.data.LibraryState
import com.OxGames.Pluvia.ui.enums.FabFilter

@Composable
internal fun LibraryFab(
    fabState: FloatingActionMenuState,
    state: LibraryState,
    onFabFilter: (FabFilter) -> Unit,
) {
    FloatingActionMenu(
        state = fabState,
        imageVector = Icons.Filled.FilterList,
        closeImageVector = Icons.Filled.Close,
    ) {
        FloatingActionMenuItem(
            labelText = "Installed",
            isSelected = state.appInfoSortType.contains(FabFilter.INSTALLED),
            onClick = {
                onFabFilter(FabFilter.INSTALLED)
                fabState.close()
            },
            content = { Icon(Icons.Filled.InstallMobile, "Installed") },
        )
        FloatingActionMenuItem(
            labelText = "Game",
            isSelected = state.appInfoSortType.contains(FabFilter.GAME),
            onClick = {
                onFabFilter(FabFilter.GAME)
                fabState.close()
            },
            content = { Icon(Icons.Filled.VideogameAsset, "Game") },
        )
        FloatingActionMenuItem(
            labelText = "Application",
            isSelected = state.appInfoSortType.contains(FabFilter.APPLICATION),
            onClick = {
                onFabFilter(FabFilter.APPLICATION)
                fabState.close()
            },
            content = { Icon(Icons.Filled.Computer, "Application") },
        )
        FloatingActionMenuItem(
            labelText = "Tool",
            isSelected = state.appInfoSortType.contains(FabFilter.TOOL),
            onClick = {
                onFabFilter(FabFilter.TOOL)
                fabState.close()
            },
            content = { Icon(Icons.Filled.Build, "Tool") },
        )
        FloatingActionMenuItem(
            labelText = "Demo",
            isSelected = state.appInfoSortType.contains(FabFilter.DEMO),
            onClick = {
                onFabFilter(FabFilter.DEMO)
                fabState.close()
            },
            content = { Icon(Icons.Filled.AvTimer, "Demo") },
        )
    }
}

/***********
 * PREVIEW *
 ***********/
