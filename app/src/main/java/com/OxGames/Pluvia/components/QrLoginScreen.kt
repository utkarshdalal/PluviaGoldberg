package com.OxGames.Pluvia.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.enums.LoginResult
import com.OxGames.Pluvia.events.SteamEvent

@Composable
fun QrLoginScreen(
    innerPadding: PaddingValues,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    var url: String? by remember { mutableStateOf(null) }
    var isFailed: Boolean by remember { mutableStateOf(false) }
    var attemptNum: Int by remember { mutableIntStateOf(0) }
    var isLoggingIn by remember { mutableStateOf(SteamService.isLoggingIn) }

    DisposableEffect(lifecycleOwner, key2 = attemptNum) {
        Log.d("QrLoginScreen", "Starting QR authentication")
        isFailed = false
        isLoggingIn = SteamService.isLoggingIn

        // TODO: revisit this to make sure all looks correct
        val onLogonStarted: (SteamEvent.LogonStarted) -> Unit = { isLoggingIn = true }
        val onLogonEnded: (SteamEvent.LogonEnded) -> Unit = {
            isLoggingIn = false
            if (it.loginResult != LoginResult.Success)
                SteamService.startLoginWithQr()
        }
        val onQrChallengeReceived: (SteamEvent.QrChallengeReceived) -> Unit = { url = it.challengeUrl }
        val onQrAuthEnded: (SteamEvent.QrAuthEnded) -> Unit = {
            isFailed = !it.success
            url = null
        }
        PluviaApp.events.on<SteamEvent.LogonStarted>(onLogonStarted)
        PluviaApp.events.on<SteamEvent.LogonEnded>(onLogonEnded)
        PluviaApp.events.on<SteamEvent.QrChallengeReceived>(onQrChallengeReceived)
        PluviaApp.events.on<SteamEvent.QrAuthEnded>(onQrAuthEnded)
        if (!isLoggingIn)
            SteamService.startLoginWithQr()

        onDispose {
            PluviaApp.events.off<SteamEvent.LogonStarted>(onLogonStarted)
            PluviaApp.events.off<SteamEvent.LogonEnded>(onLogonEnded)
            PluviaApp.events.off<SteamEvent.QrChallengeReceived>(onQrChallengeReceived)
            PluviaApp.events.off<SteamEvent.QrAuthEnded>(onQrAuthEnded)
            SteamService.stopLoginWithQr()
        }
    }

    if ((url != null || isFailed) && !isLoggingIn) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isFailed)
                ElevatedButton(onClick = { attemptNum++ }) { Text("Retry") }
            else
                QrCodeImage(content = url!!, size = 256.dp)
        }
    } else
        LoadingScreen(innerPadding)
}