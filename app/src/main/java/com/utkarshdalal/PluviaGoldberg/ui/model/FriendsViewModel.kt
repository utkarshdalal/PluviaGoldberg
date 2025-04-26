package com.utkarshdalal.PluviaGoldberg.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utkarshdalal.PluviaGoldberg.PluviaApp
import com.utkarshdalal.PluviaGoldberg.PrefManager
import com.utkarshdalal.PluviaGoldberg.data.OwnedGames
import com.utkarshdalal.PluviaGoldberg.db.dao.SteamFriendDao
import com.utkarshdalal.PluviaGoldberg.events.SteamEvent
import com.utkarshdalal.PluviaGoldberg.service.SteamService
import com.utkarshdalal.PluviaGoldberg.ui.data.FriendsState
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.dragonbra.javasteam.types.SteamID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val steamFriendDao: SteamFriendDao,
) : ViewModel() {

    private val _friendsState = MutableStateFlow(FriendsState())
    val friendsState: StateFlow<FriendsState> = _friendsState.asStateFlow()

    private var selectedFriendJob: Job? = null
    private var observeFriendListJob: Job? = null

    private val onAliasHistory: (SteamEvent.OnAliasHistory) -> Unit = {
        _friendsState.update { currentState -> currentState.copy(profileFriendAlias = it.names) }
    }

    init {
        observeFriendList()
        PluviaApp.events.on<SteamEvent.OnAliasHistory, Unit>(onAliasHistory)
    }

    override fun onCleared() {
        Timber.d("onCleared")

        selectedFriendJob?.cancel()
        observeFriendListJob?.cancel()
        PluviaApp.events.off<SteamEvent.OnAliasHistory, Unit>(onAliasHistory)
    }

    fun observeSelectedFriend(friendID: Long) {
        selectedFriendJob?.cancel()

        // Force clear states when this method if is called again.
        _friendsState.update {
            it.copy(
                profileFriend = null,
                profileFriendGames = emptyList(),
                profileFriendInfo = null,
                profileFriendAlias = emptyList(),
            )
        }

        viewModelScope.launch {
            launch {
                val resp = SteamService.getProfileInfo(SteamID(friendID))
                _friendsState.update { it.copy(profileFriendInfo = resp) }
            }
            launch {
                val resp = SteamService.getOwnedGames(friendID).sortedWith(
                    compareBy<OwnedGames> { (it.sortAs ?: it.name).lowercase() }
                        .thenByDescending { it.playtimeTwoWeeks },
                )

                resp.forEach {
                    Timber.d(it.toString())
                }

                _friendsState.update { it.copy(profileFriendGames = resp) }
            }
            selectedFriendJob = launch {
                steamFriendDao.findFriendFlow(friendID).collect { friend ->
                    if (friend == null) {
                        Timber.w("Collecting friend was null")
                        return@collect
                    }
                    _friendsState.update { it.copy(profileFriend = friend) }
                }
            }
        }
    }

    fun onHeaderAction(value: String) {
        _friendsState.update { currentState ->
            val list = currentState.collapsedListSections.toMutableSet()
            if (value in list) {
                list.remove(value)
            } else {
                list.add(value)
            }
            PrefManager.friendsListHeader = list
            currentState.copy(collapsedListSections = list)
        }
    }

    fun onBlock(friendID: Long) {
        viewModelScope.launch {
            SteamService.blockFriend(friendID)
        }
    }

    fun onRemove(friendID: Long) {
        viewModelScope.launch {
            SteamService.removeFriend(friendID)
        }
    }

    fun onNickName(value: String) {
        viewModelScope.launch {
            SteamService.setNickName(_friendsState.value.profileFriend!!.id, value)
        }
    }

    fun onAlias() {
        viewModelScope.launch {
            SteamService.requestAliasHistory(_friendsState.value.profileFriend!!.id)
        }
    }

    private fun observeFriendList() {
        observeFriendListJob = viewModelScope.launch(Dispatchers.IO) {
            steamFriendDao.getAllFriendsFlow().collect { friends ->
                _friendsState.update { currentState ->
                    val sortedList = friends
                        .filter { it.isFriend && !it.isBlocked }
                        .sortedWith(
                            compareBy(
                                { it.isRequestRecipient.not() },
                                { it.isPlayingGame.not() },
                                { it.isInGameAwayOrSnooze },
                                { it.isOnline.not() },
                                { it.isAwayOrSnooze },
                                { it.isOffline.not() },
                                { it.nameOrNickname.lowercase() },
                            ),
                        )

                    val groupedList = sortedList.groupBy { friend ->
                        when {
                            friend.isRequestRecipient -> "Friend Request"
                            friend.isPlayingGame || friend.isInGameAwayOrSnooze -> "In-Game"
                            friend.isOnline || friend.isAwayOrSnooze -> "Online"
                            else -> "Offline"
                        }
                    }.toMap()

                    currentState.copy(friendsList = groupedList)
                }
            }
        }
    }
}
