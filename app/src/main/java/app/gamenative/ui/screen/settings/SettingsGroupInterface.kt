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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import androidx.compose.runtime.remember

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

    // Load Steam regions from assets
    val steamRegionsMap: Map<Int, String> = remember {
        val jsonString = context.assets.open("steam_regions.json").bufferedReader().use { it.readText() }
        Json.decodeFromString<Map<String, String>>(jsonString).mapKeys { it.key.toInt() }
    }
    val steamRegionsList = remember {
        // Always put 'Automatic' (id 0) first, then sort the rest alphabetically
        val entries = steamRegionsMap.toList()
        val (autoEntries, otherEntries) = entries.partition { it.first == 0 }
        autoEntries + otherEntries.sortedBy { it.second }
    }
    var openRegionDialog by rememberSaveable { mutableStateOf(false) }
    var selectedRegionIndex by rememberSaveable { mutableStateOf(
        steamRegionsList.indexOfFirst { it.first == PrefManager.cellId }.takeIf { it >= 0 } ?: 0
    ) }

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
        var useExternalStorage by rememberSaveable { mutableStateOf(PrefManager.useExternalStorage) }
        SettingsSwitch(
            colors = settingsTileColorsAlt(),
            title = { Text(text = "Write to external storage") },
            subtitle = { Text(text = "Save games to external storage") },
            state = useExternalStorage,
            onCheckedChange = {
                useExternalStorage = it
                PrefManager.useExternalStorage = it
            },
        )
        // Steam download server selection
        SettingsMenuLink(
            colors = settingsTileColorsAlt(),
            title = { Text(text = "Steam Download Server") },
            subtitle = { Text(text = steamRegionsList.getOrNull(selectedRegionIndex)?.second ?: "Default") },
            onClick = { openRegionDialog = true }
        )
    }

    // Steam Download Server choice dialog
    SingleChoiceDialog(
        openDialog = openRegionDialog,
        icon = Icons.Default.Map,
        iconDescription = "Steam Download Server",
        title = "Steam Download Server",
        items = steamRegionsList.map { it.second },
        currentItem = selectedRegionIndex,
        onSelected = { index ->
            selectedRegionIndex = index
            val selectedId = steamRegionsList[index].first
            PrefManager.cellId = selectedId
            PrefManager.cellIdManuallySet = selectedId != 0
        },
        onDismiss = { openRegionDialog = false }
    )
}
