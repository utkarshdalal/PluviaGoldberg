package app.gamenative.data

import app.gamenative.enums.PathType
import kotlinx.serialization.Serializable

@Serializable
data class SaveFilePattern(
    val root: PathType,
    val path: String,
    val pattern: String,
) {
    val prefix: String
        get() = "%${root.name}%$path"
}
