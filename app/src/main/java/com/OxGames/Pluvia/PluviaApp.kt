package com.OxGames.Pluvia

import android.app.Application
import com.OxGames.Pluvia.events.EventDispatcher

class PluviaApp : Application() {
    companion object {
        val events: EventDispatcher = EventDispatcher()
    }
}