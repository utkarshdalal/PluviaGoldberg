package com.OxGames.Pluvia.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.OxGames.Pluvia.PrefManager
import com.OxGames.Pluvia.db.dao.SteamFriendDao
import com.OxGames.Pluvia.service.SteamService
import com.OxGames.Pluvia.ui.data.FriendsState
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

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val steamFriendDao: SteamFriendDao,
) : ViewModel() {

    private val _friendsState = MutableStateFlow(FriendsState())
    val friendsState: StateFlow<FriendsState> = _friendsState.asStateFlow()

    private var selectedFriendJob: Job? = null
    private var observeFriendListJob: Job? = null

    init {
        observeFriendList()
    }

    override fun onCleared() {
        selectedFriendJob?.cancel()
        observeFriendListJob?.cancel()
    }

    fun observeSelectedFriend(friendID: Long) {
        selectedFriendJob?.cancel()

        // Force clear states when this method if is called again.
        _friendsState.update { it.copy(profileFriend = null, profileFriendGames = emptyList(), profileFriendInfo = null) }

        viewModelScope.launch {
            val resp = SteamService.getProfileInfo(SteamID(friendID))
            _friendsState.update { it.copy(profileFriendInfo = resp) }
        }

        viewModelScope.launch {
            val resp = SteamService.getOwnedGames(friendID)
            _friendsState.update { it.copy(profileFriendGames = resp) }
        }

        selectedFriendJob = viewModelScope.launch {
            steamFriendDao.findFriendFlow(friendID).collect { friend ->
                _friendsState.update { it.copy(profileFriend = friend) }
            }
        }
    }

    fun onHeaderAction(value: String) {
        // TODO save value as preference & restore it
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
