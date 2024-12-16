package com.OxGames.Pluvia.data

import kotlinx.serialization.Serializable

@Serializable
data class SteamData(
    var cellId: Int = 0,
    var clientId: Long? = null,
    var accountName: String? = null,
    var accessToken: String? = null,
    var refreshToken: String? = null,
    var password: String? = null,
    var appInstallPath: String,
    var appStagingPath: String,
    val appChangeNumbers: MutableMap<Int, Long> = mutableMapOf(),
    val appFileChangeLists: MutableMap<Int, List<UserFileInfo>> = mutableMapOf()
)