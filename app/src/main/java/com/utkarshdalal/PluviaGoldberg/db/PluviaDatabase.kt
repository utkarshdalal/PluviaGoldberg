package com.utkarshdalal.PluviaGoldberg.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.utkarshdalal.PluviaGoldberg.data.ChangeNumbers
import com.utkarshdalal.PluviaGoldberg.data.Emoticon
import com.utkarshdalal.PluviaGoldberg.data.FileChangeLists
import com.utkarshdalal.PluviaGoldberg.data.FriendMessage
import com.utkarshdalal.PluviaGoldberg.data.SteamApp
import com.utkarshdalal.PluviaGoldberg.data.SteamFriend
import com.utkarshdalal.PluviaGoldberg.data.SteamLicense
import com.utkarshdalal.PluviaGoldberg.db.converters.AppConverter
import com.utkarshdalal.PluviaGoldberg.db.converters.ByteArrayConverter
import com.utkarshdalal.PluviaGoldberg.db.converters.FriendConverter
import com.utkarshdalal.PluviaGoldberg.db.converters.LicenseConverter
import com.utkarshdalal.PluviaGoldberg.db.converters.PathTypeConverter
import com.utkarshdalal.PluviaGoldberg.db.converters.UserFileInfoListConverter
import com.utkarshdalal.PluviaGoldberg.db.dao.ChangeNumbersDao
import com.utkarshdalal.PluviaGoldberg.db.dao.EmoticonDao
import com.utkarshdalal.PluviaGoldberg.db.dao.FileChangeListsDao
import com.utkarshdalal.PluviaGoldberg.db.dao.FriendMessagesDao
import com.utkarshdalal.PluviaGoldberg.db.dao.SteamAppDao
import com.utkarshdalal.PluviaGoldberg.db.dao.SteamFriendDao
import com.utkarshdalal.PluviaGoldberg.db.dao.SteamLicenseDao

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
    version = 3,
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
