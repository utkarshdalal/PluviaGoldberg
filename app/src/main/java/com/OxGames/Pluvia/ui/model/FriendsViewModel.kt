package com.OxGames.Pluvia.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.OxGames.Pluvia.data.SteamFriend
import com.OxGames.Pluvia.db.dao.SteamFriendDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendsState(
    val friendsList: Map<String, List<SteamFriend>> = mapOf()
)

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val steamFriendDao: SteamFriendDao
) : ViewModel() {

    private val _friendsState = MutableStateFlow(FriendsState())
    val friendsState: StateFlow<FriendsState> = _friendsState.asStateFlow()

    init {
        observeFriendList()
    }

    private fun observeFriendList() {
        viewModelScope.launch(Dispatchers.IO) {
            steamFriendDao.getAllFriends().collect { friends ->
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
                                { it.nameOrNickname.lowercase() }
                            )
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