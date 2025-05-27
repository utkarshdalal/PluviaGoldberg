package app.gamenative.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("app_change_numbers")
data class ChangeNumbers(
    @PrimaryKey val appId: Int? = null,
    val changeNumber: Long? = null,
)
