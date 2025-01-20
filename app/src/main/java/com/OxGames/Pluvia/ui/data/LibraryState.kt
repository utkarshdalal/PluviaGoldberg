package com.OxGames.Pluvia.ui.data

import com.OxGames.Pluvia.data.AppInfo
import com.OxGames.Pluvia.ui.enums.FabFilter

data class LibraryState(
    val appInfoSortType: FabFilter = FabFilter.ALPHABETIC,
    val appInfoList: List<AppInfo> = listOf(),

    val isSearching: Boolean = false,
    val searchQuery: String = "",
)
