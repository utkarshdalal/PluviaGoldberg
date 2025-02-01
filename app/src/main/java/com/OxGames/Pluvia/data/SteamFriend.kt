package com.OxGames.Pluvia.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Web
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.OxGames.Pluvia.ui.component.icons.VR
import com.OxGames.Pluvia.ui.theme.friendAwayOrSnooze
import com.OxGames.Pluvia.ui.theme.friendBlocked
import com.OxGames.Pluvia.ui.theme.friendInGame
import com.OxGames.Pluvia.ui.theme.friendInGameAwayOrSnooze
import com.OxGames.Pluvia.ui.theme.friendOffline
import com.OxGames.Pluvia.ui.theme.friendOnline
import `in`.dragonbra.javasteam.enums.EClientPersonaStateFlag
import `in`.dragonbra.javasteam.enums.EFriendRelationship
import `in`.dragonbra.javasteam.enums.EPersonaState
import `in`.dragonbra.javasteam.enums.EPersonaStateFlag
import `in`.dragonbra.javasteam.types.GameID
import `in`.dragonbra.javasteam.types.SteamID
import java.util.Date
import java.util.EnumSet

@Entity("steam_friend")
data class SteamFriend(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "relation")
    val relation: EFriendRelationship = EFriendRelationship.None,
    @ColumnInfo("status_flags")
    val statusFlags: EnumSet<EClientPersonaStateFlag> = EClientPersonaStateFlag.from(0),
    @ColumnInfo("state")
    val state: EPersonaState = EPersonaState.Offline,
    @ColumnInfo("state_flags")
    val stateFlags: EnumSet<EPersonaStateFlag> = EPersonaStateFlag.from(0),
    @ColumnInfo("game_app_id")
    val gameAppID: Int = 0,
    @ColumnInfo("game_id")
    val gameID: GameID = GameID(),
    @ColumnInfo("game_name")
    val gameName: String = "",
    @ColumnInfo("game_server_ip")
    val gameServerIP: Int = 0,
    @ColumnInfo("game_server_port")
    val gameServerPort: Int = 0,
    @ColumnInfo("query_port")
    val queryPort: Int = 0,
    @ColumnInfo("source_steam_id")
    val sourceSteamID: SteamID = SteamID(),
    @ColumnInfo("game_data_blob")
    val gameDataBlob: String = "",
    @ColumnInfo("name")
    val name: String = "",
    @ColumnInfo("nickname")
    val nickname: String = "",
    @ColumnInfo("avatar_hash")
    val avatarHash: String = "",
    @ColumnInfo("last_log_off")
    val lastLogOff: Date = Date(0),
    @ColumnInfo("last_log_on")
    val lastLogOn: Date = Date(0),
    @ColumnInfo("clan_rank")
    val clanRank: Int = 0,
    @ColumnInfo("clan_tag")
    val clanTag: String = "",
    @ColumnInfo("online_session_instances")
    val onlineSessionInstances: Int = 0,

    // Chat message
    @ColumnInfo("chat_entry_type")
    val isTyping: Boolean = false,
    @ColumnInfo("unread_messages")
    val unreadMessageCount: Int = 0,
) {
    val isOnline: Boolean
        get() = (state.code() in 1..6)

    val isOffline: Boolean
        get() = state == EPersonaState.Offline

    val nameOrNickname: String
        get() = nickname.ifEmpty { name.ifEmpty { "<unknown>" } }

    val isPlayingGame: Boolean
        get() = if (isOnline) gameAppID > 0 || gameName.isEmpty().not() else false

    val isPlayingGameName: String
        get() = if (isPlayingGame) {
            gameName.ifEmpty { "Playing game id: $gameAppID" }
        } else {
            if (isBlocked) {
                relation.name
            } else {
                state.name
            }
        }

    val isAwayOrSnooze: Boolean
        get() = state.let {
            it == EPersonaState.Away || it == EPersonaState.Snooze || it == EPersonaState.Busy
        }

    val isInGameAwayOrSnooze: Boolean
        get() = isPlayingGame && isAwayOrSnooze

    val isRequestRecipient: Boolean
        get() = relation == EFriendRelationship.RequestRecipient

    val isBlocked: Boolean
        get() = relation == EFriendRelationship.Blocked ||
            relation == EFriendRelationship.Ignored ||
            relation == EFriendRelationship.IgnoredFriend

    val isFriend: Boolean
        get() = relation == EFriendRelationship.Friend

    val statusColor: Color
        get() = when {
            isBlocked -> friendBlocked
            isOffline -> friendOffline
            isInGameAwayOrSnooze -> friendInGameAwayOrSnooze
            isAwayOrSnooze -> friendAwayOrSnooze
            isPlayingGame -> friendInGame
            isOnline -> friendOnline
            else -> friendOffline
        }

    val statusIcon: ImageVector?
        get() = when {
            isRequestRecipient -> Icons.Default.PersonAddAlt1
            isAwayOrSnooze -> Icons.Default.Bedtime
            stateFlags.contains(EPersonaStateFlag.ClientTypeVR) -> Icons.Default.VR
            stateFlags.contains(EPersonaStateFlag.ClientTypeTenfoot) -> Icons.Default.SportsEsports
            stateFlags.contains(EPersonaStateFlag.ClientTypeMobile) -> Icons.Default.Smartphone
            stateFlags.contains(EPersonaStateFlag.ClientTypeWeb) -> Icons.Default.Web
            else -> null
        }
}
