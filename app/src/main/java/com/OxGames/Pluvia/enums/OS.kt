package com.OxGames.Pluvia.enums

import java.util.EnumSet
import timber.log.Timber

enum class OS {
    windows,
    macos,
    linux,
    none,
    ;

    companion object {
        fun from(keyValue: String?): EnumSet<OS> {
            val osses = keyValue?.takeUnless { it.isEmpty() }
                ?.split(',')
                ?.map {
                    try {
                        OS.valueOf(it.trim())
                    } catch (_: Exception) {
                        Timber.w("Could not identify OS $it")
                        none
                    }
                }
                ?.toCollection(EnumSet.noneOf(OS::class.java))

            return osses ?: EnumSet.of(none)
        }
    }
}
