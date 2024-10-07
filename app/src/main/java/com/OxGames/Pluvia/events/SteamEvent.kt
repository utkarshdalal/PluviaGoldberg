package com.OxGames.Pluvia.events

sealed interface SteamEvent : Event {
    data object Connected : SteamEvent
    data object Disconnected : SteamEvent
    data class QrChallengeReceived(val challengeUrl: String) : SteamEvent
    data class QrAuthEnded(val success: Boolean) : SteamEvent
    data class LogonStarted(val username: String) : SteamEvent
    data class LogonEnded(val username: String, val success: Boolean) : SteamEvent
    data class LoggedOut(val username: String) : SteamEvent
}