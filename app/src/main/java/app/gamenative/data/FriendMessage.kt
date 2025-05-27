package app.gamenative.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("chat_message")
data class FriendMessage(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Long = 0L,
    @ColumnInfo(name = "steam_id_friend") val steamIDFriend: Long,
    @ColumnInfo(name = "from_local") val fromLocal: Boolean,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "timestamp") val timestamp: Int,
)
