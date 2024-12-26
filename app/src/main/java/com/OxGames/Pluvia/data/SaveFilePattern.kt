package com.OxGames.Pluvia.data

import com.OxGames.Pluvia.enums.PathType

data class SaveFilePattern(
    val root: PathType,
    val path: String,
    val pattern: String,
) {
    val prefix: String
        get() = "%${root.name}%$path"
}
