package com.utkarshdalal.PluviaGoldberg.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.utkarshdalal.PluviaGoldberg.data.Emoticon
import kotlinx.coroutines.flow.Flow

@Dao
interface EmoticonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(emoticons: List<Emoticon>)

    @Query("SELECT * FROM emoticon ORDER BY isSticker DESC, appID DESC, name DESC")
    fun getAll(): Flow<List<Emoticon>>

    @Query("SELECT * FROM emoticon ORDER BY isSticker DESC, appID DESC, name DESC")
    fun getAllAsList(): List<Emoticon>

    @Query("DELETE FROM emoticon")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(emoticons: List<Emoticon>) {
        deleteAll()
        insertAll(emoticons)
    }

    @Query("SELECT COUNT(*) FROM emoticon")
    fun getCount(): Flow<Int>

    @Query("SELECT * FROM emoticon WHERE isSticker = :isSticker ORDER BY name ASC")
    fun getByType(isSticker: Boolean): Flow<List<Emoticon>>
}
