package com.OxGames.Pluvia.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job

data class DownloadInfo(
    private var downloadJob: Job? = null,
    private var progress: Float = 0f
) {
    private val downloadProgressListeners = mutableListOf<((Float) -> Unit)>()

    fun cancel() {
        downloadJob?.cancel(CancellationException("Cancelled by user"))
    }
    fun setDownloadJob(job: Job) {
        downloadJob = job
    }
    fun getProgress(): Float {
        return progress
    }
    fun setProgress(amount: Float) {
        progress = amount
        emitProgressChange()
    }
    fun addProgressListener(listener: (Float) -> Unit) {
        downloadProgressListeners.add(listener)
    }
    fun removeProgressListener(listener: (Float) -> Unit) {
        downloadProgressListeners.remove(listener)
    }
    fun emitProgressChange() {
        for (listener in downloadProgressListeners) {
            listener(progress)
        }
    }
}