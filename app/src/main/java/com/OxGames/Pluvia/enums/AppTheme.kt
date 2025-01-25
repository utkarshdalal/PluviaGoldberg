package com.OxGames.Pluvia.enums

enum class AppTheme(val code: Int) {
    AUTO(0),
    DAY(1),
    NIGHT(2),
    ;

    companion object {
        fun from(value: Int): AppTheme = entries.firstOrNull { it.code == value } ?: NIGHT
    }
}
