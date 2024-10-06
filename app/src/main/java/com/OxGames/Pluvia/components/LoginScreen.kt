package com.OxGames.Pluvia.components

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun LoginScreen(innerPadding: PaddingValues) {
    val isUsernameLogin = remember { mutableStateOf(false) }

    if (isUsernameLogin.value)
        UserLoginScreen(innerPadding, onLoginBtnClick = { username: String, password: String ->
            Log.d("LoginScreen", "Username: $username\nPassword: $password")
        })
    else
        QrLoginScreen(innerPadding)
}