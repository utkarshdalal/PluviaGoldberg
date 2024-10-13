package com.OxGames.Pluvia.components

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
import androidx.compose.runtime.MutableState
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

@Composable
fun UserLoginScreen(
    innerPadding: PaddingValues,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    var isLoggingIn by remember { mutableStateOf(SteamService.isLoggingIn) }
    var loginResult by remember { mutableStateOf(LoginResult.Failed) }
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val rememberMe = remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val onLogonStarted: (SteamEvent.LogonStarted) -> Unit = { isLoggingIn = true }
        val onLogonEnded: (SteamEvent.LogonEnded) -> Unit = {
            Log.d("UserLoginScreen", "Received login result: ${it.loginResult}")
            loginResult = it.loginResult
            isLoggingIn = false
        }
        val onBackPressed: (AndroidEvent.BackPressed) -> Unit = {
            if (!isLoggingIn)
                loginResult = LoginResult.Failed
        }
        PluviaApp.events.on<SteamEvent.LogonStarted>(onLogonStarted)
        PluviaApp.events.on<SteamEvent.LogonEnded>(onLogonEnded)
        PluviaApp.events.on<AndroidEvent.BackPressed>(onBackPressed)

        onDispose {
            PluviaApp.events.off<SteamEvent.LogonStarted>(onLogonStarted)
            PluviaApp.events.off<SteamEvent.LogonEnded>(onLogonEnded)
            PluviaApp.events.off<AndroidEvent.BackPressed>(onBackPressed)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isLoggingIn && loginResult != LoginResult.TryAgain) {
            when (loginResult) {
                LoginResult.TwoFactorCode -> TwoFactorAuth(
                    authMsg = "Please enter the two factor authentication code found in the Steam Authenticator app",
                    onLoginBtnClick = {
                        SteamService.logOn(
                            username = username.value,
                            password = password.value,
                            shouldRememberPassword = rememberMe.value,
                            twoFactorAuth = it
                        )
                    }
                )
                LoginResult.EmailAuth -> TwoFactorAuth(
                    authMsg = "Please enter the two factor authentication code sent to your email",
                    onLoginBtnClick = {
                        SteamService.logOn(
                            username = username.value,
                            password = password.value,
                            shouldRememberPassword = rememberMe.value,
                            emailAuth = it
                        )
                    }
                )
                else -> UsernamePassword(
                    username = username,
                    password = password,
                    rememberMe = rememberMe,
                    onLoginBtnClick = {
                        SteamService.logOn(
                            username = username.value,
                            password = password.value,
                            shouldRememberPassword = rememberMe.value
                        )
                    }
                )
            }
        } else
            LoadingScreen(innerPadding)
    }
}

@Composable
fun UsernamePassword(username: MutableState<String>, password: MutableState<String>, rememberMe: MutableState<Boolean>, onLoginBtnClick: () -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .width(256.dp)
            .height(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        TextField(
            value = username.value,
            singleLine = true,
            onValueChange = { username.value = it },
            label = { Text("Username") }
        )
        TextField(
            value = password.value,
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            onValueChange = { password.value = it },
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
                    checked = rememberMe.value,
                    onCheckedChange = { rememberMe.value = it },
                )
                Text("Remember me")
            }
            ElevatedButton(onClick = onLoginBtnClick) { Text("Login") }
        }
    }
}

@Composable
fun TwoFactorAuth(authMsg: String, onLoginBtnClick: (String) -> Unit) {
    var authCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .width(256.dp)
            .height(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(authMsg)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = authCode,
            singleLine = true,
            onValueChange = { authCode = it },
            label = { Text("Auth Code") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        ElevatedButton(onClick = { onLoginBtnClick(authCode) }) { Text("Login") }
    }
}