package com.OxGames.Pluvia.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.OxGames.Pluvia.data.SteamFriend
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.PlayerNickname
import kotlinx.coroutines.flow.Flow

@Dao
interface SteamFriendDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(friend: SteamFriend)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(friends: List<SteamFriend>)

    @Update
    suspend fun update(friend: SteamFriend)

    @Transaction
    suspend fun updateNicknames(nickname: List<PlayerNickname>) {
        nickname.forEach {
            updateNicknameInternal(it.steamID.convertToUInt64(), it.nickname)
        }
    }

    @Query("UPDATE steam_friend SET nickname = :newNickname WHERE id = :friendId")
    suspend fun updateNicknameInternal(friendId: Long, newNickname: String)

    @Query("UPDATE steam_friend SET nickname = ''")
    suspend fun clearAllNicknames()

    @Query("SELECT * FROM steam_friend ORDER BY name ASC")
    fun getAllFriendsFlow(): Flow<List<SteamFriend>>

    @Query("SELECT * FROM steam_friend WHERE id = :id")
    fun findFriendFlow(id: Long): Flow<SteamFriend?>

    @Query("SELECT * FROM steam_friend WHERE id = :id")
    fun findFriend(id: Long): SteamFriend?

    @Query("SELECT * FROM steam_friend WHERE name LIKE '%' || :name || '%' OR nickname LIKE '%' || :name || '%'")
    fun findFriendFlow(name: String): Flow<List<SteamFriend>>
}
