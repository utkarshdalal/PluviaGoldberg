package com.OxGames.Pluvia

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.OxGames.Pluvia.ui.component.PluviaMain
import com.OxGames.Pluvia.events.AndroidEvent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent { PluviaMain() }
    }

    // override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    //     return super.dispatchKeyEvent(event)
    // }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("MainActivity", "onKeyDown($keyCode):\n$event")
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            PluviaApp.events.emit(AndroidEvent.BackPressed)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}