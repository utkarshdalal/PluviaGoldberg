package com.OxGames.Pluvia.ui.component

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowSize
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.OxGames.Pluvia.ui.enums.PluviaDestination

private val WINDOW_WIDTH_LARGE = 1200.dp

@Composable
internal fun HomeNavigationWrapperUI(
    destination: PluviaDestination,
    onDestination: (PluviaDestination) -> Unit,
    content: @Composable () -> Unit = {}
) {
    val windowSize = with(LocalDensity.current) {
        currentWindowSize().toSize().toDpSize()
    }
    val navLayoutType = if (windowSize.width >= WINDOW_WIDTH_LARGE) {
        // Show a permanent drawer when window width is large.
        NavigationSuiteType.NavigationDrawer
    } else {
        // Otherwise use the default from NavigationSuiteScaffold.
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
    }

    // TODO play nice with oxters nav,
    //  but also handle our nav first!
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            PluviaDestination.entries.forEach {
                item(
                    label = { Text(stringResource(it.title)) },
                    icon = { Icon(it.icon, stringResource(it.title)) },
                    selected = it == destination,
                    onClick = { onDestination(it) },
                )
            }
        },
        layoutType = navLayoutType,
        content = content
    )
}