package com.OxGames.Pluvia.ui.component.topbar

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
import com.OxGames.Pluvia.ui.util.ListItemImage

@Composable
fun AccountButton(
    contentDescription: String? = null,
    onClick: () -> Unit,
) {
    var avatarHash by remember {
        var hash = SteamService.MISSING_AVATAR_URL
        SteamService.getUserSteamId()?.let { id ->
            SteamService.getPersonaStateOf(id)?.let { persona ->
                hash = SteamService.getAvatarURL(persona.avatarHash)
            }
        }
        mutableStateOf(hash)
    }

    DisposableEffect(true) {
        val onPersonaStateReceived: (SteamEvent.PersonaStateReceived) -> Unit = {
            avatarHash = it.persona?.let { persona ->
                SteamService.getAvatarURL(persona.avatarHash)
            } ?: SteamService.MISSING_AVATAR_URL
        }

        PluviaApp.events.on<SteamEvent.PersonaStateReceived, Unit>(onPersonaStateReceived)

        onDispose {
            PluviaApp.events.off<SteamEvent.PersonaStateReceived, Unit>(onPersonaStateReceived)
        }
    }

    IconButton(
        onClick = onClick,
        content = {
            ListItemImage(
                image = { avatarHash },
                contentDescription = contentDescription,
            )
        }
    )
}