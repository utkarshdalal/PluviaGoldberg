package com.OxGames.Pluvia.ui.component

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.enums.LoginResult
import com.OxGames.Pluvia.events.AndroidEvent
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.ui.model.UserLoginViewModel

@Composable
fun UserLoginScreen(
    userLoginViewModel: UserLoginViewModel,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    var isSteamConnected by remember { mutableStateOf(SteamService.isConnected) }
    var isLoggingIn by remember { mutableStateOf(SteamService.isLoggingIn) }
    // var loginResult by remember { mutableStateOf(LoginResult.Failed) }

    DisposableEffect(lifecycleOwner) {
        val onSteamConnected: (SteamEvent.Connected) -> Unit = {
            Log.d("UserLoginScreen", "Received is connected")
            isLoggingIn = it.isAutoLoggingIn
            isSteamConnected = true
        }
        val onSteamDisconnected: (SteamEvent.Disconnected) -> Unit = {
            Log.d("UserLoginScreen", "Received disconnected from Steam")
            isSteamConnected = false
        }
        val onLogonStarted: (SteamEvent.LogonStarted) -> Unit = {
            isLoggingIn = true
        }
        val onLogonEnded: (SteamEvent.LogonEnded) -> Unit = {
            Log.d("UserLoginScreen", "Received login result: ${it.loginResult}")
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
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isSteamConnected && !isLoggingIn && userLoginViewModel.loginResult != LoginResult.TryAgain) {
           UsernamePassword(
               userLoginViewModel = userLoginViewModel,
               onLoginBtnClick = {
                   SteamService.logOn(
                       username = userLoginViewModel.username,
                       password = userLoginViewModel.password,
                       shouldRememberPassword = userLoginViewModel.rememberPass
                   )
               }
           )
        } else
            LoadingScreen()
    }
}

@Composable
fun UsernamePassword(userLoginViewModel: UserLoginViewModel, onLoginBtnClick: () -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .width(256.dp)
            .height(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        TextField(
            value = userLoginViewModel.username,
            singleLine = true,
            onValueChange = { userLoginViewModel.username = it },
            label = { Text("Username") }
        )
        TextField(
            value = userLoginViewModel.password,
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            onValueChange = { userLoginViewModel.password = it },
            label = { Text("Password") },
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = {passwordVisible = !passwordVisible}) {
                    Icon(imageVector = image, description)
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = userLoginViewModel.rememberPass,
                    onCheckedChange = { userLoginViewModel.rememberPass = it },
                )
                Text("Remember me")
            }
            ElevatedButton(onClick = onLoginBtnClick) { Text("Login") }
        }
    }
}