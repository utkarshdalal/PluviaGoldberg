package com.OxGames.Pluvia.ui.data

import com.OxGames.Pluvia.ui.enums.HomeDestination

data class HomeState(
    val currentDestination: HomeDestination = HomeDestination.Library,
    val confirmExit: Boolean = false,
)
