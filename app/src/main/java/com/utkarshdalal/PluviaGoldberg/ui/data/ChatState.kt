package com.utkarshdalal.PluviaGoldberg.ui.data

import com.utkarshdalal.PluviaGoldberg.data.Emoticon
import com.utkarshdalal.PluviaGoldberg.data.FriendMessage
import com.utkarshdalal.PluviaGoldberg.data.SteamFriend

data class ChatState(
    val friend: SteamFriend = SteamFriend(0),
    val messages: List<FriendMessage> = listOf(),
    val emoticons: List<Emoticon> = listOf(),
    val isLoading: Boolean = true,
)
