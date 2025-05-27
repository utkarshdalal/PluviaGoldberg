package app.gamenative.ui.data

import app.gamenative.data.Emoticon
import app.gamenative.data.FriendMessage
import app.gamenative.data.SteamFriend

data class ChatState(
    val friend: SteamFriend = SteamFriend(0),
    val messages: List<FriendMessage> = listOf(),
    val emoticons: List<Emoticon> = listOf(),
    val isLoading: Boolean = true,
)
