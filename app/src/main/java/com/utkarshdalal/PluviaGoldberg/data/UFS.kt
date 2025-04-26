package com.utkarshdalal.PluviaGoldberg.data

import kotlinx.serialization.Serializable

@Serializable
data class UFS(
    val quota: Int = 0,
    val maxNumFiles: Int = 0,
    val saveFilePatterns: List<SaveFilePattern> = emptyList(),
)
