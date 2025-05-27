package app.gamenative.ui.data

import app.gamenative.PrefManager
import app.gamenative.ui.enums.HomeDestination

data class HomeState(
    val currentDestination: HomeDestination = PrefManager.startScreen,
    val confirmExit: Boolean = false,
)
