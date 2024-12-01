package com.OxGames.Pluvia.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.OxGames.Pluvia.data.SteamFriend
import com.OxGames.Pluvia.db.dao.SteamFriendDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendsState(
    val friendsList: List<SteamFriend> = listOf()
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
        viewModelScope.launch {
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

                    currentState.copy(friendsList =sortedList )
                }
            }
        }
    }
}