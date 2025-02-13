package com.OxGames.Pluvia.ui.data

import com.OxGames.Pluvia.data.LibraryItem
import com.OxGames.Pluvia.ui.enums.AppFilter
import java.util.EnumSet

// TODO:
//  1. Missing games?? Did I break something or...?
//  2. The filter chips do not change their selected color.
//  3. Close button on sheet or not? Tapping outside or swipe down dismisses it.

data class LibraryState(
    val appInfoSortType: EnumSet<AppFilter> = EnumSet.of(AppFilter.ALPHABETIC, AppFilter.GAME), // TODO save as pref
    val appInfoList: List<LibraryItem> = emptyList(),
    val modalBottomSheet: Boolean = false,

    val isSearching: Boolean = false,
    val searchQuery: String = "",
)
