package com.OxGames.Pluvia

import android.app.Application
import androidx.navigation.NavController
import com.OxGames.Pluvia.events.EventDispatcher
import dagger.hilt.android.HiltAndroidApp

typealias NavChangedListener = NavController.OnDestinationChangedListener

@HiltAndroidApp
class PluviaApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Init our datastore preferences.
        PrefManager.init(this)
    }

    companion object {
        internal val events: EventDispatcher = EventDispatcher()
        internal var onDestinationChangedListener: NavChangedListener? = null
    }
}
