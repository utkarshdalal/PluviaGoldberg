package com.OxGames.Pluvia

import android.app.Application
import androidx.navigation.NavController
import com.OxGames.Pluvia.events.EventDispatcher
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PluviaApp : Application() {
    companion object {
        internal val events: EventDispatcher = EventDispatcher()
        internal var onDestinationChangedListener: NavController.OnDestinationChangedListener? = null
    }
}