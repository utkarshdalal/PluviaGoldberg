package com.OxGames.Pluvia.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job

data class DownloadInfo(
    val jobCount: Int = 1,
) {
    private var downloadJob: Job? = null
    private val downloadProgressListeners = mutableListOf<((Float) -> Unit)>()
    private val progresses: Array<Float> = Array(jobCount) { 0f }

    fun cancel() {
        downloadJob?.cancel(CancellationException("Cancelled by user"))
    }
    fun setDownloadJob(job: Job) {
        downloadJob = job
    }
    fun getProgress(): Float {
        return progresses.sum() / jobCount
    }
    fun setProgress(amount: Float, jobIndex: Int = 0) {
        progresses[jobIndex] = amount
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
            listener(getProgress())
        }
    }
}
