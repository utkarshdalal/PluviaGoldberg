package com.OxGames.Pluvia.ui.screen.settings

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.OxGames.Pluvia.BuildConfig
import com.OxGames.Pluvia.PrefManager
import com.OxGames.Pluvia.ui.component.dialog.CrashLogDialog
import com.alorma.compose.settings.ui.SettingsGroup
import com.alorma.compose.settings.ui.SettingsMenuLink
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoilApi::class)
@Composable
fun SettingsGroupDebug() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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

        if (BuildConfig.DEBUG) {
            SettingsMenuLink(
                title = { Text(text = "Clear Preferences") },
                onClick = {
                    scope.launch {
                        PrefManager.clearPreferences()
                        (context as ComponentActivity).finishAffinity()
                    }
                },
            )

            SettingsMenuLink(
                title = { Text(text = "Clear Image Cache") },
                onClick = {
                    context.imageLoader.diskCache?.clear()
                    context.imageLoader.memoryCache?.clear()
                },
            )
        }
    }
}
