package com.OxGames.Pluvia.data

import com.OxGames.Pluvia.Constants

/**
 * Data class for the Library list
 */
data class LibraryItem(
    val index: Int = 0,
    val appId: Int = 0,
    val name: String = "",
    val iconHash: String = "",
    val isShared: Boolean = false,
) {
    val clientIconUrl: String
        get() = Constants.Library.ICON_URL + "$appId/$iconHash.ico"
}
