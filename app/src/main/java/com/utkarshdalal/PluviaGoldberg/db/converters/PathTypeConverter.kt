package com.utkarshdalal.PluviaGoldberg.db.converters

import androidx.room.TypeConverter
import com.utkarshdalal.PluviaGoldberg.enums.PathType

class PathTypeConverter {
    @TypeConverter
    fun fromPathType(pathType: PathType): String = pathType.name

    @TypeConverter
    fun toPathType(value: String): PathType = PathType.from(value)
}
