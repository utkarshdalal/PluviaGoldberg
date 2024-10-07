package com.OxGames.Pluvia.events

sealed interface SteamEvent : Event {
    data object Connected : SteamEvent
    data object Disconnected : SteamEvent
    data class QrChallengeReceived(val challengeUrl: String) : SteamEvent
    data class QrAuthEnded(val success: Boolean) : SteamEvent
    data class LoggedIn(val username: String) : SteamEvent
    data class LoggedOut(val username: String) : SteamEvent
}