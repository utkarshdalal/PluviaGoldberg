package com.OxGames.Pluvia.ui.data

import com.OxGames.Pluvia.enums.LoginResult
import com.OxGames.Pluvia.enums.LoginScreen

data class UserLoginState(
    val username: String = "",
    val password: String = "",
    val rememberPass: Boolean = false,
    val twoFactorCode: String = "",

    val isSteamConnected: Boolean = false,
    val isLoggingIn: Boolean = false,

    val loginResult: LoginResult = LoginResult.Failed,
    val loginScreen: LoginScreen = LoginScreen.CREDENTIAL,

    val attemptCount: Int = -1,
    val previousCodeIncorrect: Boolean = false,

    val email: String? = null,

    val qrCode: String? = null,
    val isQrFailed: Boolean = false,
)
