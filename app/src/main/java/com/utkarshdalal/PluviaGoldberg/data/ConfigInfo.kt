package com.utkarshdalal.PluviaGoldberg.data

import kotlinx.serialization.Serializable

@Serializable
data class ConfigInfo(
    val installDir: String = "",
    val launch: List<LaunchInfo> = emptyList(),
    val steamControllerTemplateIndex: Int = 0,
    val steamControllerTouchTemplateIndex: Int = 0,
    // val steamControllerTouchConfigDetails: TouchConfigDetails,
)
