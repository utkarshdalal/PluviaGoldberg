package com.OxGames.Pluvia

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.OxGames.Pluvia.components.LoginScreen
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.ui.theme.PluviaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            SteamService.events.once<SteamEvent.LoggedIn> { Log.d("MainActivity", "Logged in as ${it.username}") }

            PluviaTheme {
                LoginScreen()
            }
        }
    }
}