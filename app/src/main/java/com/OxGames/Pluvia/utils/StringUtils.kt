package com.OxGames.Pluvia.utils

import com.OxGames.Pluvia.Constants

/**
 * Extension functions relating to [String] as the receiver type.
 */

fun String.getAvatarURL(): String =
    this.ifEmpty { null }
        ?.takeIf { str -> str.isNotEmpty() && !str.all { it == '0' } }
        ?.let { "${Constants.Persona.AVATAR_BASE_URL}${it.substring(0, 2)}/${it}_full.jpg" }
        ?: Constants.Persona.MISSING_AVATAR_URL

// This doesn't belong here, but i'm tired.
fun Long.getProfileUrl(): String = "${Constants.Persona.PROFILE_URL}$this/"
