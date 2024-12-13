package com.OxGames.Pluvia.data

import com.OxGames.Pluvia.enums.PathType

data class SaveFile(
    val root: PathType,
    val path: String,
    val pattern: String
)