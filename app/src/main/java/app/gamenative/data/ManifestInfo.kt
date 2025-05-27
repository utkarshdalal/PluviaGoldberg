package app.gamenative.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer

@Serializable
data class ManifestInfo(
    val name: String,
    @Serializable(with = LongAsStringSerializer::class)
    val gid: Long,
    @Serializable(with = LongAsStringSerializer::class)
    val size: Long,
    @Serializable(with = LongAsStringSerializer::class)
    val download: Long,
)
