package com.OxGames.Pluvia

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?,
) : Thread.UncaughtExceptionHandler {

    companion object {
        private const val LOG_CAT_COUNT = 150
        private const val CRASH_FILE_HISTORY_COUNT = 1

        fun initialize(context: Context) {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            val crashHandler = CrashHandler(context.applicationContext, defaultHandler)
            Thread.setDefaultUncaughtExceptionHandler(crashHandler)
        }
    }

    private val crashFileDir by lazy {
        File(context.getExternalFilesDir(null), "crash_logs").apply {
            if (!exists()) mkdirs()
        }
    }

    private val recentLogcat: String
        get() = try {
            val process = Runtime.getRuntime().exec("logcat -d -t $LOG_CAT_COUNT --pid=${android.os.Process.myPid()}")
            process.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "Failed to retrieve logcat: ${e.message}"
        }

    private val cleanupOldCrashFiles: () -> Unit = {
        crashFileDir.listFiles()?.let { files ->
            if (files.size > CRASH_FILE_HISTORY_COUNT) {
                files.sortByDescending { it.lastModified() }
                files.drop(CRASH_FILE_HISTORY_COUNT).forEach { it.delete() }
            }
        }
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        PrefManager.recentlyCrashed = true

        saveCrashToFile(throwable)
        defaultHandler?.uncaughtException(thread, throwable)
    }

    private fun saveCrashToFile(throwable: Throwable) {
        try {
            val stackTrace = StringWriter().apply {
                val pw = PrintWriter(this)
                throwable.printStackTrace(pw)
            }.toString()

            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())

            val crashReport = buildString {
                appendLine("Timestamp: $timestamp")
                appendLine("Exception: ${throwable.javaClass.name}")
                appendLine("Message: ${throwable.message}")
                appendLine()
                appendLine("Stack Trace:")
                appendLine(stackTrace)
                appendLine()
                appendLine("Device Information:")
                appendLine("Model: ${android.os.Build.MODEL}")
                appendLine("Android Version: ${android.os.Build.VERSION.RELEASE}")
                appendLine("App Version: ${context.packageManager.getPackageInfo(context.packageName, 0).versionName}")
                appendLine()
                appendLine("Logcat:")
                appendLine("----------------------------------------")
                appendLine(recentLogcat)
            }

            File(crashFileDir, "pluvia_crash_$timestamp.txt").writeText(crashReport)

            cleanupOldCrashFiles()
        } catch (e: Exception) {
            defaultHandler?.uncaughtException(Thread.currentThread(), throwable)
        }
    }
}
