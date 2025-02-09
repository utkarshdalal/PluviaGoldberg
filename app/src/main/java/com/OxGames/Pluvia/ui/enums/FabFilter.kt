package com.OxGames.Pluvia.ui.enums

import com.OxGames.Pluvia.enums.AppType
import java.util.EnumSet

enum class FabFilter(val code: Int) {
    INSTALLED(0x01),
    ALPHABETIC(0x02),
    GAME(0x04),
    APPLICATION(0x08),
    TOOL(0x10),
    DEMO(0x20),
    ;

    companion object {
        fun getAppType(fabFilter: EnumSet<FabFilter>): EnumSet<AppType> {
            val output: EnumSet<AppType> = EnumSet.noneOf(AppType::class.java)
            if (fabFilter.contains(GAME)) {
                output.add(AppType.game)
            }
            if (fabFilter.contains(APPLICATION)) {
                output.add(AppType.application)
            }
            if (fabFilter.contains(TOOL)) {
                output.add(AppType.tool)
            }
            if (fabFilter.contains(DEMO)) {
                output.add(AppType.demo)
            }
            return output
        }
    }
}
