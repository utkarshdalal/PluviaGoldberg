package app.gamenative.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("app_file_change_lists")
data class FileChangeLists(
    @PrimaryKey val appId: Int? = null,
    val userFileInfo: List<UserFileInfo> = emptyList(),
)
