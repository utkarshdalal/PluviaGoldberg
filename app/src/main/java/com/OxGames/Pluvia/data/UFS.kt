package com.OxGames.Pluvia.data

data class UFS(
    val quota: Int,
    val maxNumFiles: Int,
    val saveFiles: Array<SaveFile>,
)