package com.OxGames.Pluvia

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.OrientationEventListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.OxGames.Pluvia.events.AndroidEvent
import com.OxGames.Pluvia.ui.component.PluviaMain
import com.OxGames.Pluvia.ui.enums.Orientation
import com.skydoves.landscapist.coil.LocalCoilImageLoader
import com.winlator.core.AppUtils
import dagger.hilt.android.AndroidEntryPoint
import okio.Path.Companion.toOkioPath
import java.util.EnumSet
import kotlin.math.abs
import kotlin.math.min

@AndroidEntryPoint
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
        private var availableOrientations: EnumSet<Orientation> =
            EnumSet.of(Orientation.UNSPECIFIED)
    }

    private var index = totalIndex++

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // startOrientator() // causes memory leak since activity restarted every orientation change
        PluviaApp.events.on<AndroidEvent.SetSystemUIVisibility, Unit>(onSetSystemUi)
        PluviaApp.events.on<AndroidEvent.StartOrientator, Unit>(onStartOrientator)
        PluviaApp.events.on<AndroidEvent.SetAllowedOrientation, Unit>(onSetAllowedOrientation)

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val imageLoader = remember {
                ImageLoader.Builder(context)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .memoryCache {
                        MemoryCache.Builder(context)
                            .maxSizePercent(0.1)
                            .strongReferencesEnabled(true)
                            .build()
                    }
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .diskCache {
                        DiskCache.Builder()
                            .maxSizePercent(0.03)
                            .directory(context.cacheDir.resolve("image_cache").toOkioPath())
                            .build()
                    }
                    .logger(if (BuildConfig.DEBUG) DebugLogger() else null)
                    .build()
            }

            CompositionLocalProvider(LocalCoilImageLoader provides imageLoader) {
                PluviaMain()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("MainActivity$index", "onDestroy")
        PluviaApp.events.emit(AndroidEvent.ActivityDestroyed)
        PluviaApp.events.off<AndroidEvent.SetSystemUIVisibility, Unit>(onSetSystemUi)
        PluviaApp.events.off<AndroidEvent.StartOrientator, Unit>(onStartOrientator)
        PluviaApp.events.off<AndroidEvent.SetAllowedOrientation, Unit>(onSetAllowedOrientation)
    }

    // override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    //     // Log.d("MainActivity$index", "onKeyDown($keyCode):\n$event")
    //     if (keyCode == KeyEvent.KEYCODE_BACK) {
    //         PluviaApp.events.emit(AndroidEvent.BackPressed)
    //         return true
    //     }
    //     return super.onKeyDown(keyCode, event)
    // }


    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Log.d("MainActivity$index", "dispatchKeyEvent(${event.keyCode}):\n$event")
        var eventDispatched =
            PluviaApp.events.emit(AndroidEvent.KeyEvent(event)) { it.any { it } } == true
        // TODO: Temp'd removed this.
        //  Idealy, compose handles back presses automaticially in which we can override it in certain composables.
        //  Since LibraryScreen uses its own navigation system, this will need to be re-worked accordingly.
//        if (!eventDispatched) {
//            if (event.keyCode == KeyEvent.KEYCODE_BACK) {
//                PluviaApp.events.emit(AndroidEvent.BackPressed)
//                eventDispatched = true
//            }
//        }
        return if (!eventDispatched) super.dispatchKeyEvent(event) else true
    }

    override fun dispatchGenericMotionEvent(ev: MotionEvent?): Boolean {
        // Log.d("MainActivity$index", "dispatchGenericMotionEvent(${ev?.deviceId}:${ev?.device?.name}):\n$ev")
        val eventDispatched =
            PluviaApp.events.emit(AndroidEvent.MotionEvent(ev)) { it.any { it } } == true
        return if (!eventDispatched) super.dispatchGenericMotionEvent(ev) else true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Log.d("MainActivity", "Requested orientation: $requestedOrientation => ${Orientation.fromActivityInfoValue(requestedOrientation)}")
    }

    private fun startOrientator() {
        // Log.d("MainActivity$index", "Orientator starting up")
        val orientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                currentOrientationChangeValue =
                    if (orientation != ORIENTATION_UNKNOWN) orientation else currentOrientationChangeValue
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
        val orientations = conformTo.ifEmpty { EnumSet.of(Orientation.UNSPECIFIED) }
        var inRange =
            orientations.filter { it.angleRanges.any { it.contains(adjustedOrientation) } }
                .toTypedArray()
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
        val currentOrientationDist =
            distances.firstOrNull { it.first.activityInfoValue == requestedOrientation }?.second
                ?: Int.MAX_VALUE
        if (requestedOrientation != nearest.first.activityInfoValue && currentOrientationDist > nearest.second) {
            Log.d(
                "MainActivity",
                "$adjustedOrientation => currentOrientation(${
                    Orientation.fromActivityInfoValue(requestedOrientation)
                }) != nearestOrientation(${nearest.first}) && currentDistance($currentOrientationDist) > nearestDistance(${nearest.second})"
            )
            requestedOrientation = nearest.first.activityInfoValue
        }
    }
}