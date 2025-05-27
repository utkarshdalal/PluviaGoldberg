package app.gamenative.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emoticon")
data class Emoticon(@PrimaryKey val name: String, val appID: Int, val isSticker: Boolean)
