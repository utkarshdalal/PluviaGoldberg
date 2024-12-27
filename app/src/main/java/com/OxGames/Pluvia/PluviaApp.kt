package com.OxGames.Pluvia

import android.app.Application
import androidx.navigation.NavController
import com.OxGames.Pluvia.events.EventDispatcher
import com.winlator.xenvironment.XEnvironment
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

        // TODO: find a way to make this saveable, this is terrible
        internal var xEnvironment: XEnvironment? = null
    }
}
