package com.OxGames.Pluvia.data

import com.OxGames.Pluvia.enums.OS
import com.OxGames.Pluvia.enums.OSArch
import java.util.EnumSet

data class DepotInfo(
    val depotId: Int,
    val depotFromApp: Int,
    val sharedInstall: Boolean,
    val osList: EnumSet<OS>,
    val osArch: OSArch,
    val manifests: Map<String, ManifestInfo>,
    val encryptedManifests: Map<String, ManifestInfo>,
)