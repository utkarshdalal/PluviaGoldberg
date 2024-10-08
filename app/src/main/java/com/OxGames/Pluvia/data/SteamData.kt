package com.OxGames.Pluvia.data

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

// I don't understand why the suppress lint is needed when
// https://kotlinlang.org/docs/serialization.html#serialize-and-deserialize-json
// tells you how to set it all up without warning you that a suppress lint will be needed
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class SteamData(
    var cellId: Int = 0,
    var accountName: String? = null,
    var accessToken: String? = null,
    var refreshToken: String? = null,
    var password: String? = null
)