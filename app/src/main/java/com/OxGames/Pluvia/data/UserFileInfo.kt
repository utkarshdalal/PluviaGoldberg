package com.OxGames.Pluvia.data

import com.OxGames.Pluvia.enums.PathType
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.pathString
import kotlinx.serialization.Serializable

/**
 * @param timestamp the value in milliseconds, since the epoch (1970-01-01T00:00:00Z)
 */
@Serializable
data class UserFileInfo(
    val root: PathType,
    val path: String,
    val filename: String,
    val timestamp: Long,
    val sha: ByteArray,
) {
    fun getPrefix(): String {
        return Paths.get("%${root.name}%$path").pathString
    }
    fun getPrefixPath(): String {
        return Paths.get(getPrefix(), filename).pathString
    }
    fun getAbsPath(prefixToPath: (String) -> String): Path {
        return Paths.get(prefixToPath(root.toString()), path, filename)
    }
}
