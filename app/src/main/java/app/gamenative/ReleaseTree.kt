package app.gamenative

import android.util.Log
import timber.log.Timber

/**
 * A log manager instance for release mode.
 * Debug mode uses [timber.log.Timber.DebugTree]
 */
class ReleaseTree : Timber.Tree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean = priority >= Log.INFO // Ignore Verbose and Debug logs.

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (!isLoggable(tag, priority)) {
            return
        }

        val priorityChar = when (priority) {
            Log.INFO -> 'I'
            Log.WARN -> 'W'
            Log.ERROR -> 'E'
            Log.ASSERT -> "A"
            else -> 'V' // Treat anything else as Verbose
        }

        Log.println(priority, tag, "$priorityChar: $message")

        t?.let {
            Log.println(priority, tag, Log.getStackTraceString(it))
        }
    }
}
