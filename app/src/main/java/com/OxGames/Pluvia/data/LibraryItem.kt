package com.OxGames.Pluvia.data

/**
 * Data class for the Library list
 */
data class LibraryItem(
    val index: Int = 0,
    val appId: Int = 0,
    val name: String = "",
    val iconHash: String = "",
) {
    val clientIconUrl: String
        get() = "https://steamcdn-a.akamaihd.net/steamcommunity/public/images/apps/$appId/$iconHash.ico"
}
