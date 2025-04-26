package com.utkarshdalal.PluviaGoldberg.ui.data

import com.utkarshdalal.PluviaGoldberg.PrefManager
import com.utkarshdalal.PluviaGoldberg.ui.enums.HomeDestination

data class HomeState(
    val currentDestination: HomeDestination = PrefManager.startScreen,
    val confirmExit: Boolean = false,
)
