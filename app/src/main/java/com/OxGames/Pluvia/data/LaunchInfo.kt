package com.OxGames.Pluvia.data

import com.OxGames.Pluvia.enums.OS
import java.util.EnumSet

data class LaunchInfo(
    val executable: String,
    val description: String,
    val type: String,
    val configOS: EnumSet<OS>,
)
