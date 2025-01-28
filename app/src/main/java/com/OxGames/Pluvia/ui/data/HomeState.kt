package com.OxGames.Pluvia.ui.data

import com.OxGames.Pluvia.PrefManager
import com.OxGames.Pluvia.ui.enums.HomeDestination

data class HomeState(
    // Allow user to set screen for launch. But we also respect pressing back to go to the Library.
    val currentDestination: HomeDestination = PrefManager.startScreen,
    val confirmExit: Boolean = false,
)
