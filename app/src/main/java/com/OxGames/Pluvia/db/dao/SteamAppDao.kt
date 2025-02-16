package com.OxGames.Pluvia.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.OxGames.Pluvia.data.SteamApp
import com.OxGames.Pluvia.service.SteamService.Companion.INVALID_PKG_ID
import `in`.dragonbra.javasteam.enums.ELicenseFlags
import kotlinx.coroutines.flow.Flow

@Dao
interface SteamAppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(apps: SteamApp)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(apps: List<SteamApp>)

    @Update
    suspend fun update(app: SteamApp)

    @Query(
        "SELECT * FROM steam_app " +
            "WHERE id != 480 " + // Actively filter out Spacewar
            "AND (owner_account_id IN (:ownerIds) OR license_flags & :borrowedCode = :borrowedCode) " +
            "AND package_id != :invalidPkgId " +
            "AND type != 0 " +
            "ORDER BY LOWER(name)",
    )
    fun getAllOwnedApps(
        ownerIds: List<Int>,
        invalidPkgId: Int = INVALID_PKG_ID,
        borrowedCode: Int = ELicenseFlags.Borrowed.code(),
    ): Flow<List<SteamApp>>

    @Query("SELECT * FROM steam_app WHERE received_pics = 0 AND package_id != :invalidPkgId AND owner_account_id = :ownerId")
    fun getAllOwnedAppsWithoutPICS(
        ownerId: Int,
        invalidPkgId: Int = INVALID_PKG_ID,
    ): List<SteamApp>

    @Query("SELECT * FROM steam_app WHERE id = :appId")
    suspend fun findApp(appId: Int): SteamApp?

    @Query("DELETE from steam_app")
    suspend fun deleteAll()
}
