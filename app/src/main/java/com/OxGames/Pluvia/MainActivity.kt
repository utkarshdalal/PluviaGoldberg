package com.OxGames.Pluvia

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.OxGames.Pluvia.components.LoadingScreen
import com.OxGames.Pluvia.components.LoginScreen
import com.OxGames.Pluvia.components.QrCodeImage
import com.OxGames.Pluvia.components.QrLoginScreen
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.ui.theme.PluviaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            var isSteamConnected by remember { mutableStateOf(SteamService.isRunning) }
            SteamService.events.once<SteamEvent.Connected> {
                isSteamConnected = true
            }
            startSteamService()

            PluviaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (isSteamConnected)
                        LoginScreen(innerPadding = innerPadding)
                    else
                        LoadingScreen(innerPadding)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun startSteamService() {
        val intent = Intent(this, SteamService::class.java)
        startService(intent)
    }
}