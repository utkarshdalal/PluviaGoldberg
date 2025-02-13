package com.OxGames.Pluvia.ui.data

import com.OxGames.Pluvia.PrefManager
import com.OxGames.Pluvia.data.LibraryItem
import com.OxGames.Pluvia.ui.enums.AppFilter
import java.util.EnumSet

data class LibraryState(
    val appInfoSortType: EnumSet<AppFilter> = PrefManager.libraryFilter,
    val appInfoList: List<LibraryItem> = emptyList(),
    val modalBottomSheet: Boolean = false,

    val isSearching: Boolean = false,
    val searchQuery: String = "",
)
