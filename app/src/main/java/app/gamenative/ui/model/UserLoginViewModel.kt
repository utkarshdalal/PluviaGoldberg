package app.gamenative.ui.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.gamenative.PluviaApp
import app.gamenative.enums.LoginResult
import app.gamenative.enums.LoginScreen
import app.gamenative.events.AndroidEvent
import app.gamenative.events.SteamEvent
import app.gamenative.service.SteamService
import app.gamenative.ui.data.UserLoginState
import `in`.dragonbra.javasteam.steam.authentication.IAuthenticator
import java.util.concurrent.CompletableFuture
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class UserLoginViewModel : ViewModel() {
    private val _loginState = MutableStateFlow(UserLoginState())
    val loginState: StateFlow<UserLoginState> = _loginState.asStateFlow()

    private val _snackEvents = Channel<String>()
    val snackEvents = _snackEvents.receiveAsFlow()

    private val submitChannel = Channel<String>()

    private val authenticator = object : IAuthenticator {
        override fun acceptDeviceConfirmation(): CompletableFuture<Boolean> {
            Timber.i("Two-Factor, device confirmation")

            _loginState.update { currentState ->
                currentState.copy(
                    loginResult = LoginResult.DeviceConfirm,
                    loginScreen = LoginScreen.TWO_FACTOR,
                    isLoggingIn = false,
                )
            }

            return CompletableFuture.completedFuture(true)
        }

        override fun getDeviceCode(previousCodeWasIncorrect: Boolean): CompletableFuture<String> {
            Timber.d("Two-Factor, device code")

            _loginState.update { currentState ->
                currentState.copy(
                    loginResult = LoginResult.DeviceAuth,
                    loginScreen = LoginScreen.TWO_FACTOR,
                    isLoggingIn = false,
                    previousCodeIncorrect = previousCodeWasIncorrect,
                )
            }

            return CompletableFuture<String>().apply {
                viewModelScope.launch {
                    val code = submitChannel.receive()
                    complete(code)
                }
            }
        }

        override fun getEmailCode(
            email: String?,
            previousCodeWasIncorrect: Boolean,
        ): CompletableFuture<String> {
            Timber.d("Two-Factor, asking for email code")

            _loginState.update { currentState ->
                currentState.copy(
                    loginResult = LoginResult.EmailAuth,
                    loginScreen = LoginScreen.TWO_FACTOR,
                    isLoggingIn = false,
                    email = email,
                    previousCodeIncorrect = previousCodeWasIncorrect,
                )
            }

            return CompletableFuture<String>().apply {
                viewModelScope.launch {
                    val code = submitChannel.receive()
                    complete(code)
                }
            }
        }
    }

    private val onSteamConnected: (SteamEvent.Connected) -> Unit = {
        Timber.i("Received is connected")

        _loginState.update { currentState ->
            currentState.copy(
                isLoggingIn = it.isAutoLoggingIn,
                isSteamConnected = true,
            )
        }
    }

    private val onSteamDisconnected: (SteamEvent.Disconnected) -> Unit = {
        Timber.i("Received disconnected from Steam")
        _loginState.update { currentState ->
            currentState.copy(isSteamConnected = false)
        }
    }

    private val onRemoteDisconnected: (SteamEvent.RemotelyDisconnected) -> Unit = {
        Timber.i("Disconnected from steam remotely")
        _loginState.update { it.copy(isSteamConnected = false) }
    }

    private val onLogonStarted: (SteamEvent.LogonStarted) -> Unit = {
        _loginState.update { currentState ->
            currentState.copy(isLoggingIn = true)
        }
    }

    private val onLogonEnded: (SteamEvent.LogonEnded) -> Unit = {
        Timber.i("Received login result: ${it.loginResult}")
        _loginState.update { currentState ->
            currentState.copy(
                isLoggingIn = false,
                loginResult = it.loginResult,
            )
        }

        it.message?.let(::showSnack)

        // Why is this here? - Lossy Jan 17 2025
        // if (it.loginResult != LoginResult.Success) {
        //     SteamService.startLoginWithQr()
        // }
    }

    private val onBackPressed: (AndroidEvent.BackPressed) -> Unit = {
        if (!_loginState.value.isLoggingIn) {
            _loginState.update { currentState ->
                currentState.copy(loginResult = LoginResult.Failed)
            }
        }
    }

    private val onQrChallengeReceived: (SteamEvent.QrChallengeReceived) -> Unit = {
        _loginState.update { currentState ->
            currentState.copy(isQrFailed = false, qrCode = it.challengeUrl)
        }
    }

    private val onQrAuthEnded: (SteamEvent.QrAuthEnded) -> Unit = {
        _loginState.update { currentState ->
            currentState.copy(isQrFailed = !it.success, qrCode = null)
        }

        it.message?.let(::showSnack)
    }

    private val onLoggedOut: (SteamEvent.LoggedOut) -> Unit = {
        Timber.i("Received logged out")
        _loginState.update {
            it.copy(
                isSteamConnected = false,
                isLoggingIn = false,
                isQrFailed = false,
                loginResult = LoginResult.Failed,
                loginScreen = LoginScreen.CREDENTIAL,
            )
        }
    }

    init {
        Timber.d("init")

        PluviaApp.events.on<SteamEvent.Connected, Unit>(onSteamConnected)
        PluviaApp.events.on<SteamEvent.Disconnected, Unit>(onSteamDisconnected)
        PluviaApp.events.on<SteamEvent.LogonStarted, Unit>(onLogonStarted)
        PluviaApp.events.on<SteamEvent.LogonEnded, Unit>(onLogonEnded)
        PluviaApp.events.on<AndroidEvent.BackPressed, Unit>(onBackPressed)
        PluviaApp.events.on<SteamEvent.QrChallengeReceived, Unit>(onQrChallengeReceived)
        PluviaApp.events.on<SteamEvent.QrAuthEnded, Unit>(onQrAuthEnded)
        PluviaApp.events.on<SteamEvent.LoggedOut, Unit>(onLoggedOut)
        PluviaApp.events.on<SteamEvent.RemotelyDisconnected, Unit>(onRemoteDisconnected)

        val isLoggedIn = SteamService.isLoggedIn
        val isSteamConnected = SteamService.isConnected
        Timber.d("Logged in? $isLoggedIn")
        if (isLoggedIn) {
            _loginState.update {
                it.copy(isSteamConnected = isSteamConnected, isLoggingIn = true, isQrFailed = false, loginResult = LoginResult.Success)
            }
        } else {
            _loginState.update {
                it.copy(isSteamConnected = isSteamConnected, isLoggingIn = false, isQrFailed = false, loginResult = LoginResult.Failed)
            }
        }
    }

    override fun onCleared() {
        Timber.d("onCleared")

        PluviaApp.events.off<SteamEvent.Connected, Unit>(onSteamConnected)
        PluviaApp.events.off<SteamEvent.Disconnected, Unit>(onSteamDisconnected)
        PluviaApp.events.off<SteamEvent.LogonStarted, Unit>(onLogonStarted)
        PluviaApp.events.off<SteamEvent.LogonEnded, Unit>(onLogonEnded)
        PluviaApp.events.off<AndroidEvent.BackPressed, Unit>(onBackPressed)
        PluviaApp.events.off<SteamEvent.QrChallengeReceived, Unit>(onQrChallengeReceived)
        PluviaApp.events.off<SteamEvent.QrAuthEnded, Unit>(onQrAuthEnded)
        PluviaApp.events.off<SteamEvent.LoggedOut, Unit>(onLoggedOut)

        SteamService.stopLoginWithQr()
    }

    private fun showSnack(message: String) {
        viewModelScope.launch {
            _snackEvents.send(message)
        }
    }

    fun onCredentialLogin() {
        with(_loginState.value) {
            if (username.isEmpty() && password.isEmpty()) {
                return@with
            }

            viewModelScope.launch {
                app.gamenative.service.SteamService.startLoginWithCredentials(
                    username = username,
                    password = password,
                    rememberSession = rememberSession,
                    authenticator = authenticator,
                )
            }
        }
    }

    fun submit() {
        viewModelScope.launch {
            submitChannel.send(_loginState.value.twoFactorCode)

            _loginState.update { currentState ->
                currentState.copy(isLoggingIn = true)
            }
        }
    }

    fun onQrRetry() {
        viewModelScope.launch { SteamService.startLoginWithQr() }
    }

    fun onShowLoginScreen(loginScreen: LoginScreen) {
        when (loginScreen) {
            LoginScreen.CREDENTIAL -> SteamService.stopLoginWithQr()
            LoginScreen.QR -> viewModelScope.launch { SteamService.startLoginWithQr() }
            else -> Timber.w("onShowLoginScreen ended up in an unknown state: ${loginScreen.name}")
        }

        _loginState.update { currentState ->
            currentState.copy(loginScreen = loginScreen)
        }
    }

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

    fun setRememberSession(rememberPass: Boolean) {
        _loginState.update { currentState ->
            currentState.copy(rememberSession = rememberPass)
        }
    }

    fun setTwoFactorCode(twoFactorCode: String) {
        _loginState.update { currentState ->
            currentState.copy(twoFactorCode = twoFactorCode)
        }
    }

    fun retryConnection(context: Context) {
        // Reset error/login state if needed
        _loginState.update { currentState ->
            currentState.copy(
                isLoggingIn = false,
                loginResult = LoginResult.Failed,
                isSteamConnected = false,
                isQrFailed = false,
                qrCode = null
            )
        }
        // Restart the SteamService
        viewModelScope.launch {
            try {
                val intent = android.content.Intent(context, app.gamenative.service.SteamService::class.java)
                context.startForegroundService(intent)
            } catch (e: Exception) {
                Timber.e(e, "Failed to restart SteamService in retryConnection")
                showSnack("Failed to restart Steam connection: ${e.localizedMessage}")
            }
        }
    }
}
