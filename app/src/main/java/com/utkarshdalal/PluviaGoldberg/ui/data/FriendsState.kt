package com.utkarshdalal.PluviaGoldberg.ui.data

import com.utkarshdalal.PluviaGoldberg.PrefManager
import com.utkarshdalal.PluviaGoldberg.data.OwnedGames
import com.utkarshdalal.PluviaGoldberg.data.SteamFriend
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.ProfileInfoCallback

data class FriendsState(
    val friendsList: Map<String, List<SteamFriend>> = emptyMap(),
    val collapsedListSections: Set<String> = PrefManager.friendsListHeader,
    val profileFriend: SteamFriend? = null,
    val profileFriendInfo: ProfileInfoCallback? = null,
    val profileFriendGames: List<OwnedGames> = emptyList(),
    val profileFriendAlias: List<String> = emptyList(),
)
