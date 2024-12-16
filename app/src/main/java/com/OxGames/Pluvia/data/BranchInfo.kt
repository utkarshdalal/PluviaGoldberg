package com.OxGames.Pluvia.data

import java.util.Date

data class BranchInfo(
    val name: String,
    val buildId: Long,
    val pwdRequired: Boolean,
    val timeUpdated: Date,
)