package com.OxGames.Pluvia.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.OxGames.Pluvia.data.SteamApp
import com.OxGames.Pluvia.enums.AppType
import com.OxGames.Pluvia.service.SteamService.Companion.INVALID_PKG_ID
import kotlinx.coroutines.flow.Flow
import java.util.EnumSet

@Dao
interface SteamAppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg app: SteamApp)

    @Update
    suspend fun update(app: SteamApp)


    @Query("SELECT * FROM steam_app WHERE package_id != :excludedPkgId AND type != 0 AND type & :filter = type")
    fun getAllAppsWithLicense(filter: Int = AppType.code(EnumSet.allOf(AppType::class.java)), excludedPkgId: Int = INVALID_PKG_ID): Flow<List<SteamApp>>

    @Query("SELECT * FROM steam_app WHERE received_pics = 0")
    fun getAllAppsWithoutPICS(): Flow<List<SteamApp>>

    @Query("SELECT * FROM steam_app WHERE id = :appId")
    fun findApp(appId: Int): Flow<SteamApp?>

    @Query("DELETE from steam_app")
    suspend fun deleteAll()
}
