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
import com.OxGames.Pluvia.service.PcgwApiService
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

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val steamAppDao: SteamAppDao,
    private val pcgwApiService: PcgwApiService
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    // Keep the library scroll state. This will last longer as the VM will stay alive.
    var listState: LazyListState by mutableStateOf(LazyListState(0, 0))

    // Complete and unfiltered app list
    private var appList: List<SteamApp> = emptyList()

    // Define cache duration (e.g., 7 days)
    private val drmCacheDurationMs = 7 * 24 * 60 * 60 * 1000L

    init {
        viewModelScope.launch(Dispatchers.IO) {
            steamAppDao.getAllOwnedApps(
                // ownerIds = SteamService.familyMembers.ifEmpty { listOf(SteamService.userSteamId!!.accountID.toInt()) },
            ).collect { apps ->
                Timber.tag("LibraryViewModel").d("Collecting ${apps.size} apps")

                appList = apps

                // Initial filter pass
                onFilterApps()

                // Trigger DRM checks in the background after initial list is processed
                checkAndFetchDrmStatuses(apps)
            }
        }
    }

    // Function to map SteamApp to LibraryItem, now including DRM status
    private fun mapToLibraryItem(steamApp: SteamApp): LibraryItem {
        return LibraryItem(
            appId = steamApp.id,
            name = steamApp.name,
            iconHash = steamApp.clientIconHash, // Make sure this field exists and is correct
            isShared = !steamApp.ownerAccountId.contains(SteamService.userSteamId!!.accountID.toInt()),
            isDrmFree = steamApp.isDrmFree // Use cached value
        )
    }

    private fun checkAndFetchDrmStatuses(appsToCheck: List<SteamApp>) {
        viewModelScope.launch(Dispatchers.IO) { // Use IO dispatcher for API/DB work
            val now = System.currentTimeMillis()
            appsToCheck.forEach { app ->
                // Check if status is unknown or cache is expired
                val needsCheck = app.isDrmFree == null ||
                    (app.drmCheckTimestamp ?: 0L) < (now - drmCacheDurationMs)

                if (needsCheck) {
                    Timber.d("Checking DRM status for ${app.id} (${app.name})")
                    val drmStatus = pcgwApiService.getDrmStatus(app.id)

                    // Only update if status was successfully determined (not null)
                    // This prevents overwriting a known status with null if the API fails later
                    if (drmStatus != null) {
                        steamAppDao.updateDrmStatus(app.id, drmStatus, now)
                        // Update the UI state immediately for this item
                        _state.update { currentState ->
                            val updatedList = currentState.appInfoList.map {
                                if (it.appId == app.id) {
                                    it.copy(isDrmFree = drmStatus)
                                } else {
                                    it
                                }
                            }
                            currentState.copy(appInfoList = updatedList)
                        }
                    } else {
                         // Optional: Could update timestamp even on failure to avoid retrying immediately
                         // steamAppDao.updateDrmStatus(app.id, app.isDrmFree, now)
                        Timber.w("DRM check failed or returned null for ${app.id}, keeping previous status.")
                    }
                    // Add a small delay to avoid overwhelming the API
                    delay(300) // Slightly increased delay
                }
            }
            Timber.d("Finished DRM status check routine.")
        }
    }

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

    // TODO: include other sort types
    fun onFilterChanged(value: AppFilter) {
        _state.update { currentState ->
            val updatedFilter = EnumSet.copyOf(currentState.appInfoSortType)

            if (updatedFilter.contains(value)) {
                updatedFilter.remove(value)
            } else {
                updatedFilter.add(value)
            }

            PrefManager.libraryFilter = updatedFilter

            currentState.copy(appInfoSortType = updatedFilter)
        }

        onFilterApps()
    }

    // Refactored filtering to use the central mapToLibraryItem function
    private fun onFilterApps() {
        Timber.tag("LibraryViewModel").d("onFilterApps")
        viewModelScope.launch(Dispatchers.Default) { // Use Default dispatcher for filtering/mapping
            val currentState = _state.value
            val currentFilter = AppFilter.getAppType(currentState.appInfoSortType)

            val filteredList = appList
                .asSequence()
                 // Apply owner/sharing filter first
                .filter { item ->
                    val isOwned = item.ownerAccountId.contains(SteamService.userSteamId!!.accountID.toInt())
                    // If SHARED filter is active, include all. Otherwise, only include owned.
                    currentState.appInfoSortType.contains(AppFilter.SHARED) || isOwned
                }
                 // Apply type filter (Game, DLC, etc.)
                .filter { item ->
                    currentFilter.any { item.type == it }
                }
                 // Apply search query filter
                .filter { item ->
                    currentState.searchQuery.isBlank() || item.name.contains(currentState.searchQuery, ignoreCase = true)
                }
                 // Apply installed filter
                .filter { item ->
                    !currentState.appInfoSortType.contains(AppFilter.INSTALLED) || SteamService.isAppInstalled(item.id)
                }
                // Map to LibraryItem, including the cached DRM status
                .mapIndexed { idx, item -> mapToLibraryItem(item).copy(index = idx) } // Use mapToLibraryItem here
                .toList()

            Timber.tag("LibraryViewModel").d("Filtered list size: ${filteredList.size}")
            // Update the state with the fully mapped and filtered list
            _state.update { it.copy(appInfoList = filteredList) }
        }
    }

    // Remember to close the Ktor client when the ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        pcgwApiService.close()
        Timber.d("LibraryViewModel cleared, Ktor client closed.")
    }
}
