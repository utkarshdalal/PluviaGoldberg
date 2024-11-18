package com.OxGames.Pluvia.data

import com.OxGames.Pluvia.enums.OS
import com.OxGames.Pluvia.enums.OSArch
import java.util.EnumSet

data class LaunchInfo(
    val executable: String,
    val workingDir: String,
    val description: String,
    val type: String,
    val configOS: EnumSet<OS>,
    val configArch: OSArch,
)
