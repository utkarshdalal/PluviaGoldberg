package com.OxGames.Pluvia.ui.model

import androidx.lifecycle.ViewModel
import com.OxGames.Pluvia.enums.LoginResult
import com.OxGames.Pluvia.ui.data.UserLoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UserLoginViewModel : ViewModel() {
    private val _loginState = MutableStateFlow(UserLoginState())
    val loginState: StateFlow<UserLoginState> = _loginState.asStateFlow()

    fun setUsername(username: String) {
        _loginState.update { currentState ->
            currentState.copy(username = username)
        }
    }
    fun setPassword(password: String) {
        _loginState.update { currentState ->
            currentState.copy(password = password)
        }
    }
    fun setRememberPass(rememberPass: Boolean) {
        _loginState.update { currentState ->
            currentState.copy(rememberPass = rememberPass)
        }
    }
    fun setTwoFactorCode(twoFactorCode: String) {
        _loginState.update { currentState ->
            currentState.copy(twoFactorCode = twoFactorCode)
        }
    }
    fun setLoginResult(loginResult: LoginResult) {
        _loginState.update { currentState ->
            currentState.copy(loginResult = loginResult)
        }
    }
}