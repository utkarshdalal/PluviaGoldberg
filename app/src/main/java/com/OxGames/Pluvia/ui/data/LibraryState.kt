package com.OxGames.Pluvia.ui.data

import com.OxGames.Pluvia.data.AppInfo

data class LibraryState(
    val searchText: String = "",
    val searchAlphabetic: Boolean = false,
    val searchInstalled: Boolean = false,
    val appInfoList: List<AppInfo> = listOf(),
)