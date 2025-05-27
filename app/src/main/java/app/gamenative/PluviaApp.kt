package app.gamenative

import android.os.StrictMode
import androidx.navigation.NavController
import app.gamenative.events.EventDispatcher
import com.google.android.play.core.splitcompat.SplitCompatApplication
import com.winlator.inputcontrols.InputControlsManager
import com.winlator.widget.InputControlsView
import com.winlator.widget.TouchpadView
import com.winlator.widget.XServerView
import com.winlator.xenvironment.XEnvironment
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

typealias NavChangedListener = NavController.OnDestinationChangedListener

@HiltAndroidApp
class PluviaApp : SplitCompatApplication() {

    override fun onCreate() {
        super.onCreate()

        // Allows to find resource streams not closed within gamenative and JavaSteam
        if (BuildConfig.DEBUG) {
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build(),
            )

            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }

        // Init our custom crash handler.
        CrashHandler.initialize(this)

        // Init our datastore preferences.
        PrefManager.init(this)
    }

    companion object {
        internal val events: EventDispatcher = EventDispatcher()
        internal var onDestinationChangedListener: NavChangedListener? = null

        // TODO: find a way to make this saveable, this is terrible
        internal var xEnvironment: XEnvironment? = null
        internal var xServerView: XServerView? = null
        var inputControlsView: InputControlsView? = null
        var inputControlsManager: InputControlsManager? = null
        var touchpadView: TouchpadView? = null
    }
}
