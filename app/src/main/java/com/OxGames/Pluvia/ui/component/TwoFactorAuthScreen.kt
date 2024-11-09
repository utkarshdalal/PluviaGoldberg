package com.OxGames.Pluvia.ui.component

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.enums.LoginResult
import com.OxGames.Pluvia.events.AndroidEvent
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.ui.model.UserLoginViewModel
import com.OxGames.Pluvia.R

@Composable
fun TwoFactorAuthScreen(
    userLoginViewModel: UserLoginViewModel,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    var authCode by remember { mutableStateOf("") }

    var isSteamConnected by remember { mutableStateOf(SteamService.isConnected) }
    var isLoggingIn by remember { mutableStateOf(SteamService.isLoggingIn) }
    // var loginResult by remember { mutableStateOf(LoginResult.Failed) }

    DisposableEffect(lifecycleOwner) {
        val onSteamConnected: (SteamEvent.Connected) -> Unit = {
            Log.d("TwoFactorAuthScreen", "Received is connected")
            isLoggingIn = it.isAutoLoggingIn
            isSteamConnected = true
        }
        val onSteamDisconnected: (SteamEvent.Disconnected) -> Unit = {
            Log.d("TwoFactorAuthScreen", "Received disconnected from Steam")
            isSteamConnected = false
        }
        val onLogonStarted: (SteamEvent.LogonStarted) -> Unit = {
            isLoggingIn = true
        }
        val onLogonEnded: (SteamEvent.LogonEnded) -> Unit = {
            Log.d("TwoFactorAuthScreen", "Received login result: ${it.loginResult}")
            userLoginViewModel.loginResult = it.loginResult
            isLoggingIn = false
        }
        val onBackPressed: (AndroidEvent.BackPressed) -> Unit = {
            if (!isLoggingIn)
                userLoginViewModel.loginResult = LoginResult.Failed
        }

        PluviaApp.events.on<SteamEvent.Connected>(onSteamConnected)
        PluviaApp.events.on<SteamEvent.Disconnected>(onSteamDisconnected)
        PluviaApp.events.on<SteamEvent.LogonStarted>(onLogonStarted)
        PluviaApp.events.on<SteamEvent.LogonEnded>(onLogonEnded)
        PluviaApp.events.on<AndroidEvent.BackPressed>(onBackPressed)

        onDispose {
            PluviaApp.events.off<SteamEvent.Connected>(onSteamConnected)
            PluviaApp.events.off<SteamEvent.Disconnected>(onSteamDisconnected)
            PluviaApp.events.off<SteamEvent.LogonStarted>(onLogonStarted)
            PluviaApp.events.off<SteamEvent.LogonEnded>(onLogonEnded)
            PluviaApp.events.off<AndroidEvent.BackPressed>(onBackPressed)
        }
    }

    Column(
        modifier = Modifier
            .width(256.dp)
            .height(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isSteamConnected && !isLoggingIn && userLoginViewModel.loginResult != LoginResult.TryAgain) {
            Text(
                when (userLoginViewModel.loginResult) {
                    LoginResult.EmailAuth -> stringResource(R.string.email_2fa_msg)
                    LoginResult.TwoFactorCode -> stringResource(R.string.steam_auth_msg)
                    else -> stringResource(R.string.other_2fa_msg)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = authCode,
                singleLine = true,
                onValueChange = { authCode = it },
                label = { Text("Auth Code") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            ElevatedButton(onClick = {
                if (userLoginViewModel.loginResult == LoginResult.EmailAuth) {
                    SteamService.logOn(
                        username = userLoginViewModel.username,
                        password = userLoginViewModel.password,
                        shouldRememberPassword = userLoginViewModel.rememberPass,
                        emailAuth = userLoginViewModel.twoFactorCode
                    )
                } else {
                    SteamService.logOn(
                        username = userLoginViewModel.username,
                        password = userLoginViewModel.password,
                        shouldRememberPassword = userLoginViewModel.rememberPass,
                        twoFactorAuth = userLoginViewModel.twoFactorCode
                    )
                }
            }) { Text("Login") }
        } else
            LoadingScreen()
    }
}