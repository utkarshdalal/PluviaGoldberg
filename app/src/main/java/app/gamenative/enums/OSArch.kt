package app.gamenative.enums

import timber.log.Timber

enum class OSArch(val keyValName: String) {
    Arch32(keyValName = "32"),
    Arch64(keyValName = "64"),
    Unknown(keyValName = "unknown"),
    ;

    companion object {
        fun from(keyValue: String?): OSArch {
            return when (keyValue) {
                Arch32.keyValName -> Arch32
                Arch64.keyValName -> Arch64
                else -> {
                    if (keyValue != null) {
                        Timber.w("Could not identify $keyValue as OSArch")
                    }
                    Unknown
                }
            }
        }
    }
}
