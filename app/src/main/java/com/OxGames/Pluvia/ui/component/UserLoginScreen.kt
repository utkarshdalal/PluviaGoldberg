package com.OxGames.Pluvia.ui.component

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.R
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.enums.LoginResult
import com.OxGames.Pluvia.events.AndroidEvent
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.ui.data.UserLoginState
import com.OxGames.Pluvia.ui.model.UserLoginViewModel
import com.OxGames.Pluvia.ui.theme.PluviaTheme

@Composable
fun UserLoginScreen(
    userLoginViewModel: UserLoginViewModel,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    var isSteamConnected by remember { mutableStateOf(SteamService.isConnected) }
    var isLoggingIn by remember { mutableStateOf(SteamService.isLoggingIn) }
    var attemptNum: Int by remember { mutableIntStateOf(0) }

    // var loginResult by remember { mutableStateOf(LoginResult.Failed) }
    val userLoginState by userLoginViewModel.loginState.collectAsState()

    DisposableEffect(lifecycleOwner, key2 = attemptNum) {
        userLoginViewModel.setQrFailedState(false)
        isLoggingIn = SteamService.isLoggingIn

        val onSteamConnected: (SteamEvent.Connected) -> Unit = {
            Log.d("UserLoginScreen", "Received is connected")
            isLoggingIn = it.isAutoLoggingIn
            isSteamConnected = true
        }
        val onSteamDisconnected: (SteamEvent.Disconnected) -> Unit = {
            Log.d("UserLoginScreen", "Received disconnected from Steam")
            isSteamConnected = false
        }
        val onLogonStarted: (SteamEvent.LogonStarted) -> Unit = {
            isLoggingIn = true
        }
        val onLogonEnded: (SteamEvent.LogonEnded) -> Unit = {
            Log.d("UserLoginScreen", "Received login result: ${it.loginResult}")
            isLoggingIn = false
            userLoginViewModel.setLoginResult(it.loginResult)
            if (it.loginResult != LoginResult.Success) {
                SteamService.startLoginWithQr()
            }
        }
        val onBackPressed: (AndroidEvent.BackPressed) -> Unit = {
            if (!isLoggingIn) {
                userLoginViewModel.setLoginResult(LoginResult.Failed)
            }
        }
        val onQrChallengeReceived: (SteamEvent.QrChallengeReceived) -> Unit = {
            userLoginViewModel.setQrCode(it.challengeUrl)
        }
        val onQrAuthEnded: (SteamEvent.QrAuthEnded) -> Unit = {
            userLoginViewModel.setQrFailedState(!it.success)
            userLoginViewModel.setQrCode(null)
        }

        PluviaApp.events.on<SteamEvent.Connected, Unit>(onSteamConnected)
        PluviaApp.events.on<SteamEvent.Disconnected, Unit>(onSteamDisconnected)
        PluviaApp.events.on<SteamEvent.LogonStarted, Unit>(onLogonStarted)
        PluviaApp.events.on<SteamEvent.LogonEnded, Unit>(onLogonEnded)
        PluviaApp.events.on<AndroidEvent.BackPressed, Unit>(onBackPressed)
        PluviaApp.events.on<SteamEvent.QrChallengeReceived, Unit>(onQrChallengeReceived)
        PluviaApp.events.on<SteamEvent.QrAuthEnded, Unit>(onQrAuthEnded)

        onDispose {
            PluviaApp.events.off<SteamEvent.Connected, Unit>(onSteamConnected)
            PluviaApp.events.off<SteamEvent.Disconnected, Unit>(onSteamDisconnected)
            PluviaApp.events.off<SteamEvent.LogonStarted, Unit>(onLogonStarted)
            PluviaApp.events.off<SteamEvent.LogonEnded, Unit>(onLogonEnded)
            PluviaApp.events.off<AndroidEvent.BackPressed, Unit>(onBackPressed)
            PluviaApp.events.off<SteamEvent.QrChallengeReceived, Unit>(onQrChallengeReceived)
            PluviaApp.events.off<SteamEvent.QrAuthEnded, Unit>(onQrAuthEnded)
            SteamService.stopLoginWithQr()
        }
    }

    UserLoginScreenContent(
        isSteamConnected = isSteamConnected,
        isLoggingIn = isLoggingIn,
        userLoginState = userLoginState,
        onUsername = userLoginViewModel::setUsername,
        onPassword = userLoginViewModel::setPassword,
        onShowQrCode = { showQR ->
            if (showQR) {
                SteamService.startLoginWithQr()
            } else {
                SteamService.stopLoginWithQr()
            }
            userLoginViewModel.setShowQrCode(showQR)
        },
        onRememberPassword = userLoginViewModel::setRememberPass,
        onRetry = { attemptNum++ }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserLoginScreenContent(
    isSteamConnected: Boolean,
    isLoggingIn: Boolean,
    userLoginState: UserLoginState,
    onUsername: (String) -> Unit,
    onPassword: (String) -> Unit,
    onShowQrCode: (Boolean) -> Unit,
    onRememberPassword: (Boolean) -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_name))
                }
            )
        },
        floatingActionButton = {
            // Scaffold seems not to calculate 'end' padding when using 3-Button Nav Bar in landscape.
            val systemBarPadding = WindowInsets.systemBars
                .only(WindowInsetsSides.End)
                .asPaddingValues()

            ExtendedFloatingActionButton(
                modifier = Modifier.padding(
                    end = systemBarPadding.calculateEndPadding(LayoutDirection.Ltr)
                ),
                onClick = { onShowQrCode(!userLoginState.showQrCode) },
                text = { Text(text = if (userLoginState.showQrCode) "Credential Sign In" else "QR Sign In") },
                icon = {
                    Icon(
                        imageVector = if (userLoginState.showQrCode) Icons.Filled.Keyboard else Icons.Filled.QrCode2,
                        contentDescription = null
                    )
                },
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isSteamConnected && !isLoggingIn && userLoginState.loginResult != LoginResult.Success) {
                Crossfade(
                    modifier = Modifier.fillMaxSize(),
                    targetState = userLoginState.showQrCode
                ) { showQR ->
                    if (showQR) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (userLoginState.isQrFailed) {
                                ElevatedButton(onClick = onRetry) { Text("Retry") }
                            } else if (userLoginState.qrCode.isNullOrEmpty()) {
                                CircularProgressIndicator()
                            } else {
                                QrCodeImage(content = userLoginState.qrCode, size = 256.dp)
                            }
                        }
                    } else {
                        UsernamePassword(
                            username = userLoginState.username,
                            onUsername = onUsername,
                            password = userLoginState.password,
                            onPassword = onPassword,
                            rememberPassword = userLoginState.rememberPass,
                            onRememberPassword = onRememberPassword,
                            onLoginBtnClick = {
                                if (userLoginState.username.isNotEmpty() && userLoginState.password.isNotEmpty()) {
                                    SteamService.logOn(
                                        username = userLoginState.username,
                                        password = userLoginState.password,
                                        shouldRememberPassword = userLoginState.rememberPass
                                    )
                                }
                            }
                        )
                    }
                }
            } else {
                LoadingScreen()
            }
        }
    }
}

@Composable
private fun UsernamePassword(
    username: String,
    onUsername: (String) -> Unit,
    password: String,
    onPassword: (String) -> Unit,
    rememberPassword: Boolean,
    onRememberPassword: (Boolean) -> Unit,
    onLoginBtnClick: () -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        OutlinedTextField(
            value = username,
            singleLine = true,
            onValueChange = onUsername,
            label = { Text("Username") }
        )
        OutlinedTextField(
            value = password,
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            onValueChange = onPassword,
            label = { Text("Password") },
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, description)
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberPassword,
                    onCheckedChange = onRememberPassword,
                )
                Text(text = "Remember me")
            }
            Spacer(modifier = Modifier.width(32.dp))
            ElevatedButton(onClick = onLoginBtnClick) { Text(text = "Login") }
        }
    }
}

@Preview
@Composable
private fun Preview_UserLoginScreen() {
    var state by remember { mutableStateOf(UserLoginState(qrCode = "Hello World")) }
    PluviaTheme(darkTheme = true) {
        Surface {
            UserLoginScreenContent(
                isSteamConnected = true,
                isLoggingIn = false,
                userLoginState = state,
                onUsername = { },
                onPassword = { },
                onShowQrCode = {
                    state = state.copy(showQrCode = it)
                },
                onRememberPassword = { },
                onRetry = { },
            )
        }
    }
}

@Preview
@Composable
private fun Preview_UserLoginScreen_ShowQR() {
    PluviaTheme(darkTheme = true) {
        Surface {
            UserLoginScreenContent(
                isSteamConnected = true,
                isLoggingIn = false,
                userLoginState = UserLoginState(showQrCode = true, qrCode = "Hello World"),
                onUsername = { },
                onPassword = { },
                onShowQrCode = { },
                onRememberPassword = { },
                onRetry = { },
            )
        }
    }
}

@Preview
@Composable
private fun Preview_UserLoginScreen_ShowQR_Failed() {
    PluviaTheme(darkTheme = true) {
        Surface {
            UserLoginScreenContent(
                isSteamConnected = true,
                isLoggingIn = false,
                userLoginState = UserLoginState(showQrCode = true, isQrFailed = true),
                onUsername = { },
                onPassword = { },
                onShowQrCode = { },
                onRememberPassword = { },
                onRetry = { },
            )
        }
    }
}

@Preview
@Composable
private fun Preview_UserLoginScreen_ShowQR_Loading() {
    PluviaTheme(darkTheme = true) {
        Surface {
            UserLoginScreenContent(
                isSteamConnected = false,
                isLoggingIn = false,
                userLoginState = UserLoginState(),
                onUsername = { },
                onPassword = { },
                onShowQrCode = { },
                onRememberPassword = { },
                onRetry = { },
            )
        }
    }
}