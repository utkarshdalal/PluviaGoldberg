package com.OxGames.Pluvia.data

import com.OxGames.Pluvia.db.serializers.OsEnumSetSerializer
import com.OxGames.Pluvia.enums.OS
import com.OxGames.Pluvia.enums.OSArch
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
