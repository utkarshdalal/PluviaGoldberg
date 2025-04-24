package com.OxGames.Pluvia.data

import com.OxGames.Pluvia.Constants

/**
 * Data class for the Library list UI representation
 */
data class LibraryItem(
    val index: Int = 0,
    val appId: Int = 0,
    val name: String = "",
    val iconHash: String = "",
    val isShared: Boolean = false,
    val isDrmFree: Boolean? = null, // Status from PCGW (null if unknown/unchecked)
    val drmCheckTimestamp: Long? = null // Add timestamp field
) {
    val clientIconUrl: String
        get() = Constants.Library.ICON_URL + "$appId/$iconHash.ico"
}
