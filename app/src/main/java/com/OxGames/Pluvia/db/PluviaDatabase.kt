package com.OxGames.Pluvia.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.OxGames.Pluvia.data.ChangeNumbers
import com.OxGames.Pluvia.data.Emoticon
import com.OxGames.Pluvia.data.FileChangeLists
import com.OxGames.Pluvia.data.FriendMessage
import com.OxGames.Pluvia.data.SteamApp
import com.OxGames.Pluvia.data.SteamFriend
import com.OxGames.Pluvia.data.SteamLicense
import com.OxGames.Pluvia.db.converters.AppConverter
import com.OxGames.Pluvia.db.converters.ByteArrayConverter
import com.OxGames.Pluvia.db.converters.FriendConverter
import com.OxGames.Pluvia.db.converters.LicenseConverter
import com.OxGames.Pluvia.db.converters.PathTypeConverter
import com.OxGames.Pluvia.db.converters.UserFileInfoListConverter
import com.OxGames.Pluvia.db.dao.ChangeNumbersDao
import com.OxGames.Pluvia.db.dao.EmoticonDao
import com.OxGames.Pluvia.db.dao.FileChangeListsDao
import com.OxGames.Pluvia.db.dao.FriendMessagesDao
import com.OxGames.Pluvia.db.dao.SteamAppDao
import com.OxGames.Pluvia.db.dao.SteamFriendDao
import com.OxGames.Pluvia.db.dao.SteamLicenseDao

const val DATABASE_NAME = "pluvia.db"

@Database(
    entities = [
        SteamApp::class,
        SteamLicense::class,
        SteamFriend::class,
        ChangeNumbers::class,
        FileChangeLists::class,
        FriendMessage::class,
        Emoticon::class,
    ],
    version = 2,
    exportSchema = false, // Should export once stable.
)
@TypeConverters(
    AppConverter::class,
    ByteArrayConverter::class,
    FriendConverter::class,
    LicenseConverter::class,
    PathTypeConverter::class,
    UserFileInfoListConverter::class,
)
abstract class PluviaDatabase : RoomDatabase() {

    abstract fun steamLicenseDao(): SteamLicenseDao

    abstract fun steamAppDao(): SteamAppDao

    abstract fun steamFriendDao(): SteamFriendDao

    abstract fun appChangeNumbersDao(): ChangeNumbersDao

    abstract fun appFileChangeListsDao(): FileChangeListsDao

    abstract fun friendMessagesDao(): FriendMessagesDao

    abstract fun emoticonDao(): EmoticonDao
}
