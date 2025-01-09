package com.OxGames.Pluvia.ui.model

import androidx.lifecycle.ViewModel
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.enums.AppType
import com.OxGames.Pluvia.events.SteamEvent
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

    private val onAppInfoReceived: (SteamEvent.AppInfoReceived) -> Unit = {
        getAppList()

        Timber.d("Updating games list with ${state.value.appInfoList.count()} item(s)")
    }

    init {
        PluviaApp.events.on<SteamEvent.AppInfoReceived, Unit>(onAppInfoReceived)

        getAppList()
    }

    override fun onCleared() {
        PluviaApp.events.off<SteamEvent.AppInfoReceived, Unit>(onAppInfoReceived)
    }

    fun onFabFilter(filter: FabFilter) {
        _state.update { currentValue ->
            when (filter) {
                FabFilter.SEARCH -> {
                    Timber.w("Search not implemented!")
                    currentValue.copy()
                }

                FabFilter.INSTALLED -> currentValue.copy(searchInstalled = !currentValue.searchInstalled)
                FabFilter.ALPHABETIC -> currentValue.copy(searchAlphabetic = !currentValue.searchAlphabetic)
            }
        }

        getAppList()
    }

    private fun getAppList() {
        val list = with(state.value) {
            SteamService.getAppList(EnumSet.of(AppType.game))
                .filter { if (searchInstalled) SteamService.isAppInstalled(it.appId) else true }
                .filter { it.name.contains(searchText, true) }
                .let {
                    if (searchAlphabetic) {
                        it.sortedBy { appInfo -> appInfo.name }
                    } else {
                        it.sortedBy { appInfo -> appInfo.receiveIndex }.reversed()
                    }
                }
        }

        _state.update { currentValue ->
            currentValue.copy(appInfoList = list)
        }
    }
}
