package com.OxGames.Pluvia.ui.screen.settings

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.OxGames.Pluvia.service.SteamService
import com.OxGames.Pluvia.ui.component.dialog.CrashLogDialog
import com.OxGames.Pluvia.ui.theme.settingsTileColors
import com.OxGames.Pluvia.ui.theme.settingsTileColorsDebug
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import java.io.File

@Suppress("UnnecessaryOptInAnnotation") // ExperimentalFoundationApi
@OptIn(ExperimentalCoilApi::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsGroupDebug() {
    val context = LocalContext.current

    /* Crash Log stuff */
    var showLogcatDialog by rememberSaveable { mutableStateOf(false) }
    var latestCrashFile: File? by rememberSaveable { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        val crashDir = File(context.getExternalFilesDir(null), "crash_logs")
        latestCrashFile = crashDir.listFiles()
            ?.filter { it.name.startsWith("pluvia_crash_") }
            ?.maxByOrNull { it.lastModified() }
    }

    /* Save crash log */
    val saveResultContract = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
    ) { resultUri ->
        try {
            resultUri?.let { uri ->
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    latestCrashFile?.inputStream()?.use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save logcat to destination", Toast.LENGTH_SHORT).show()
        }
    }

    CrashLogDialog(
        visible = showLogcatDialog && latestCrashFile != null,
        fileName = latestCrashFile?.name ?: "No Filename",
        fileText = latestCrashFile?.readText() ?: "Couldn't read crash log.",
        onSave = { latestCrashFile?.let { file -> saveResultContract.launch(file.name) } },
        onDismissRequest = { showLogcatDialog = false },
    )

    SettingsGroup(title = { Text(text = "Debug") }) {
        SettingsMenuLink(
            colors = settingsTileColors(),
            title = { Text(text = "View latest crash") },
            subtitle = {
                val text = if (latestCrashFile != null) {
                    "Shows the most recent crash log"
                } else {
                    "No recent crash logs found"
                }
                Text(text = text)
            },
            enabled = latestCrashFile != null,
            onClick = { showLogcatDialog = true },
        )

        SettingsMenuLink(
            modifier = Modifier.combinedClickable(
                onLongClick = {
                    SteamService.logOut()
                    (context as ComponentActivity).finishAffinity()
                },
                onClick = {
                    Toast.makeText(context, "Long click to activate", Toast.LENGTH_SHORT).show()
                },
            ),
            colors = settingsTileColorsDebug(),
            title = { Text(text = "Clear Preferences") },
            subtitle = { Text("[Closes App] Logs out the client and wipes local preference data.") },
            onClick = {},
        )

        SettingsMenuLink(
            modifier = Modifier.combinedClickable(
                onLongClick = {
                    SteamService.stop()
                    SteamService.clearDatabase()
                    (context as ComponentActivity).finishAffinity()
                },
                onClick = {
                    Toast.makeText(context, "Long click to activate", Toast.LENGTH_SHORT).show()
                },
            ),
            colors = settingsTileColorsDebug(),
            title = { Text(text = "Clear Local Database") },
            subtitle = { Text("[Closes app] May help fix issues with library items or messages.") },
            onClick = {},
        )

        SettingsMenuLink(
            modifier = Modifier.combinedClickable(
                onLongClick = {
                    context.imageLoader.diskCache?.clear()
                    context.imageLoader.memoryCache?.clear()
                },
                onClick = {
                    Toast.makeText(context, "Long click to activate", Toast.LENGTH_SHORT).show()
                },
            ),
            colors = settingsTileColorsDebug(),
            title = { Text(text = "Clear Image Cache") },
            subtitle = { Text(text = "Remove all images that were loaded.") },
            onClick = {},
        )
    }
}
