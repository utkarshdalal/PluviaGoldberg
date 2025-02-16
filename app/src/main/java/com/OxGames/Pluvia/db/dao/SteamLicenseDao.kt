package com.OxGames.Pluvia.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.OxGames.Pluvia.data.SteamLicense

@Dao
interface SteamLicenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(license: List<SteamLicense>)

    @Update
    suspend fun update(license: SteamLicense)

    @Query("UPDATE steam_license SET app_ids = :appIds WHERE packageId = :packageId")
    suspend fun updateApps(packageId: Int, appIds: List<Int>)

    @Query("UPDATE steam_license SET depot_ids = :depotIds WHERE packageId = :packageId")
    suspend fun updateDepots(packageId: Int, depotIds: List<Int>)

    @Query("SELECT * FROM steam_license")
    suspend fun getAllLicenses(): List<SteamLicense>

    @Query("SELECT * FROM steam_license WHERE packageId = :packageId")
    suspend fun findLicense(packageId: Int): SteamLicense?

    @Query("SELECT * FROM steam_license WHERE packageId NOT IN (:packageIds)")
    suspend fun findStaleLicences(packageIds: List<Int>): List<SteamLicense>

    @Query("DELETE FROM steam_license WHERE packageId IN (:packageIds)")
    suspend fun deleteStaleLicenses(packageIds: List<Int>)

    @Query("DELETE from steam_license")
    suspend fun deleteAll()
}
