package com.OxGames.Pluvia.data

import com.OxGames.Pluvia.enums.SyncResult
import `in`.dragonbra.javasteam.steam.handlers.steamcloud.PendingRemoteOperation

data class PostSyncInfo(
    val syncResult: SyncResult,
    val remoteTimestamp: Long = 0,
    val localTimestamp: Long = 0,
    val uploadsRequired: Boolean = false,
    val uploadsCompleted: Boolean = false,
    val pendingRemoteOperations: List<PendingRemoteOperation> = emptyList(),
)