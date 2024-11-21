package com.OxGames.Pluvia.events

import com.OxGames.Pluvia.enums.LoginResult
import `in`.dragonbra.javasteam.types.SteamID

sealed interface SteamEvent<T> : Event<T> {
    data class Connected(val isAutoLoggingIn: Boolean) : SteamEvent<Unit>
    data object Disconnected : SteamEvent<Unit>
    data class QrChallengeReceived(val challengeUrl: String) : SteamEvent<Unit>
    data class QrAuthEnded(val success: Boolean) : SteamEvent<Unit>
    data class LogonStarted(val username: String?) : SteamEvent<Unit>
    data class LogonEnded(val username: String?, val loginResult: LoginResult) : SteamEvent<Unit>
    data class LoggedOut(val username: String?) : SteamEvent<Unit>
    data class PersonaStateReceived(val steamId: SteamID) : SteamEvent<Unit>
    data object AppInfoReceived : SteamEvent<Unit>
}