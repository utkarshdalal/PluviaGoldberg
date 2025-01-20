package com.OxGames.Pluvia.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.OxGames.Pluvia.data.ChangeNumbers
import com.OxGames.Pluvia.data.FileChangeLists
import com.OxGames.Pluvia.data.SteamFriend
import com.OxGames.Pluvia.db.converters.ByteArrayConverter
import com.OxGames.Pluvia.db.converters.FriendConverter
import com.OxGames.Pluvia.db.converters.PathTypeConverter
import com.OxGames.Pluvia.db.converters.UserFileInfoListConverter
import com.OxGames.Pluvia.db.dao.ChangeNumbersDao
import com.OxGames.Pluvia.db.dao.FileChangeListsDao
import com.OxGames.Pluvia.db.dao.SteamFriendDao

const val DATABASE_NAME = "pluvia.db"

@Database(
    entities = [SteamFriend::class, ChangeNumbers::class, FileChangeLists::class],
    version = 1,
    exportSchema = false, // Should export once stable.
)
@TypeConverters(
    FriendConverter::class,
    PathTypeConverter::class,
    ByteArrayConverter::class,
    UserFileInfoListConverter::class,
)
abstract class PluviaDatabase : RoomDatabase() {

    abstract fun steamFriendDao(): SteamFriendDao

    abstract fun appChangeNumbersDao(): ChangeNumbersDao

    abstract fun appFileChangeListsDao(): FileChangeListsDao
}
