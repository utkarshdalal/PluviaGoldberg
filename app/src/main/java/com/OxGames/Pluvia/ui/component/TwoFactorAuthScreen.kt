package com.OxGames.Pluvia.ui.component

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
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
fun TwoFactorAuthScreen(
    userLoginViewModel: UserLoginViewModel,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    // var authCode by remember { mutableStateOf("") }
    val userLoginState by userLoginViewModel.loginState.collectAsState()

    var isSteamConnected by remember { mutableStateOf(SteamService.isConnected) }
    var isLoggingIn by remember { mutableStateOf(SteamService.isLoggingIn) }
    // var loginResult by remember { mutableStateOf(LoginResult.Failed) }

    DisposableEffect(lifecycleOwner) {
        val onSteamConnected: (SteamEvent.Connected) -> Unit = {
            Log.d("TwoFactorAuthScreen", "Received is connected")
            isLoggingIn = it.isAutoLoggingIn
            isSteamConnected = true
        }
        val onSteamDisconnected: (SteamEvent.Disconnected) -> Unit = {
            Log.d("TwoFactorAuthScreen", "Received disconnected from Steam")
            isSteamConnected = false
        }
        val onLogonStarted: (SteamEvent.LogonStarted) -> Unit = {
            isLoggingIn = true
        }
        val onLogonEnded: (SteamEvent.LogonEnded) -> Unit = {
            Log.d("TwoFactorAuthScreen", "Received login result: ${it.loginResult}")
            userLoginViewModel.setLoginResult(it.loginResult)
            isLoggingIn = false
        }
        val onBackPressed: (AndroidEvent.BackPressed) -> Unit = {
            if (!isLoggingIn)
                userLoginViewModel.setLoginResult(LoginResult.Failed)
        }

        PluviaApp.events.on<SteamEvent.Connected, Unit>(onSteamConnected)
        PluviaApp.events.on<SteamEvent.Disconnected, Unit>(onSteamDisconnected)
        PluviaApp.events.on<SteamEvent.LogonStarted, Unit>(onLogonStarted)
        PluviaApp.events.on<SteamEvent.LogonEnded, Unit>(onLogonEnded)
        PluviaApp.events.on<AndroidEvent.BackPressed, Unit>(onBackPressed)

        onDispose {
            PluviaApp.events.off<SteamEvent.Connected, Unit>(onSteamConnected)
            PluviaApp.events.off<SteamEvent.Disconnected, Unit>(onSteamDisconnected)
            PluviaApp.events.off<SteamEvent.LogonStarted, Unit>(onLogonStarted)
            PluviaApp.events.off<SteamEvent.LogonEnded, Unit>(onLogonEnded)
            PluviaApp.events.off<AndroidEvent.BackPressed, Unit>(onBackPressed)
        }
    }

    TwoFactorAuthScreenContent(
        userLoginState = userLoginState,
        isSteamConnected = isSteamConnected,
        isLoggingIn = isLoggingIn,
        onSetTwoFactor = userLoginViewModel::setTwoFactorCode,
    )
}

@Composable
private fun TwoFactorAuthScreenContent(
    userLoginState: UserLoginState,
    isSteamConnected: Boolean,
    isLoggingIn: Boolean,
    onSetTwoFactor: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .width(256.dp)
            .height(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isSteamConnected && !isLoggingIn && userLoginState.loginResult != LoginResult.Success) {
            Text(
                when (userLoginState.loginResult) {
                    LoginResult.EmailAuth -> stringResource(R.string.email_2fa_msg)
                    LoginResult.TwoFactorCode -> stringResource(R.string.steam_auth_msg)
                    else -> stringResource(R.string.other_2fa_msg)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            TwoFactorTextField(
                twoFactorText = userLoginState.twoFactorCode,
                singleLine = true,
                onTwoFactorTextChange = onSetTwoFactor,
            )
//            OutlinedTextField(
//                value = userLoginState.twoFactorCode,
//                singleLine = true,
//                onValueChange = onSetTwoFactor,
//                label = { Text("Auth Code") }
//            )
            Spacer(modifier = Modifier.height(16.dp))
            ElevatedButton(
                onClick = {
                    if (userLoginState.twoFactorCode.isNotEmpty()) {
                        if (userLoginState.loginResult == LoginResult.EmailAuth) {
                            SteamService.logOn(
                                username = userLoginState.username,
                                password = userLoginState.password,
                                shouldRememberPassword = userLoginState.rememberPass,
                                emailAuth = userLoginState.twoFactorCode
                            )
                        } else {
                            SteamService.logOn(
                                username = userLoginState.username,
                                password = userLoginState.password,
                                shouldRememberPassword = userLoginState.rememberPass,
                                twoFactorAuth = userLoginState.twoFactorCode
                            )
                        }
                    }
                },
                content = { Text(text = "Login") }
            )
        } else
            LoadingScreen()
    }
}

// TODO: limit code to 6 in length.
// TODO: decorate some more, make sure paste works.
@Composable
private fun TwoFactorTextField(
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    twoFactorText: String,
    onTwoFactorTextChange: (String) -> Unit
) {
    BasicTextField(
        modifier = Modifier.fillMaxWidth(),
        value = twoFactorText,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        onValueChange = onTwoFactorTextChange,
        decorationBox = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(modifier),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(6) { idx ->
                    Text(
                        modifier = Modifier
                            .width(32.dp)
                            .border(
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
                                shape = RoundedCornerShape(4.dp)
                            ),
                        text = twoFactorText[idx].toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    )
}

@Preview
@Composable
private fun Preview_TwoFactorAuthScreen() {
    PluviaTheme(darkTheme = true) {
        Surface {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                TwoFactorAuthScreenContent(
                    userLoginState = UserLoginState(twoFactorCode = "A1B2C3"),
                    isSteamConnected = true,
                    isLoggingIn = false,
                    onSetTwoFactor = { },
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview_TwoFactorAuthScreen_NotConnected() {
    PluviaTheme(darkTheme = true) {
        Surface {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                TwoFactorAuthScreenContent(
                    userLoginState = UserLoginState(),
                    isSteamConnected = false,
                    isLoggingIn = false,
                    onSetTwoFactor = { },
                )
            }
        }
    }
}