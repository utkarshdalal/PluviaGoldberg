package com.OxGames.Pluvia.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.OxGames.Pluvia.data.SteamFriend
import com.OxGames.Pluvia.db.dao.SteamFriendDao

const val DATABASE_NAME = "pluvia.db"

@Database(
    entities = [SteamFriend::class],
    version = 1,
    exportSchema = true
)
abstract class PluviaDatabase : RoomDatabase() {

    abstract fun steamFriendDao(): SteamFriendDao
}