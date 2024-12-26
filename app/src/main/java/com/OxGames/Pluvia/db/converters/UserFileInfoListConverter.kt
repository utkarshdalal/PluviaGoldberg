package com.OxGames.Pluvia.db.converters

import androidx.room.TypeConverter
import com.OxGames.Pluvia.data.UserFileInfo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UserFileInfoListConverter {
    @TypeConverter
    fun fromUserFileInfoList(userFileInfoList: List<UserFileInfo>?): String? {
        return userFileInfoList?.let { list ->
            Json.encodeToString(list)
        }
    }

    @TypeConverter
    fun toUserFileInfoList(value: String?): List<UserFileInfo>? {
        return value?.let {
            Json.decodeFromString<List<UserFileInfo>>(it)
        }
    }
}
