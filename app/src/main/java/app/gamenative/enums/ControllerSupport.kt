package app.gamenative.enums

import timber.log.Timber

enum class ControllerSupport(val code: Int) {
    none(0),
    partial(1),
    full(2),
    ;

    companion object {
        fun from(keyValue: String?): ControllerSupport {
            return when (keyValue?.lowercase()) {
                none.name -> none
                partial.name -> partial
                full.name -> full
                else -> {
                    if (keyValue != null) {
                        Timber.e("Could not find proper ControllerSupport from $keyValue")
                    }
                    none
                }
            }
        }

        fun from(code: Int): ControllerSupport {
            ControllerSupport.entries.forEach { appType ->
                if (code == appType.code) {
                    return appType
                }
            }
            return none
        }
    }
}
