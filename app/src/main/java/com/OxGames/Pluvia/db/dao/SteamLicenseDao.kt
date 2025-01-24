package com.OxGames.Pluvia.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.OxGames.Pluvia.data.SteamLicense
import kotlinx.coroutines.flow.Flow

@Dao
interface SteamLicenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg license: SteamLicense)

    @Update
    suspend fun update(license: SteamLicense)


    @Query("UPDATE steam_license SET app_ids = :appIds WHERE id = :packageId")
    suspend fun updateApps(packageId: Int, appIds: List<Int>)

    @Query("UPDATE steam_license SET depot_ids = :depotIds WHERE id = :packageId")
    suspend fun updateDepots(packageId: Int, depotIds: List<Int>)

    @Query("SELECT * FROM steam_license")
    fun getAllLicenses(): Flow<List<SteamLicense>>

    @Query("SELECT * FROM steam_license WHERE id = :packageId")
    fun findLicense(packageId: Int): Flow<SteamLicense?>

    @Query("DELETE from steam_license")
    suspend fun deleteAll()
}
