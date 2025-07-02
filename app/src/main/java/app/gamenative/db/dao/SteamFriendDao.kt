package app.gamenative.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.gamenative.data.SteamFriend
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

    @Update
    suspend fun updateAll(friend: List<SteamFriend>)

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

    @Query("SELECT * FROM steam_friend WHERE game_app_id > 0")
    suspend fun findFriendsInGame(): List<SteamFriend>

    @Query("SELECT * FROM steam_friend WHERE id = :id")
    fun findFriendFlow(id: Long): Flow<SteamFriend?>

    @Query("SELECT * FROM steam_friend WHERE id = :id")
    suspend fun findFriend(id: Long): SteamFriend?

    @Query("SELECT * FROM steam_friend WHERE name LIKE '%' || :name || '%' OR nickname LIKE '%' || :name || '%'")
    fun findFriendFlow(name: String): Flow<List<SteamFriend>>

    @Query("DELETE FROM steam_friend WHERE id = :friendId")
    suspend fun remove(friendId: Long)

    @Query("DELETE from steam_friend")
    suspend fun deleteAll()
}
