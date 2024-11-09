package com.OxGames.Pluvia.ui.data

import com.OxGames.Pluvia.enums.LoginResult

data class UserLoginState(
    val username: String = "",
    val password: String = "",
    val rememberPass: Boolean = false,
    val twoFactorCode: String = "",
    val loginResult: LoginResult = LoginResult.Failed,
)