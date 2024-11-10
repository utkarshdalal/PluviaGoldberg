package com.OxGames.Pluvia.data

import com.OxGames.Pluvia.enums.Language

data class LibraryHeroInfo(
    val image: Map<Language, String>,
    val image2x: Map<Language, String>
)