package com.utkarshdalal.PluviaGoldberg.ui.data

import com.utkarshdalal.PluviaGoldberg.ui.enums.AppOptionMenuType

data class AppMenuOption(
    val optionType: AppOptionMenuType,
    val onClick: () -> Unit,
)
