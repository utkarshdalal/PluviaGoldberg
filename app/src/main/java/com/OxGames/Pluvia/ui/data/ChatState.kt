package com.OxGames.Pluvia.ui.data

import com.OxGames.Pluvia.data.Emoticon
import com.OxGames.Pluvia.data.FriendMessage
import com.OxGames.Pluvia.data.SteamFriend

data class ChatState(
    val friend: SteamFriend = SteamFriend(0),
    val messages: List<FriendMessage> = listOf(),
    val emoticons: List<Emoticon> = listOf(),
    val isLoading: Boolean = true,
)
