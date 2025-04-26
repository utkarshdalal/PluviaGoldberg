package com.utkarshdalal.PluviaGoldberg.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.utkarshdalal.PluviaGoldberg.data.FriendMessage
import `in`.dragonbra.javasteam.types.SteamID
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendMessagesDao {
    @Insert
    suspend fun insertMessage(message: FriendMessage)

    @Insert
    suspend fun insertMessages(messages: List<FriendMessage>)

    @Delete
    suspend fun deleteMessage(message: FriendMessage)

    @Query("DELETE FROM chat_message")
    suspend fun deleteAllMessages()

    @Query("DELETE FROM chat_message WHERE steam_id_friend = :steamId")
    suspend fun deleteAllMessagesForFriend(steamId: SteamID)

    @Query("SELECT * FROM chat_message WHERE steam_id_friend = :steamId ORDER BY timestamp DESC")
    fun getAllMessagesForFriend(steamId: Long): Flow<List<FriendMessage>>

    @Update
    suspend fun updateMessage(message: FriendMessage)

    @Query("SELECT COUNT(*) FROM chat_message WHERE steam_id_friend = :steamId")
    fun getMessageCountForFriend(steamId: SteamID): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM chat_message WHERE steam_id_friend = :steamId AND timestamp = :timestamp AND message = :message)")
    suspend fun messageExists(steamId: Long, timestamp: Int, message: String): Boolean

    @Transaction
    suspend fun insertMessageIfNotExists(message: FriendMessage): Boolean {
        val exists = messageExists(
            steamId = message.steamIDFriend,
            timestamp = message.timestamp,
            message = message.message,
        )

        if (!exists) {
            insertMessage(message)
            return true
        }

        return false
    }

    @Transaction
    suspend fun insertMessagesIfNotExist(messages: List<FriendMessage>): List<FriendMessage> {
        val insertedMessages = mutableListOf<FriendMessage>()

        messages.forEach { message ->
            val exists = messageExists(
                steamId = message.steamIDFriend,
                timestamp = message.timestamp,
                message = message.message,
            )

            if (!exists) {
                insertMessage(message)
                insertedMessages.add(message)
            }
        }

        return insertedMessages
    }
}
