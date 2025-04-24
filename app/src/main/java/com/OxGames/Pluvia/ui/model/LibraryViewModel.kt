package com.OxGames.Pluvia.ui.model

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.OxGames.Pluvia.PrefManager
import com.OxGames.Pluvia.data.LibraryItem
import com.OxGames.Pluvia.data.SteamApp
import com.OxGames.Pluvia.db.dao.SteamAppDao
import com.OxGames.Pluvia.utils.PcgwHelper
import com.OxGames.Pluvia.service.SteamService
import com.OxGames.Pluvia.ui.data.LibraryState
import com.OxGames.Pluvia.ui.enums.AppFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.EnumSet
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val steamAppDao: SteamAppDao,
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    var listState: LazyListState by mutableStateOf(LazyListState(0, 0))
    private var appList: List<SteamApp> = emptyList()
    private val drmCacheDurationMs = 7 * 24 * 60 * 60 * 1000L
    private val activeDrmChecks = ConcurrentHashMap<Int, Boolean>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            steamAppDao.getAllOwnedApps().collect { apps ->
                Timber.tag("LibraryViewModel").d("Collecting ${apps.size} apps")
                appList = apps
                // Initial filter pass
                onFilterApps()
            }
        }
    }

    // Helper to map SteamApp to LibraryItem
    private fun mapToLibraryItem(steamApp: SteamApp): LibraryItem {
        return LibraryItem(
            appId = steamApp.id,
            name = steamApp.name,
            iconHash = steamApp.clientIconHash, 
            isShared = !steamApp.ownerAccountId.contains(SteamService.userSteamId!!.accountID.toInt()),
            isDrmFree = steamApp.isDrmFree, // These fields are now populated from DB
            drmCheckTimestamp = steamApp.drmCheckTimestamp
        )
    }

    // Function to fetch DRM statuses in the background
    private fun checkAndFetchDrmStatuses(appsToCheck: List<SteamApp>) {
        viewModelScope.launch(Dispatchers.IO) { 
            appsToCheck.forEach { app ->
                if (!activeDrmChecks.containsKey(app.id)) {
                    val currentDbApp = steamAppDao.getAppById(app.id)
                    if (currentDbApp == null) {
                        Timber.e("DRM Check: Could not find app ${app.id} in DB for timestamp check.")
                        continue 
                    }

                    val needsCheck = (currentDbApp.drmCheckTimestamp ?: 0L) < (System.currentTimeMillis() - drmCacheDurationMs)
                    
                    if (needsCheck) {
                        activeDrmChecks[app.id] = true
                        try {
                            val checkAttemptTimestamp = System.currentTimeMillis() 
                            val drmStatus = PcgwHelper.getDrmStatusBlocking(app.id) 

                            if (drmStatus != null) {
                                // Success
                                if (drmStatus == true) {
                                    Timber.i("Detected ${app.name} (${app.id}) as DRM-free.")
                                }
                                steamAppDao.updateDrmStatus(app.id, drmStatus, checkAttemptTimestamp)
                                _state.update { currentState ->
                                    val updatedList = currentState.appInfoList.map {
                                        if (it.appId == app.id) {
                                            it.copy(isDrmFree = drmStatus, drmCheckTimestamp = checkAttemptTimestamp)
                                        } else {
                                            it
                                        }
                                    }
                                    currentState.copy(appInfoList = updatedList)
                                }
                            } else {
                                // Failure
                                Timber.w("DRM check failed or returned null for ${app.id} (${app.name})")
                                steamAppDao.updateDrmTimestamp(app.id, checkAttemptTimestamp) 
                                 _state.update { currentState ->
                                    val updatedList = currentState.appInfoList.map {
                                        if (it.appId == app.id) {
                                            it.copy(drmCheckTimestamp = checkAttemptTimestamp)
                                        } else {
                                            it
                                        }
                                    }
                                    currentState.copy(appInfoList = updatedList)
                                }
                            }
                        } finally {
                             activeDrmChecks.remove(app.id)
                        }
                        delay(300) 
                    } // end if(needsCheck)
                } // end if(!activeDrmChecks.containsKey)
            } // end forEach
        }
    }

    // --- Existing filter/search/modal logic ---

    fun onModalBottomSheet(value: Boolean) {
        _state.update { it.copy(modalBottomSheet = value) }
    }

    fun onIsSearching(value: Boolean) {
        _state.update { it.copy(isSearching = value) }
        if (!value) {
            onSearchQuery("")
        }
    }

    fun onSearchQuery(value: String) {
        _state.update { it.copy(searchQuery = value) }
        onFilterApps()
    }

    fun onFilterChanged(value: AppFilter) {
        _state.update { currentState ->
            val updatedFilter = EnumSet.copyOf(currentState.appInfoSortType)
            if (updatedFilter.contains(value)) updatedFilter.remove(value) else updatedFilter.add(value)
            PrefManager.libraryFilter = updatedFilter
            currentState.copy(appInfoSortType = updatedFilter)
        }
        onFilterApps()
    }

    // Applies filters and maps to UI items
    private fun onFilterApps() {
        Timber.tag("LibraryViewModel").d("onFilterApps called")
        viewModelScope.launch {
            val currentState = _state.value
            val currentFilter = AppFilter.getAppType(currentState.appInfoSortType)

            val filteredSteamApps = appList
                .asSequence()
                .filter { item ->
                    SteamService.familyMembers.ifEmpty {
                        listOf(SteamService.userSteamId!!.accountID.toInt())
                    }.map {
                        item.ownerAccountId.contains(it)
                    }.any()
                }
                .filter { item ->
                    currentFilter.any { item.type == it }
                }
                .filter { item ->
                    if (currentState.appInfoSortType.contains(AppFilter.SHARED)) {
                        true
                    } else {
                        item.ownerAccountId.contains(SteamService.userSteamId!!.accountID.toInt())
                    }
                }
                .filter { item ->
                    if (currentState.searchQuery.isNotEmpty()) {
                        item.name.contains(currentState.searchQuery, ignoreCase = true)
                    } else {
                        true
                    }
                }
                .filter { item ->
                    if (currentState.appInfoSortType.contains(AppFilter.INSTALLED)) {
                        SteamService.isAppInstalled(item.id)
                    } else {
                        true
                    }
                }
                .toList() 

            Timber.tag("LibraryViewModel").v("onFilterApps - Filtered to ${filteredSteamApps.size} apps for display/DRM check")

            checkAndFetchDrmStatuses(filteredSteamApps)

            val finalLibraryItemList = filteredSteamApps
                .mapIndexed { idx, item -> mapToLibraryItem(item).copy(index = idx) }
                .toList()

            Timber.tag("LibraryViewModel").v("Updating UI with ${finalLibraryItemList.size} items")
            _state.update { it.copy(appInfoList = finalLibraryItemList) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("LibraryViewModel cleared.")
    }
}
