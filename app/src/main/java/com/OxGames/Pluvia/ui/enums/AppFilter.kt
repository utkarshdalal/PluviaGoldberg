package com.OxGames.Pluvia.ui.enums

import com.OxGames.Pluvia.enums.AppType
import java.util.EnumSet

enum class AppFilter(val code: Int) {
    NONE(0),
    INSTALLED(0x01),
    ALPHABETIC(0x02),
    GAME(0x04),
    APPLICATION(0x08),
    TOOL(0x10),
    DEMO(0x20),
    ;

    companion object {
        fun getAppType(appFilter: EnumSet<AppFilter>): EnumSet<AppType> {
            val output: EnumSet<AppType> = EnumSet.noneOf(AppType::class.java)
            if (appFilter.contains(GAME)) {
                output.add(AppType.game)
            }
            if (appFilter.contains(APPLICATION)) {
                output.add(AppType.application)
            }
            if (appFilter.contains(TOOL)) {
                output.add(AppType.tool)
            }
            if (appFilter.contains(DEMO)) {
                output.add(AppType.demo)
            }
            return output
        }

        fun fromFlags(flags: Int): EnumSet<AppFilter> {
            val result = EnumSet.noneOf(AppFilter::class.java)
            AppFilter.entries.forEach { appFilter ->
                if (flags and appFilter.code == appFilter.code) {
                    result.add(appFilter)
                }
            }
            return result
        }

        fun toFlags(value: EnumSet<AppFilter>): Int {
            return value.map { it.code }.reduceOrNull { first, second -> first or second } ?: NONE.code
        }
    }
}
