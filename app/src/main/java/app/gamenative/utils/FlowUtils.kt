package app.gamenative.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> Flow<T>.timeChunked(
    maxBatchSize: Int,
    delayMillis: Long = 1000L,
): Flow<List<T>> = flow {
    coroutineScope {
        val buffer = ArrayList<T>(maxBatchSize)
        var ticker: ReceiveChannel<Unit>? = null

        val upstream = produce {
            collect { send(it) }
        }

        try {
            while (isActive) {
                when {
                    buffer.size >= maxBatchSize -> {
                        emit(ArrayList(buffer))
                        buffer.clear()
                        ticker?.cancel()
                        ticker = null
                    }
                    else -> select {
                        upstream.onReceiveCatching { result ->
                            result.getOrNull()?.let { element ->
                                buffer.add(element)
                                if (buffer.size == 1) { // Start timer on first element
                                    ticker = customTicker(delayMillis)
                                }
                            } ?: run {
                                // Handle upstream completion
                                if (buffer.isNotEmpty()) {
                                    emit(ArrayList(buffer))
                                }
                                return@onReceiveCatching
                            }
                        }

                        ticker?.onReceive {
                            if (buffer.isNotEmpty()) {
                                emit(ArrayList(buffer))
                                buffer.clear()
                                ticker?.cancel()
                                ticker = null
                            }
                        }
                    }
                }
            }
        } finally {
            ticker?.cancel()
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun CoroutineScope.customTicker(periodMillis: Long): ReceiveChannel<Unit> =
    produce(capacity = Channel.CONFLATED) {
        while (isActive) {
            delay(periodMillis)
            send(Unit)
        }
    }
