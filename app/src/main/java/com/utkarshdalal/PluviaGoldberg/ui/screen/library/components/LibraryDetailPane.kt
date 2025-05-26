package com.utkarshdalal.PluviaGoldberg.ui.screen.library.components

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.utkarshdalal.PluviaGoldberg.service.SteamService
import com.utkarshdalal.PluviaGoldberg.ui.data.LibraryState
import com.utkarshdalal.PluviaGoldberg.ui.enums.AppFilter
import com.utkarshdalal.PluviaGoldberg.ui.screen.library.AppScreen
import com.utkarshdalal.PluviaGoldberg.ui.theme.PluviaTheme
import java.util.EnumSet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryDetailPane(
    appId: Int,
    onClickPlay: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    Surface {
        if (appId == SteamService.INVALID_APP_ID) {
            // Simply use the regular LibraryListPane with empty data
            val listState = rememberLazyListState()
            val sheetState = rememberModalBottomSheetState()
            val emptyState = remember {
                LibraryState(
                    appInfoList = emptyList(),
                    // Use the same default filter as in PrefManager (GAME)
                    appInfoSortType = EnumSet.of(AppFilter.GAME)
                )
            }
            
            LibraryListPane(
                state = emptyState,
                listState = listState,
                sheetState = sheetState,
                onFilterChanged = {},
                onModalBottomSheet = {},
                onIsSearching = {},
                onLogout = {},
                onNavigate = {},
                onSearchQuery = {},
                onSettings = {},
            )
        } else {
            AppScreen(
                appId = appId,
                onClickPlay = onClickPlay,
                onBack = onBack,
            )
        }
    }
}

/***********
 * PREVIEW *
 ***********/

@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES or android.content.res.Configuration.UI_MODE_TYPE_NORMAL)
@Preview
@Composable
private fun Preview_LibraryDetailPane() {
    PluviaTheme {
        LibraryDetailPane(
            appId = Int.MAX_VALUE,
            onClickPlay = { },
            onBack = { },
        )
    }
}
