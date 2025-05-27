package app.gamenative.data

import `in`.dragonbra.javasteam.steam.handlers.steamapps.AppProcessInfo

data class GameProcessInfo(
    val appId: Int,
    val branch: String = "public",
    val processes: List<AppProcessInfo>,
)
