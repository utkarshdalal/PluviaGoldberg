package com.OxGames.Pluvia.ui.model

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.data.LibraryItem
import com.OxGames.Pluvia.enums.AppType
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.service.SteamService
import com.OxGames.Pluvia.ui.data.LibraryState
import com.OxGames.Pluvia.ui.enums.FabFilter
import java.util.EnumSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

class LibraryViewModel : ViewModel() {
    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    // Keep the library scroll state. This will last longer as the VM will stay alive.
    var listState: LazyListState by mutableStateOf(LazyListState(0, 0))

    private val onAppInfoReceived: (SteamEvent.AppInfoReceived) -> Unit = {
        getAppList()

        Timber.d("Updating games list with ${state.value.appInfoList.count()} item(s)")
    }

    init {
        PluviaApp.events.on<SteamEvent.AppInfoReceived, Unit>(onAppInfoReceived)

        getAppList()
    }

    override fun onCleared() {
        Timber.d("onCleared")
        PluviaApp.events.off<SteamEvent.AppInfoReceived, Unit>(onAppInfoReceived)
    }

    fun onIsSearching(value: Boolean) {
        _state.update { it.copy(isSearching = value) }

        if (!value) {
            getAppList()
        }
    }

    fun onSearchQuery(value: String) {
        _state.update { it.copy(searchQuery = value) }

        getAppList()
    }

    fun onFabFilter(value: FabFilter) {
        _state.update { it.copy(appInfoSortType = value) }

        getAppList()
    }

    private fun getAppList() {
        val list = with(state.value) {
            SteamService.getAppList(EnumSet.of(AppType.game))
                .filter { if (appInfoSortType == FabFilter.INSTALLED) SteamService.isAppInstalled(it.appId) else true }
                .filter { it.name.contains(searchQuery, true) }
                .let {
                    if (appInfoSortType == FabFilter.ALPHABETIC) {
                        it.sortedBy { appInfo -> appInfo.name }
                    } else {
                        it.sortedBy { appInfo -> appInfo.receiveIndex }.reversed()
                    }
                }
        }.mapIndexed { idx, item ->
            // Slim down the list with only the necessary values.
            LibraryItem(
                index = idx,
                appId = item.appId,
                name = item.name,
                iconHash = item.clientIconHash,
            )
        }

        _state.update { it.copy(appInfoList = list) }
    }
}
