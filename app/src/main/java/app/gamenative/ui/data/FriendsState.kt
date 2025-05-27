package app.gamenative.ui.data

import app.gamenative.PrefManager
import app.gamenative.data.OwnedGames
import app.gamenative.data.SteamFriend
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.ProfileInfoCallback

data class FriendsState(
    val friendsList: Map<String, List<SteamFriend>> = emptyMap(),
    val collapsedListSections: Set<String> = PrefManager.friendsListHeader,
    val profileFriend: SteamFriend? = null,
    val profileFriendInfo: ProfileInfoCallback? = null,
    val profileFriendGames: List<OwnedGames> = emptyList(),
    val profileFriendAlias: List<String> = emptyList(),
)
