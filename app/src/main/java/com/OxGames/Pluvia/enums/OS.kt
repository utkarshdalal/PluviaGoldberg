package com.OxGames.Pluvia.enums

import android.util.Log
import java.util.EnumSet

enum class OS {
    windows,
    macos,
    linux,
    none;

    companion object {
        fun from(keyValue: String?): EnumSet<OS> {
            val osses = EnumSet.noneOf(OS::class.java)
            osses.addAll((keyValue ?: "none").split(',').filter { it.isNotEmpty() }.map {
                try {
                    OS.valueOf(it.trim())
                } catch (_: Exception) {
                    Log.w("OS", "Could not identify OS $it")
                    none
                }
            })
            return osses
        }
    }
}