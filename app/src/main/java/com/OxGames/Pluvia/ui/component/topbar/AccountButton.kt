package com.OxGames.Pluvia.ui.component.topbar

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.SteamService
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.ui.util.CoilAsyncImage

@Composable
fun AccountButton(
    contentDescription: String? = null,
    onClick: () -> Unit,
) {
    var profilePicUrl by remember {
        mutableStateOf(
            SteamService.getUserSteamId()?.let {
                SteamService.getPersonaStateOf(it)
            }?.avatarUrl ?: SteamService.MISSING_AVATAR_URL
        )
    }

    DisposableEffect(true) {
        val onPersonaStateReceived: (SteamEvent.PersonaStateReceived) -> Unit = {
            val persona = SteamService.getPersonaStateOf(it.steamId)
            // Log.d("PluviaMain", "Testing persona of ${persona?.name}:${persona?.friendID?.accountID} to ${SteamService.getUserSteamId()?.accountID}")
            if (persona != null && persona.friendID.accountID == SteamService.getUserSteamId()?.accountID) {
                Log.d("PluviaMain", "Setting avatar url to ${persona.avatarUrl}")
                profilePicUrl = persona.avatarUrl
            }
        }

        PluviaApp.events.on<SteamEvent.PersonaStateReceived, Unit>(onPersonaStateReceived)

        onDispose {
            PluviaApp.events.off<SteamEvent.PersonaStateReceived, Unit>(onPersonaStateReceived)
        }
    }
    IconButton(
        onClick = onClick,
        content = {
            CoilAsyncImage(url = profilePicUrl)
        }
    )
}