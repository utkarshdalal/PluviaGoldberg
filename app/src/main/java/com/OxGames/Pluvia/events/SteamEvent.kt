package com.OxGames.Pluvia.events

import com.OxGames.Pluvia.enums.LoginResult
import `in`.dragonbra.javasteam.types.SteamID

sealed interface SteamEvent : Event {
    data class Connected(val isAutoLoggingIn: Boolean) : SteamEvent
    data object Disconnected : SteamEvent
    data class QrChallengeReceived(val challengeUrl: String) : SteamEvent
    data class QrAuthEnded(val success: Boolean) : SteamEvent
    data class LogonStarted(val username: String?) : SteamEvent
    data class LogonEnded(val username: String?, val loginResult: LoginResult) : SteamEvent
    data class LoggedOut(val username: String?) : SteamEvent
    data class PersonaStateReceived(val steamId: SteamID) : SteamEvent
    data object AppInfoReceived : SteamEvent
}