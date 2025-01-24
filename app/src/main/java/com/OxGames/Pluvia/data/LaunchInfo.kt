package com.OxGames.Pluvia.data

import com.OxGames.Pluvia.db.serializers.OsEnumSetSerializer
import com.OxGames.Pluvia.enums.OS
import com.OxGames.Pluvia.enums.OSArch
import kotlinx.serialization.Serializable
import java.util.EnumSet

@Serializable
data class LaunchInfo(
    val executable: String,
    val workingDir: String,
    val description: String,
    val type: String,
    @Serializable(with = OsEnumSetSerializer::class)
    val configOS: EnumSet<OS>,
    val configArch: OSArch,
)
