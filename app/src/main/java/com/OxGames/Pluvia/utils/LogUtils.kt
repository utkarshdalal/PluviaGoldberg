@file:Suppress("unused")

package com.OxGames.Pluvia.utils

import android.util.Log

/**
 * Utility methods to use instead of [Log]
 */

fun Any.logD(message: String, throwable: Throwable? = null, tag: String? = null) {
    Log.d(tag ?: this::class.java.simpleName, message, throwable)
}

fun Any.logI(message: String, throwable: Throwable? = null, tag: String? = null) {
    Log.i(tag ?: this::class.java.simpleName, message, throwable)
}

fun Any.logW(message: String, throwable: Throwable? = null, tag: String? = null) {
    Log.w(tag ?: this::class.java.simpleName, message, throwable)
}

fun Any.logE(message: String, throwable: Throwable? = null, tag: String? = null) {
    if (throwable != null) {
        Log.e(tag ?: this::class.java.simpleName, "$message: ${throwable.message}", throwable)
    } else {
        Log.e(tag ?: this::class.java.simpleName, message)
    }
}
