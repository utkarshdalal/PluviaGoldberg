package com.OxGames.Pluvia.ui.data

import com.OxGames.Pluvia.enums.LoginResult

data class UserLoginState(
    val username: String = "",
    val password: String = "",
    val rememberPass: Boolean = false,
    val twoFactorCode: String = "",
    val showQrCode: Boolean = false,
    val qrCode: String? = null,
    val isQrFailed: Boolean = false,
    val loginResult: LoginResult = LoginResult.Failed,
)