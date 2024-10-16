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
    /**
     * Gets the status flags. This shows what has changed.
     */
    val statusFlags: EnumSet<EClientPersonaStateFlag>,
    /**
     * Gets the friend ID.
     */
    val friendID: SteamID,
    /**
     * Gets the state.
     */
    val state: EPersonaState,
    /**
     * Gets the state flags.
     */
    val stateFlags: EnumSet<EPersonaStateFlag>,
    /**
     * Gets the game app ID.
     */
    val gameAppID: Int,
    /**
     * Gets the game ID.
     */
    val gameID: GameID,
    /**
     * Gets the name of the game.
     */
    val gameName: String,
    /**
     * Gets the game server IP.
     */
    val gameServerIP: InetAddress,
    /**
     * Gets the game server port.
     */
    val gameServerPort: Int,
    /**
     * Gets the query port.
     */
    val queryPort: Int,
    /**
     * Gets the source steam ID.
     */
    val sourceSteamID: SteamID,
    /**
     * Gets the game data blob.
     */
    val gameDataBlob: ByteArray,
    /**
     * Gets the name.
     */
    val name: String,
    /**
     * Gets the avatar hash.
     */
    val avatarUrl: String,
    /**
     * Gets the last log off.
     */
    val lastLogOff: Date,
    /**
     * Gets the last log on.
     */
    val lastLogOn: Date,
    /**
     * Gets the clan rank.
     */
    val clanRank: Int,
    /**
     * Gets the clan tag.
     */
    val clanTag: String,
    /**
     * Gets the online session instances.
     */
    val onlineSessionInstances: Int,
)