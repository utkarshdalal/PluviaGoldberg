package com.OxGames.Pluvia.ui.data

import com.OxGames.Pluvia.data.LibraryItem
import com.OxGames.Pluvia.ui.enums.FabFilter
import java.util.EnumSet

data class LibraryState(
    val appInfoSortType: EnumSet<FabFilter> = EnumSet.of(FabFilter.ALPHABETIC, FabFilter.GAME),
    val appInfoList: List<LibraryItem> = emptyList(),

    val isSearching: Boolean = false,
    val searchQuery: String = "",
)
