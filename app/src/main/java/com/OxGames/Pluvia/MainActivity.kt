package com.OxGames.Pluvia

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.OrientationEventListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.OxGames.Pluvia.ui.component.PluviaMain
import com.OxGames.Pluvia.events.AndroidEvent
import com.OxGames.Pluvia.ui.enums.Orientation
import com.winlator.core.AppUtils
import java.util.EnumSet
import kotlin.math.abs
import kotlin.math.min

class MainActivity : ComponentActivity() {
    private val onSetSystemUi: (AndroidEvent.SetSystemUIVisibility) -> Unit = {
        AppUtils.hideSystemUI(this, !it.visible)
    }
    private val onSetAllowedOrientation: (AndroidEvent.SetAllowedOrientation) -> Unit = {
        // Log.d("MainActivity", "Requested allowed orientations of $it")
        availableOrientations = it.orientations
        setOrientationTo(currentOrientationChangeValue, availableOrientations)
    }
    private val onStartOrientator: (AndroidEvent.StartOrientator) -> Unit = {
        startOrientator()
    }


    companion object {
        private var totalIndex = 0

        private var currentOrientationChangeValue: Int = 0
        private var availableOrientations: EnumSet<Orientation> = EnumSet.of(Orientation.UNSPECIFIED)
    }
    private var index = totalIndex++

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // startOrientator() // causes memory leak since activity restarted every orientation change
        PluviaApp.events.on<AndroidEvent.SetSystemUIVisibility>(onSetSystemUi)
        PluviaApp.events.on<AndroidEvent.StartOrientator>(onStartOrientator)
        PluviaApp.events.on<AndroidEvent.SetAllowedOrientation>(onSetAllowedOrientation)

        enableEdgeToEdge()
        setContent { PluviaMain() }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Log.d("MainActivity$index", "onDestroy")
        PluviaApp.events.off<AndroidEvent.SetSystemUIVisibility>(onSetSystemUi)
        PluviaApp.events.off<AndroidEvent.StartOrientator>(onStartOrientator)
        PluviaApp.events.off<AndroidEvent.SetAllowedOrientation>(onSetAllowedOrientation)
    }

    // override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    //     return super.dispatchKeyEvent(event)
    // }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("MainActivity$index", "onKeyDown($keyCode):\n$event")
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            PluviaApp.events.emit(AndroidEvent.BackPressed)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Log.d("MainActivity", "Requested orientation: $requestedOrientation => ${Orientation.fromActivityInfoValue(requestedOrientation)}")
    }

    private fun startOrientator() {
        // Log.d("MainActivity$index", "Orientator starting up")
        val orientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                currentOrientationChangeValue = orientation
                setOrientationTo(currentOrientationChangeValue, availableOrientations)
            }
        }
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable()
        }
    }
    private fun setOrientationTo(orientation: Int, conformTo: EnumSet<Orientation>) {
        // Log.d("MainActivity$index", "Setting orientation to conform")
        // reverse direction of orientation
        val adjustedOrientation = 360 - orientation
        // if our available orientations are empty then assume unspecified
        val orientations = if (conformTo.isNotEmpty()) conformTo else EnumSet.of(Orientation.UNSPECIFIED)
        var inRange = orientations.filter { it.angleRanges.any { it.contains(adjustedOrientation) } }.toTypedArray()
        if (inRange.isEmpty()) {
            // none of the available orientations conform to the reported orientation
            // so set it to the original orientations in preparation for finding the
            // nearest conforming orientation
            inRange = orientations.toTypedArray()
        }
        // find the nearest orientation to the reported
        val distances = orientations.map {
            Pair(it, it.angleRanges.map {
                it.map {
                    // since 0 can be represented as 360 and vice versa
                    if (adjustedOrientation == 0 || adjustedOrientation == 360)
                        min(abs(it - 0), abs(it - 360))
                    else
                        abs(it - adjustedOrientation)
                }.min()
            }.min())
        }
        val nearest = distances.sortedBy { it.second }.first()
        // set the requested orientation to the nearest if it is not already as long as it is nearer than what is currently set
        if (requestedOrientation != nearest.first.activityInfoValue && distances.firstOrNull { it.first.activityInfoValue == requestedOrientation }?.second != nearest.second) {
            requestedOrientation = nearest.first.activityInfoValue
        }
    }
}