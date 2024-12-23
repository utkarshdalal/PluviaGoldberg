package com.OxGames.Pluvia.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.OxGames.Pluvia.data.SteamFriend
import com.OxGames.Pluvia.db.converters.FriendConverter
import com.OxGames.Pluvia.db.dao.SteamFriendDao

const val DATABASE_NAME = "pluvia.db"

@Database(
    entities = [SteamFriend::class],
    version = 1,
    exportSchema = false, // Should export once stable.
)
@TypeConverters(FriendConverter::class)
abstract class PluviaDatabase : RoomDatabase() {

    abstract fun steamFriendDao(): SteamFriendDao
}