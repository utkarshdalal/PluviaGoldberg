package com.OxGames.Pluvia.data

data class ConfigInfo(
    val installDir: String,
    val launch: Array<LaunchInfo>,
    val steamControllerTemplateIndex: Int,
    val steamControllerTouchTemplateIndex: Int,
    // val steamControllerTouchConfigDetails: TouchConfigDetails,
)
