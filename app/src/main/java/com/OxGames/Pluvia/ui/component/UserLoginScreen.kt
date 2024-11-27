package com.OxGames.Pluvia.ui.component

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.OxGames.Pluvia.R
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.enums.LoginResult
import com.OxGames.Pluvia.ui.data.UserLoginState
import com.OxGames.Pluvia.ui.model.UserLoginViewModel
import com.OxGames.Pluvia.ui.theme.PluviaTheme

@Composable
fun UserLoginScreen(
    userLoginViewModel: UserLoginViewModel,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val userLoginState by userLoginViewModel.loginState.collectAsState()

    UserLoginScreenContent(
        isSteamConnected = userLoginState.isSteamConnected,
        isLoggingIn = userLoginState.isLoggingIn,
        userLoginState = userLoginState,
        onUsername = userLoginViewModel::setUsername,
        onPassword = userLoginViewModel::setPassword,
        onShowQrCode = userLoginViewModel::setShowQrCode,
        onRememberPassword = userLoginViewModel::setRememberPass,
        onRetry = userLoginViewModel::onRetry
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