package com.OxGames.Pluvia.enums

import timber.log.Timber
import java.util.EnumSet

enum class OS {
    windows,
    macos,
    linux,
    none,
    ;

    companion object {
        fun from(keyValue: String?): EnumSet<OS> {
            val osses = EnumSet.noneOf(OS::class.java)
            osses.addAll(
                (keyValue ?: "none").split(',').filter { it.isNotEmpty() }.map {
                    try {
                        OS.valueOf(it.trim())
                    } catch (_: Exception) {
                        Timber.e("Could not identify OS $it")
                        none
                    }
                },
            )
            return osses
        }
    }
}
