package com.OxGames.Pluvia.ui.data

import com.OxGames.Pluvia.PrefManager
import com.OxGames.Pluvia.ui.enums.HomeDestination

data class HomeState(
    val currentDestination: HomeDestination = PrefManager.startScreen,
    val confirmExit: Boolean = false,
)
