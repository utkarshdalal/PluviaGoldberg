package app.gamenative.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.gamenative.data.FileChangeLists
import app.gamenative.data.UserFileInfo

@Dao
interface FileChangeListsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fileChangeLists: FileChangeLists)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fileChangeLists: List<FileChangeLists>)

    @Transaction
    suspend fun insert(appId: Int, userFileInfo: List<UserFileInfo>) {
        insert(FileChangeLists(appId, userFileInfo))
    }

    @Update
    suspend fun update(fileChangeLists: FileChangeLists)

    @Delete
    suspend fun delete(fileChangeLists: FileChangeLists)

    @Query("DELETE FROM app_file_change_lists WHERE appId = :appId")
    suspend fun deleteByAppId(appId: Int)

    @Query("SELECT * FROM app_file_change_lists WHERE appId = :appId")
    suspend fun getByAppId(appId: Int): FileChangeLists?

    @Query("SELECT * FROM app_file_change_lists")
    suspend fun getAll(): List<FileChangeLists>

    @Query("UPDATE app_file_change_lists SET userFileInfo = :newUserFileInfo WHERE appId = :appId")
    suspend fun updateUserFileInfo(appId: Int, newUserFileInfo: List<UserFileInfo>)

    @Query("DELETE from app_file_change_lists")
    suspend fun deleteAll()
}
