package app.gamenative.db.converters

import android.util.Base64
import androidx.room.TypeConverter

class ByteArrayConverter {
    @TypeConverter
    fun fromByteArray(byteArray: ByteArray): String = Base64.encodeToString(byteArray, Base64.DEFAULT)

    @TypeConverter
    fun toByteArray(value: String): ByteArray = Base64.decode(value, Base64.DEFAULT)
}
