package com.OxGames.Pluvia.events

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

// written by Claude 3.5
sealed interface Event

class EventDispatcher {
    val listeners = mutableMapOf<KClass<out Event>, MutableList<EventListener<Event>>>()

    class EventListener<E : Event>(
        val listener: (E) -> Unit,
        val once: Boolean = false
    )

    inline fun <reified E : Event> on(noinline listener: (E) -> Unit) {
        addListener(listener, false)
    }

    inline fun <reified E : Event> once(noinline listener: (E) -> Unit) {
        addListener(listener, true)
    }

    inline fun <reified E : Event> addListener(
        noinline listener: (E) -> Unit,
        once: Boolean
    ) {
        val eventClass = E::class
        val typedListener = EventListener<Event>({ event ->
            if (event is E) {
                listener(event)
            }
        }, once)
        listeners.getOrPut(eventClass) { mutableListOf() }.add(typedListener)
    }

    inline fun <reified E : Event> off(noinline listener: (E) -> Unit) {
        val eventClass = E::class
        listeners[eventClass]?.removeIf { it.toString() == listener.toString() }
    }

    inline fun <reified E : Event> clearAllListenersOf() {
        val currentKeys = listeners.keys.toList()
        for (key in currentKeys)
            if (key is E)
                listeners.remove(key)
    }
    fun clearAllListeners() {
        listeners.clear()
    }

    inline fun <reified E : Event> emit(event: E) {
        CoroutineScope(Dispatchers.Main).launch {
            val eventClass = E::class
            listeners[eventClass]?.let { eventListeners ->
                // Create a new list for iteration to avoid concurrent modification
                eventListeners.toList().forEach { eventListener ->
                    eventListener.listener(event)
                }
                // Remove one-time listeners after execution
                eventListeners.removeIf { it.once }
            }
        }
    }
}