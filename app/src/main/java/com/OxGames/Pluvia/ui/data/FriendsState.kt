package com.OxGames.Pluvia.ui.data

import com.OxGames.Pluvia.data.OwnedGames
import com.OxGames.Pluvia.data.SteamFriend
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.ProfileInfoCallback

data class FriendsState(
    val friendsList: Map<String, List<SteamFriend>> = emptyMap(),
    val profileFriend: SteamFriend? = null,
    val profileFriendInfo: ProfileInfoCallback? = null,
    val profileFriendGames: List<OwnedGames> = emptyList(),
)
