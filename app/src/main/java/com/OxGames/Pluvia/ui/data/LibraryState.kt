package com.OxGames.Pluvia.ui.data

import com.OxGames.Pluvia.data.LibraryItem
import com.OxGames.Pluvia.ui.enums.FabFilter

data class LibraryState(
    val appInfoSortType: FabFilter = FabFilter.ALPHABETIC,
    val appInfoList: List<LibraryItem> = emptyList(),

    val isSearching: Boolean = false,
    val searchQuery: String = "",
)
