package com.OxGames.Pluvia.ui.component

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.events.SteamEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current) {
    val context = LocalContext.current
    // var isUsernameLogin by remember { mutableStateOf(false) }
    var isSteamConnected by remember { mutableStateOf(SteamService.isConnected) }
    var isLoggingIn by remember { mutableStateOf(SteamService.isLoggingIn) }

    DisposableEffect(lifecycleOwner) {
        val onSteamConnected: (SteamEvent.Connected) -> Unit = {
            Log.d("LoginScreen", "Received is connected")
            isSteamConnected = !it.isAutoLoggingIn
        }
        // val onSteamDisconnected: (SteamEvent.Disconnected) -> Unit = {
        //     Log.d("LoginScreen", "Received is disconnected")
        //     isSteamConnected = false
        // }
        val onLoggingIn: (SteamEvent.LogonStarted) -> Unit = {
            Log.d("LoginScreen", "Received logon started")
            isLoggingIn = true
        }
        val onEndLoggingIn: (SteamEvent.LogonEnded) -> Unit = {
            Log.d("LoginScreen", "Received logon ended")
            isLoggingIn = false
        }
        PluviaApp.events.on<SteamEvent.Connected>(onSteamConnected)
        PluviaApp.events.on<SteamEvent.LogonStarted>(onLoggingIn)
        PluviaApp.events.on<SteamEvent.LogonEnded>(onEndLoggingIn)

        if (!isSteamConnected) {
            val intent = Intent(context, SteamService::class.java)
            context.startService(intent)
        }

        onDispose {
            PluviaApp.events.off<SteamEvent.Connected>(onSteamConnected)
            PluviaApp.events.off<SteamEvent.LogonStarted>(onLoggingIn)
            PluviaApp.events.off<SteamEvent.LogonEnded>(onEndLoggingIn)
        }
    }

    // Scaffold(modifier = Modifier.fillMaxSize(),
    //     topBar = {
    //         TopAppBar(
    //             title = { Text("Pluvia") },
    //             // actions = {
    //             //     if (isSteamConnected && !isLoggingIn)
    //             //         IconButton(onClick = {
    //             //             if (!isUsernameLogin)
    //             //                 SteamService.stopLoginWithQr()
    //             //             isUsernameLogin = !isUsernameLogin
    //             //         }) { Icon(imageVector = if (isUsernameLogin) Icons.Filled.QrCode2 else Icons.Filled.Password, "Toggle login mode") }
    //             // }
    //         )
    //     }
    // ) { innerPadding ->
    //     if (isSteamConnected) {
    //         // if (isUsernameLogin)
    //         //     UserLoginScreen(innerPadding)
    //         // else
    //             QrLoginScreen(innerPadding)
    //     } else
    //         LoadingScreen()
    // }
}