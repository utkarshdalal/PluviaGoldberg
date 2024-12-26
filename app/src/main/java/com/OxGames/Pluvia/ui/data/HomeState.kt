package com.OxGames.Pluvia.ui.data

import com.OxGames.Pluvia.ui.enums.PluviaDestination

data class HomeState(
    val currentDestination: PluviaDestination = PluviaDestination.Library,
    val confirmExit: Boolean = false,
)
