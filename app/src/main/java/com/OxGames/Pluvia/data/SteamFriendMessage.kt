package com.OxGames.Pluvia.data

import `in`.dragonbra.javasteam.enums.EChatEntryType
import `in`.dragonbra.javasteam.types.SteamID

data class SteamFriendMessage(
    val steamID: SteamID,
    val chatEntryType: EChatEntryType,
    val message: String,
    val containsBBCode: Boolean,
    val echoToSender: Boolean,
    val lowPriority: Boolean,
    val serverTimeStamp: Int,
    val clientMessageID: Int = 0,
    val lastMessage: Int = 0,
    val lastView: Int = 0,
)
