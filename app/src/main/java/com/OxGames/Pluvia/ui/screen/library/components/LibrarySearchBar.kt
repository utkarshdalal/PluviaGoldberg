package com.OxGames.Pluvia.ui.screen.library.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.OxGames.Pluvia.data.LibraryItem
import com.OxGames.Pluvia.ui.component.topbar.AccountButton
import com.OxGames.Pluvia.ui.data.LibraryState
import com.OxGames.Pluvia.ui.internal.fakeAppInfo
import com.OxGames.Pluvia.ui.theme.PluviaTheme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
internal fun LibrarySearchBar(
    state: LibraryState,
    listState: LazyListState,
    onIsSearching: (Boolean) -> Unit,
    onSearchQuery: (String) -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
    onItemClick: (Int) -> Unit,
) {
    // Debouncer: Scroll to the top after a short amount of time after typing quickly
    val internalSearchText = remember { MutableStateFlow(state.searchQuery) }
    LaunchedEffect(Unit) {
        internalSearchText.debounce(500).collect {
            Timber.d("Debounced: Scrolling to top")
            listState.scrollToItem(0)
        }
    }

    // Lambda function to provide new test to both onSearchQuery and internalSearchText
    val onSearchText: (String) -> Unit = {
        onSearchQuery(it)
        internalSearchText.value = it
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        SearchBar(
            modifier = Modifier
                .semantics { traversalIndex = 0f },
            expanded = state.isSearching,
            onExpandedChange = onIsSearching,
            inputField = {
                SearchBarDefaults.InputField(
                    query = state.searchQuery,
                    onSearch = {
                        // This is invoked when IME search is pressed.
                        // But we have nothing to utilize this for.
                    },
                    expanded = state.isSearching,
                    onExpandedChange = onIsSearching,
                    placeholder = { Text(text = "Search for games") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        Crossfade(state.isSearching) { cfState ->
                            if (cfState) {
                                IconButton(
                                    onClick = {
                                        if (state.searchQuery.isEmpty()) {
                                            onIsSearching(false)
                                        } else {
                                            onSearchText("")
                                        }
                                    },
                                    content = {
                                        Icon(Icons.Default.Clear, "Clear search query")
                                    },
                                )
                            } else {
                                AccountButton(
                                    onSettings = onSettings,
                                    onLogout = onLogout,
                                )
                            }
                        }
                    },
                    onQueryChange = onSearchText,
                )
            },
            content = {
                if (state.isSearching) {
                    LibraryList(
                        contentPaddingValues = PaddingValues(bottom = 72.dp),
                        listState = listState,
                        list = state.appInfoList,
                        onItemClick = onItemClick,
                    )
                }
            },
        )
    }
}

/***********
 * PREVIEW *
 ***********/

@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES or android.content.res.Configuration.UI_MODE_TYPE_NORMAL)
@Preview
@Composable
private fun Preview_LibrarySearchBar() {
    PluviaTheme {
        Surface {
            LibrarySearchBar(
                state = LibraryState(
                    isSearching = true,
                    appInfoList = List(5) { idx ->
                        val item = fakeAppInfo(idx)
                        LibraryItem(
                            index = idx,
                            appId = item.id,
                            name = item.name,
                            iconHash = item.iconHash,
                        )
                    },
                ),
                listState = rememberLazyListState(),
                onIsSearching = { },
                onSearchQuery = { },
                onSettings = { },
                onLogout = { },
                onItemClick = { },
            )
        }
    }
}
