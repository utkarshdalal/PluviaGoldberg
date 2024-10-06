package com.OxGames.Pluvia.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.events.SteamEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun QrLoginScreen(innerPadding: PaddingValues) {
    var url: String? by remember { mutableStateOf(null) }
    SteamService.events.once<SteamEvent.LoggedIn> { event -> Log.d("QrLoginScreen", "Logged in as ${event.username}") }
    SteamService.events.on<SteamEvent.QrChallengeReceived> { event -> url = event.challengeUrl }

    LaunchedEffect("") {
        SteamService.loginWithQr()
    }

    if (url != null) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            QrCodeImage(content = url!!, size = 256.dp)
        }
    } else
        LoadingScreen(innerPadding)
}