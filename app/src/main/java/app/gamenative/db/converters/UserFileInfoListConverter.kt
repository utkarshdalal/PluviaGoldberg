package app.gamenative.db.converters

import androidx.room.TypeConverter
import app.gamenative.data.UserFileInfo
import kotlinx.serialization.json.Json

class UserFileInfoListConverter {
    @TypeConverter
    fun fromUserFileInfoList(userFileInfoList: List<UserFileInfo>?): String? = userFileInfoList?.let { Json.encodeToString(it) }

    @TypeConverter
    fun toUserFileInfoList(value: String?): List<UserFileInfo>? = value?.let { Json.decodeFromString<List<UserFileInfo>>(it) }
}
