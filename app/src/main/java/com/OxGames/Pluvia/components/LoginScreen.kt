package com.OxGames.Pluvia.components

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.events.SteamEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    val context = LocalContext.current
    var isSteamConnected by remember { mutableStateOf(SteamService.isRunning) }
    var isUsernameLogin by remember { mutableStateOf(false) }
    var isLoggingIn by remember { mutableStateOf(false) }

    SteamService.events.once<SteamEvent.Connected> { isSteamConnected = true }
    LaunchedEffect("") {
        val onLoggingIn: (SteamEvent.LogonStarted) -> Unit = { isLoggingIn = true }
        var onEndLoggingIn: ((SteamEvent.LogonEnded) -> Unit)? = null
        onEndLoggingIn = {
            isLoggingIn = false
            if (it.success) {
                SteamService.events.off<SteamEvent.LogonStarted>(onLoggingIn)
                SteamService.events.off<SteamEvent.LogonEnded>(onEndLoggingIn!!)
            }
        }
        SteamService.events.on<SteamEvent.LogonStarted>(onLoggingIn)
        SteamService.events.on<SteamEvent.LogonEnded>(onEndLoggingIn)

        if (!isSteamConnected) {
            val intent = Intent(context, SteamService::class.java)
            context.startService(intent)
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Pluvia") },
                actions = {
                    IconButton(onClick = {
                        if (!isUsernameLogin)
                            SteamService.stopLoginWithQr()
                        isUsernameLogin = !isUsernameLogin
                    }) { Icon(imageVector = if (isUsernameLogin) Icons.Filled.QrCode2 else Icons.Filled.Password, "Toggle login mode") }
                }
            )
        }
    ) { innerPadding ->
        if (isSteamConnected && !isLoggingIn) {
            if (isUsernameLogin)
                UserLoginScreen(
                    innerPadding,
                    onLoginBtnClick = { username: String, password: String ->
                        Log.d("LoginScreen", "Username: $username\nPassword: $password")
                    })
            else
                QrLoginScreen(innerPadding)
        } else
            LoadingScreen(innerPadding)
    }
}