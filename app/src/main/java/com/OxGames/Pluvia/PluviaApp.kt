package com.OxGames.Pluvia

import androidx.navigation.NavController
import com.OxGames.Pluvia.events.EventDispatcher
import com.google.android.play.core.splitcompat.SplitCompatApplication
import com.winlator.xenvironment.XEnvironment
import dagger.hilt.android.HiltAndroidApp

typealias NavChangedListener = NavController.OnDestinationChangedListener

@HiltAndroidApp
class PluviaApp : SplitCompatApplication() {

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
