package com.OxGames.Pluvia.ui.data

import com.OxGames.Pluvia.ui.enums.PluviaScreen

data class MainState(
    val resettedScreen: PluviaScreen? = null,
    val currentScreen: PluviaScreen = PluviaScreen.LoginUser,
    val hasLaunched: Boolean = false,
    val loadingDialogVisible: Boolean = false,
    val loadingDialogProgress: Float = 0F,
    val annoyingDialogShown: Boolean = false,
    val hasCrashedLastStart: Boolean = false,
    val isSteamConnected: Boolean = false,
    val launchedAppId: Int = 0,
    val bootToContainer: Boolean = false,
)
