package com.OxGames.Pluvia.ui.data

import com.OxGames.Pluvia.ui.enums.AppOptionMenuType

data class AppMenuOption(
    val optionType: AppOptionMenuType,
    val onClick: () -> Unit,
)