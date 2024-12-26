package com.OxGames.Pluvia.db.converters

import android.util.Base64
import androidx.room.TypeConverter

class ByteArrayConverter {
    @TypeConverter
    fun fromByteArray(byteArray: ByteArray): String {
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    @TypeConverter
    fun toByteArray(value: String): ByteArray {
        return Base64.decode(value, Base64.DEFAULT)
    }
}
