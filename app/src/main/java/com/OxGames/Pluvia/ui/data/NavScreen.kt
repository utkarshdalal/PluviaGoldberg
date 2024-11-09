package com.OxGames.Pluvia.ui.data

import com.OxGames.Pluvia.ui.enums.NavType
import com.OxGames.Pluvia.ui.enums.ScreenType
import com.OxGames.Pluvia.ui.enums.PluviaScreen
import java.util.EnumSet

data class NavScreen(
    val name: PluviaScreen,
    val screenType: ScreenType,
    val navType: EnumSet<NavType>
) {
    val hasMenu: Boolean
        get() = navType.contains(NavType.MENU)
}