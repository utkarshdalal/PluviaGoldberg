package com.utkarshdalal.PluviaGoldberg.events

import com.utkarshdalal.PluviaGoldberg.data.SteamFriend
import com.utkarshdalal.PluviaGoldberg.enums.LoginResult
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.ProfileInfoCallback

sealed interface SteamEvent<T> : Event<T> {
    data class Connected(val isAutoLoggingIn: Boolean) : SteamEvent<Unit>
    data class LoggedOut(val username: String?) : SteamEvent<Unit>
    data class LogonEnded(val username: String?, val loginResult: LoginResult, val message: String? = null) : SteamEvent<Unit>
    data class LogonStarted(val username: String?) : SteamEvent<Unit>
    data class PersonaStateReceived(val persona: SteamFriend) : SteamEvent<Unit>
    data class QrAuthEnded(val success: Boolean, val message: String? = null) : SteamEvent<Unit>
    data class QrChallengeReceived(val challengeUrl: String) : SteamEvent<Unit>

    // data object AppInfoReceived : SteamEvent<Unit>
    data object ForceCloseApp : SteamEvent<Unit>
    data object Disconnected : SteamEvent<Unit>
    data object RemotelyDisconnected : SteamEvent<Unit>

    // This isn't a SteamEvent, but since its the only one now, it can stay
    data class OnProfileInfo(val info: ProfileInfoCallback) : SteamEvent<Unit>
    data class OnAliasHistory(val names: List<String>) : SteamEvent<Unit>
}
