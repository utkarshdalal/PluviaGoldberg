package com.OxGames.Pluvia.utils

class SteamUtils {
    companion object {
        /**
         * Strips non-ASCII characters from String
         */
        fun removeSpecialChars(s: String): String {
            return s.replace(Regex("[^\\u0000-\\u007F]"), "")
        }
    }
}