package com.OxGames.Pluvia.data

import `in`.dragonbra.javasteam.enums.EClientPersonaStateFlag
import `in`.dragonbra.javasteam.enums.EPersonaState
import `in`.dragonbra.javasteam.enums.EPersonaStateFlag
import `in`.dragonbra.javasteam.types.GameID
import `in`.dragonbra.javasteam.types.SteamID
import java.net.InetAddress
import java.util.Date
import java.util.EnumSet

data class UserData(
    //
    // Summary:
    //     Gets the status flags. This shows what has changed.
    //
    // Value:
    //     The status flags.
    var statusFlags: EnumSet<EClientPersonaStateFlag>,
    //
    // Summary:
    //     Gets the friend ID.
    //
    // Value:
    //     The friend ID.
    var friendID: SteamID,
    //
    // Summary:
    //     Gets the state.
    //
    // Value:
    //     The state.
    var state: EPersonaState,
    //
    // Summary:
    //     Gets the state flags.
    //
    // Value:
    //     The state flags.
    var stateFlags: EnumSet<EPersonaStateFlag>,
    //
    // Summary:
    //     Gets the game app ID.
    //
    // Value:
    //     The game app ID.
    var gameAppID: Int,
    //
    // Summary:
    //     Gets the game ID.
    //
    // Value:
    //     The game ID.
    var gameID: GameID,
    //
    // Summary:
    //     Gets the name of the game.
    //
    // Value:
    //     The name of the game.
    var gameName: String,
    //
    // Summary:
    //     Gets the game server IP.
    //
    // Value:
    //     The game server IP.
    var gameServerIP: InetAddress,
    //
    // Summary:
    //     Gets the game server port.
    //
    // Value:
    //     The game server port.
    var gameServerPort: Int,
    //
    // Summary:
    //     Gets the query port.
    //
    // Value:
    //     The query port.
    var queryPort: Int,
    //
    // Summary:
    //     Gets the source steam ID.
    //
    // Value:
    //     The source steam ID.
    var sourceSteamID: SteamID,
    //
    // Summary:
    //     Gets the game data blob.
    //
    // Value:
    //     The game data blob.
    var gameDataBlob: ByteArray,
    //
    // Summary:
    //     Gets the name.
    //
    // Value:
    //     The name.
    var name: String,
    //
    // Summary:
    //     Gets the avatar hash.
    //
    // Value:
    //     The avatar hash.
    var avatarUrl: String,
    //
    // Summary:
    //     Gets the last log off.
    //
    // Value:
    //     The last log off.
    var lastLogOff: Date,
    //
    // Summary:
    //     Gets the last log on.
    //
    // Value:
    //     The last log on.
    var lastLogOn: Date,
    //
    // Summary:
    //     Gets the clan rank.
    //
    // Value:
    //     The clan rank.
    var clanRank: Int,
    //
    // Summary:
    //     Gets the clan tag.
    //
    // Value:
    //     The clan tag.
    var clanTag: String,
    //
    // Summary:
    //     Gets the online session instances.
    //
    // Value:
    //     The online session instances.
    var onlineSessionInstances: Int,
)