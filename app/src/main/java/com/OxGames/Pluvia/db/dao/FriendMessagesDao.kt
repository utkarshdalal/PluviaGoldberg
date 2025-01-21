package com.OxGames.Pluvia.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.OxGames.Pluvia.data.FriendMessage
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
    fun getAllMessagesForFriend(steamId: SteamID): Flow<List<FriendMessage>>

    @Update
    suspend fun updateMessage(message: FriendMessage)

    @Query("SELECT COUNT(*) FROM chat_message WHERE steam_id_friend = :steamId")
    fun getMessageCountForFriend(steamId: SteamID): Flow<Int>
}
