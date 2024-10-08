package com.OxGames.Pluvia.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.ui.theme.PluviaTheme

@Composable
fun PluviaMain() {
    var isLoggedIn by remember { mutableStateOf(false) }
    LaunchedEffect("") {
        SteamService.events.on<SteamEvent.LogonEnded> { isLoggedIn = it.success }
    }

    PluviaTheme {
        if (isLoggedIn)
            LoggedInScreen()
        else
            LoginScreen()
    }
}