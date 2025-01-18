package com.OxGames.Pluvia.ui.screen.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import com.OxGames.Pluvia.Constants
import com.OxGames.Pluvia.PrefManager
import com.OxGames.Pluvia.ui.component.dialog.LibrariesDialog
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch

@Composable
fun SettingsGroupInfo() {
    SettingsGroup(title = { Text(text = "Info") }) {
        val uriHandler = LocalUriHandler.current
        var askForTip by rememberSaveable { mutableStateOf(!PrefManager.tipped) }
        var showLibrariesDialog by rememberSaveable { mutableStateOf(false) }

        LibrariesDialog(
            visible = showLibrariesDialog,
            onDismissRequest = { showLibrariesDialog = false },
        )

        SettingsMenuLink(
            title = { Text(text = "Source code") },
            subtitle = { Text(text = "View the source code of this project") },
            onClick = { uriHandler.openUri(Constants.Misc.GITHUB_LINK) },
        )

        SettingsMenuLink(
            title = { Text(text = "Libraries Used") },
            subtitle = { Text(text = "See what technologies make Pluvia possible") },
            onClick = { showLibrariesDialog = true },
        )

        SettingsMenuLink(
            title = { Text("Send tip") },
            subtitle = { Text(text = "Contribute to ongoing development") },
            icon = { Icon(imageVector = Icons.Filled.MonetizationOn, contentDescription = "Tip") },
            onClick = {
                uriHandler.openUri(Constants.Misc.TIP_JAR_LINK)
                askForTip = false
                PrefManager.tipped = !askForTip
            },
        )

        SettingsSwitch(
            state = askForTip,
            title = { Text("Ask for tip on startup") },
            subtitle = { Text(text = "Stops the tip message from appearing") },
            onCheckedChange = {
                askForTip = it
                PrefManager.tipped = !askForTip
            },
        )

        SettingsMenuLink(
            title = { Text(text = "Privacy Policy") },
            subtitle = { Text(text = "Opens a link to Pluvia's privacy policy") },
            onClick = {
                uriHandler.openUri(Constants.Misc.PRIVACY_LINK)
            },
        )
    }
}
