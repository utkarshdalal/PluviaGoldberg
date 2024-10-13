package com.OxGames.Pluvia.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.enums.LoginResult
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.ui.theme.PluviaTheme

@Composable
fun PluviaMain(lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current) {
    var isLoggedIn by remember { mutableStateOf(false) }
    DisposableEffect(lifecycleOwner) {
        val onLogonEnded: (SteamEvent.LogonEnded) -> Unit = { isLoggedIn = it.loginResult == LoginResult.Success }
        val onLoggedOut: (SteamEvent.LoggedOut) -> Unit = { isLoggedIn = false }
        PluviaApp.events.on<SteamEvent.LogonEnded>(onLogonEnded)
        PluviaApp.events.on<SteamEvent.LoggedOut>(onLoggedOut)

        onDispose {
            PluviaApp.events.off<SteamEvent.LogonEnded>(onLogonEnded)
            PluviaApp.events.off<SteamEvent.LoggedOut>(onLoggedOut)
        }
    }

    PluviaTheme {
        if (isLoggedIn)
            LoggedInScreen()
        else
            LoginScreen()
    }
}