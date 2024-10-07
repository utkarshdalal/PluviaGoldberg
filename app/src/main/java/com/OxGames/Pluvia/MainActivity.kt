package com.OxGames.Pluvia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.OxGames.Pluvia.components.LoggedInScreen
import com.OxGames.Pluvia.components.LoginScreen
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.ui.theme.PluviaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            var isLoggedIn by remember { mutableStateOf(false) }
            LaunchedEffect("") {
                SteamService.events.on<SteamEvent.LogonEnded> { isLoggedIn = it.success }
            }

            PluviaTheme {
                if (isLoggedIn)
                    LoggedInScreen()
                else
                    LoginScreen()
            }
        }
    }
}