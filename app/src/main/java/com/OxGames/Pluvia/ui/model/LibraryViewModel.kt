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

    // Keep the library scroll state. This will last longer as the VM will stay alive.
    var listState: LazyListState by mutableStateOf(LazyListState(0, 0))
    
    // Complete and unfiltered app list
    private var appList: List<SteamApp> = emptyList()
    private var previousFilteredList: List<SteamApp> = emptyList()
    private val drmCacheDurationMs = 7 * 24 * 60 * 60 * 1000L
    private val activeDrmChecks = ConcurrentHashMap<Int, Boolean>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            steamAppDao.getAllOwnedApps(
                // ownerIds = SteamService.familyMembers.ifEmpty { listOf(SteamService.userSteamId!!.accountID.toInt()) },
            ).collect { apps ->
                appList = apps
                Timber.tag("LibraryViewModel").d("Collecting ${apps.size} apps")
                onFilterApps(performDrmCheck = true)
            }
        }
    }

    // Helper to map SteamApp to LibraryItem, incorporating DRM status and timestamp
    private fun mapToLibraryItem(steamApp: SteamApp): LibraryItem {
        return LibraryItem(
            appId = steamApp.id,
            name = steamApp.name,
            iconHash = steamApp.clientIconHash, 
            isShared = !steamApp.ownerAccountId.contains(SteamService.userSteamId!!.accountID.toInt()),
            isDrmFree = steamApp.isDrmFree,
            drmCheckTimestamp = steamApp.drmCheckTimestamp // Map the timestamp
        )
    }

    // Function to fetch DRM statuses in the background
    private fun checkAndFetchDrmStatuses(appsToCheck: List<SteamApp>) {
        viewModelScope.launch(Dispatchers.IO) { 
            appsToCheck.forEach { app ->
                // Only proceed if a check for this app is not already active
                if (!activeDrmChecks.containsKey(app.id)) {
                    // Check only based on timestamp staleness
                    val needsCheck = (app.drmCheckTimestamp ?: 0L) < (System.currentTimeMillis() - drmCacheDurationMs)
                    
                    if (needsCheck) {
                        // Mark as active *before* starting the blocking call
                        activeDrmChecks[app.id] = true
                        try {
                            val checkAttemptTimestamp = System.currentTimeMillis() 
                            val drmStatus = PcgwHelper.getDrmStatusBlocking(app.id)

                            if (drmStatus != null) {
                                // Success: Update status and timestamp in DAO
                                if (drmStatus == true) {
                                    Timber.i("Detected ${app.name} (${app.id}) as DRM-free.")
                                }
                                steamAppDao.updateDrmStatus(app.id, drmStatus, checkAttemptTimestamp)
                                
                                // Update UI state with new status AND timestamp
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
                                // Failure: Update only the timestamp in DAO
                                Timber.w("DRM check failed or returned null for ${app.id} (${app.name})")
                                steamAppDao.updateDrmTimestamp(app.id, checkAttemptTimestamp) 
                                
                                // Update UI state with new timestamp ONLY (status remains null)
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
                             // Always remove from active checks when done (success, failure, or exception)
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
        onFilterApps() // Uses default performDrmCheck = false
    }

    // TODO: include other sort types
    fun onFilterChanged(value: AppFilter) {
        _state.update { currentState ->
            val updatedFilter = EnumSet.copyOf(currentState.appInfoSortType)
            if (updatedFilter.contains(value)) updatedFilter.remove(value) else updatedFilter.add(value)
            PrefManager.libraryFilter = updatedFilter
            currentState.copy(appInfoSortType = updatedFilter)
        }
        onFilterApps() // Uses default performDrmCheck = false
    }

    // Applies filters, triggers DRM check for filtered list conditionally, and maps to UI items
    private fun onFilterApps(performDrmCheck: Boolean = false) { // Added parameter
        Timber.tag("LibraryViewModel").d("onFilterApps called (performDrmCheck=$performDrmCheck)")
        // Keep original dispatcher
        viewModelScope.launch { 
             val currentState = _state.value
            val currentFilter = AppFilter.getAppType(currentState.appInfoSortType)

            // Apply all filters first to get the list of SteamApps relevant for display
            val filteredList = appList
                .asSequence()
                 // Reverted to original owner/sharing filter logic from master
                .filter { item ->
                    SteamService.familyMembers.ifEmpty {
                        listOf(SteamService.userSteamId!!.accountID.toInt())
                    }.map {
                        // Corrected variable name inside lambda
                        item.ownerAccountId.contains(it) 
                    }.any()
                }
                .filter { item ->
                    currentFilter.any { item.type == it }
                }
                 // Reverted to original separate shared filter logic
                .filter { item ->
                    if (currentState.appInfoSortType.contains(AppFilter.SHARED)) {
                        true
                    } else {
                        item.ownerAccountId.contains(SteamService.userSteamId!!.accountID.toInt())
                    }
                }
                 // Reverted to original search filter logic
                .filter { item ->
                    if (currentState.searchQuery.isNotEmpty()) {
                        item.name.contains(currentState.searchQuery, ignoreCase = true)
                    } else {
                        true
                    }
                }
                 // Reverted to original installed filter logic
                .filter { item ->
                    if (currentState.appInfoSortType.contains(AppFilter.INSTALLED)) {
                        SteamService.isAppInstalled(item.id)
                    } else {
                        true
                    }
                }
                .toList() // Collect the filtered SteamApp list

            // Change log level to Verbose
            Timber.tag("LibraryViewModel").v("onFilterApps - Filtered to ${filteredList.size} apps for display/DRM check")

            // Trigger DRM check for the filtered list ONLY if requested
            if (performDrmCheck && previousFilteredList.size != filteredList.size) {
                checkAndFetchDrmStatuses(filteredList)
            }

            // Map the filtered list to LibraryItems for the UI state
            val finalLibraryItemList = filteredList
                .mapIndexed { idx, item -> mapToLibraryItem(item).copy(index = idx) } 
                .toList()

            // Change log level to Verbose
            Timber.tag("LibraryViewModel").v("Updating UI with ${finalLibraryItemList.size} items")
            _state.update { it.copy(appInfoList = finalLibraryItemList) }
            previousFilteredList = filteredList;
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("LibraryViewModel cleared.")
    }
}
