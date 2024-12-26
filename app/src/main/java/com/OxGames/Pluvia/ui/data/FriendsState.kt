package com.OxGames.Pluvia.ui.data

import com.OxGames.Pluvia.data.SteamFriend

data class FriendsState(
    val friendsList: Map<String, List<SteamFriend>> = mapOf(),
)
