package app.gamenative.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.gamenative.data.ChangeNumbers

@Dao
interface ChangeNumbersDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(changeNumber: ChangeNumbers)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(changeNumbers: List<ChangeNumbers>)

    @Transaction
    suspend fun insert(appId: Int, changeNumber: Long) {
        insert(ChangeNumbers(appId, changeNumber))
    }

    @Update
    suspend fun update(changeNumber: ChangeNumbers)

    @Delete
    suspend fun delete(changeNumber: ChangeNumbers)

    @Query("DELETE FROM app_change_numbers WHERE appId = :appId")
    suspend fun deleteByAppId(appId: Int)

    @Query("SELECT * FROM app_change_numbers WHERE appId = :appId")
    suspend fun getByAppId(appId: Int): ChangeNumbers?

    @Query("SELECT * FROM app_change_numbers")
    suspend fun getAll(): List<ChangeNumbers>

    @Query("UPDATE app_change_numbers SET changeNumber = :newChangeNumber WHERE appId = :appId")
    suspend fun updateChangeNumber(appId: Int, newChangeNumber: Long)

    @Query("DELETE from app_change_numbers")
    suspend fun deleteAll()
}
