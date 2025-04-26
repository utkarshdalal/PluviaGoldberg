package com.utkarshdalal.PluviaGoldberg.data

import com.utkarshdalal.PluviaGoldberg.db.serializers.OsEnumSetSerializer
import com.utkarshdalal.PluviaGoldberg.enums.OS
import com.utkarshdalal.PluviaGoldberg.enums.OSArch
import java.util.EnumSet
import kotlinx.serialization.Serializable

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
