package com.utkarshdalal.PluviaGoldberg.data

import com.utkarshdalal.PluviaGoldberg.db.serializers.OsEnumSetSerializer
import com.utkarshdalal.PluviaGoldberg.enums.OS
import com.utkarshdalal.PluviaGoldberg.enums.OSArch
import java.util.EnumSet
import kotlinx.serialization.Serializable

@Serializable
data class DepotInfo(
    val depotId: Int,
    val dlcAppId: Int,
    val depotFromApp: Int,
    val sharedInstall: Boolean,
    @Serializable(with = OsEnumSetSerializer::class)
    val osList: EnumSet<OS>,
    val osArch: OSArch,
    val manifests: Map<String, ManifestInfo>,
    val encryptedManifests: Map<String, ManifestInfo>,
)
