package com.OxGames.Pluvia.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.OxGames.Pluvia.PrefManager
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch

@Composable
fun SettingsGroupInfo() {
    SettingsGroup(title = { Text(text = "Info") }) {
        val uriHandler = LocalUriHandler.current
        var askForTip by remember { mutableStateOf(!PrefManager.tipped) }

        SettingsMenuLink(
            title = { Text(text = "Source code") },
            subtitle = { Text(text = "github.com/oxters168/Pluvia") },
            onClick = {
                uriHandler.openUri("https://github.com/oxters168/Pluvia")
            },
        )

        SettingsMenuLink(
            enabled = false,
            title = { Text(text = "Technologies Used") },
            subtitle = {
                Column(
                    modifier = Modifier.padding(start = 12.dp),
                ) {
                    Text(text = "JavaSteam - github.com/Longi94/JavaSteam")
                    Text(text = "Ubuntu RootFs - releases.ubuntu.com/focal")
                    Text(text = "Wine - winehq.org")
                    Text(text = "Box86/Box64 - box86.org")
                    Text(text = "PRoot - proot-me.github.io")
                    Text(text = "Mesa (Turnip/Zink/VirGL) - mesa3d.org")
                    Text(text = "DXVK - github.com/doitsujin/dxvk")
                    Text(text = "VKD3D - gitlab.winehq.org/wine/vkd3d")
                    Text(text = "D8VK - github.com/AlpyneDreams/d8vk")
                    Text(text = "CNC DDraw - github.com/FunkyFr3sh/cnc-ddraw")
                }
            },
            onClick = {
                /* Could link to pluvia repo to credits page? */
            },
        )

        SettingsMenuLink(
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Send tip")
                    Icon(imageVector = Icons.Filled.MonetizationOn, contentDescription = "Tip")
                }
            },
            onClick = {
                uriHandler.openUri("https://buy.stripe.com/5kAaFU1bx2RFeLmbII")
                askForTip = false
                PrefManager.tipped = !askForTip
            },
        )

        SettingsSwitch(
            state = askForTip,
            title = { Text("Ask for tip on startup") },
            onCheckedChange = {
                askForTip = it
                PrefManager.tipped = !askForTip
            }
        )

        SettingsMenuLink(
            title = { Text(text = "Privacy Policy") },
            onClick = {
                uriHandler.openUri("https://github.com/oxters168/Pluvia/tree/master/PrivacyPolicy")
            },
        )
    }
}
