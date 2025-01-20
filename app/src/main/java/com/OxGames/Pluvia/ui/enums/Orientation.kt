package com.OxGames.Pluvia.ui.enums

import android.content.pm.ActivityInfo
import java.util.EnumSet

enum class Orientation(val activityInfoValue: Int, val angleRanges: Array<IntRange>) {
    PORTRAIT(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, arrayOf(330..360, 0..30)), // 0° ± 30°
    LANDSCAPE(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, arrayOf(60..120)), // 90° ± 30°
    REVERSE_PORTRAIT(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT, arrayOf(150..210)), // 180° ± 30°
    REVERSE_LANDSCAPE(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, arrayOf(240..300)), // 270° ± 30°
    UNSPECIFIED(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, arrayOf(0..360)),
    ;

    companion object {
        fun fromActivityInfoValue(value: Int): Orientation =
            Orientation.entries.firstOrNull { it.activityInfoValue == value } ?: UNSPECIFIED

        fun toInt(flags: EnumSet<Orientation>): Int =
            flags.fold(0) { acc, flag -> acc or (1 shl flag.ordinal) }

        fun fromInt(code: Int): EnumSet<Orientation> =
            EnumSet.copyOf(entries.filter { (code and (1 shl it.ordinal)) != 0 })
    }
}
