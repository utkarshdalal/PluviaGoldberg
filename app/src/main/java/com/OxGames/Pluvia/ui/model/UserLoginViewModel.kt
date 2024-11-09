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
    
    var username: String
        get() = _loginState.value.username
        set(value) = _loginState.update { currentState ->
            currentState.copy(username = value)
        }
    var password: String
        get() = _loginState.value.password
        set(value) = _loginState.update { currentState ->
            currentState.copy(password = value)
        }
    var rememberPass: Boolean
        get() = _loginState.value.rememberPass
        set(value) = _loginState.update { currentState ->
            currentState.copy(rememberPass = value)
        }
    var twoFactorCode: String
        get() = _loginState.value.twoFactorCode
        set(value) = _loginState.update { currentState ->
            currentState.copy(twoFactorCode = value)
        }
    var loginResult: LoginResult
        get() = _loginState.value.loginResult
        set(value) = _loginState.update { currentState ->
            currentState.copy(loginResult = value)
        }
}