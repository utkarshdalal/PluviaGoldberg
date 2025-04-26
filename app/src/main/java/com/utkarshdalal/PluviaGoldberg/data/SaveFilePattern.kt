package com.utkarshdalal.PluviaGoldberg.data

import com.utkarshdalal.PluviaGoldberg.enums.PathType
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
