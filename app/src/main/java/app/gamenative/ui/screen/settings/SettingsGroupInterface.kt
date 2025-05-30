package app.gamenative.ui.screen.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import app.gamenative.PrefManager
import app.gamenative.enums.AppTheme
import app.gamenative.ui.component.dialog.SingleChoiceDialog
import app.gamenative.ui.enums.HomeDestination
import app.gamenative.ui.theme.settingsTileColors
import app.gamenative.ui.theme.settingsTileColorsAlt
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.alorma.compose.settings.ui.SettingsSwitch
import com.materialkolor.PaletteStyle

@Composable
fun SettingsGroupInterface(
    appTheme: AppTheme,
    paletteStyle: PaletteStyle,
    onAppTheme: (AppTheme) -> Unit,
    onPaletteStyle: (PaletteStyle) -> Unit,
) {
    val context = LocalContext.current

    var openWebLinks by rememberSaveable { mutableStateOf(PrefManager.openWebLinksExternally) }

    var openAppThemeDialog by rememberSaveable { mutableStateOf(false) }
    var openAppPaletteDialog by rememberSaveable { mutableStateOf(false) }

    var openStartScreenDialog by rememberSaveable { mutableStateOf(false) }
    var startScreenOption by rememberSaveable(openStartScreenDialog) { mutableStateOf(PrefManager.startScreen) }

    SettingsGroup(title = { Text(text = "Interface") }) {
        SettingsSwitch(
            colors = settingsTileColorsAlt(),
            title = { Text(text = "Open web links externally") },
            subtitle = { Text(text = "Links open with your main web browser") },
            state = openWebLinks,
            onCheckedChange = {
                openWebLinks = it
                PrefManager.openWebLinksExternally = it
            },
        )
    }

    // Downloads settings
    SettingsGroup(title = { Text(text = "Downloads") }) {
        var wifiOnlyDownload by rememberSaveable { mutableStateOf(PrefManager.downloadOnWifiOnly) }
        SettingsSwitch(
            colors = settingsTileColorsAlt(),
            title = { Text(text = "Download only over Wi-Fi") },
            subtitle = { Text(text = "Prevent downloads on cellular data") },
            state = wifiOnlyDownload,
            onCheckedChange = {
                wifiOnlyDownload = it
                PrefManager.downloadOnWifiOnly = it
            },
        )
    }
}
