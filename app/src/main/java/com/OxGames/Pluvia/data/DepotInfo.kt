package com.OxGames.Pluvia.data

import com.OxGames.Pluvia.enums.OS
import java.util.EnumSet

data class DepotInfo(
    val depotId: Int,
    val depotFromApp: Int,
    val sharedInstall: Boolean,
    val osList: EnumSet<OS>,
    val manifests: Map<String, ManifestInfo>,
    val encryptedManifests: Map<String, ManifestInfo>,
)