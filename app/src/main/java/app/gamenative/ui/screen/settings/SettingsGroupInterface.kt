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

    SingleChoiceDialog(
        openDialog = openAppThemeDialog,
        icon = Icons.Default.BrightnessMedium,
        title = "App Theme",
        items = AppTheme.entries.map { it.text },
        onSelected = {
            val entry = AppTheme.entries[it]
            onAppTheme(entry)
        },
        currentItem = appTheme.ordinal,
        onDismiss = {
            openAppThemeDialog = false
        },
    )

    SingleChoiceDialog(
        openDialog = openAppPaletteDialog,
        icon = Icons.Default.ColorLens,
        title = "Palette Style",
        items = PaletteStyle.entries.map { it.name },
        onSelected = {
            val entry = PaletteStyle.entries[it]
            onPaletteStyle(entry)
        },
        currentItem = paletteStyle.ordinal,
        onDismiss = {
            openAppPaletteDialog = false
        },
    )

    SingleChoiceDialog(
        openDialog = openStartScreenDialog,
        icon = Icons.Default.Map,
        title = "Start Screen",
        items = HomeDestination.entries.map { context.getString(it.title) },
        onSelected = {
            val entry = HomeDestination.entries[it]
            startScreenOption = entry
            PrefManager.startScreen = entry
        },
        currentItem = startScreenOption.ordinal,
        onDismiss = {
            openStartScreenDialog = false
        },
    )

    SettingsGroup(title = { Text(text = "Interface") }) {
        SettingsMenuLink(
            colors = settingsTileColors(),
            title = { Text(text = "Start Destination") },
            subtitle = { Text(text = "Choose between Library, Downloads, or Friends") },
            onClick = {
                openStartScreenDialog = true
            },
        )
        SettingsMenuLink(
            colors = settingsTileColors(),
            title = { Text(text = "App Theme") },
            subtitle = { Text(text = "Choose between Day, Night, or Auto") },
            onClick = {
                openAppThemeDialog = true
            },
        )
        SettingsMenuLink(
            colors = settingsTileColors(),
            title = { Text(text = "Palette Style") },
            subtitle = { Text(text = "Change the Material Design 3 color palette") },
            onClick = {
                openAppPaletteDialog = true
            },
        )
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
}
