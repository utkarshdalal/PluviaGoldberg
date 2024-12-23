package com.OxGames.Pluvia

import android.app.Application
import androidx.navigation.NavController
import com.OxGames.Pluvia.events.EventDispatcher
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PluviaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        PrefManager.init(this)
    }

    companion object {
        internal val events: EventDispatcher = EventDispatcher()
        internal var onDestinationChangedListener: NavController.OnDestinationChangedListener? = null
    }
}