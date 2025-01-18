package com.OxGames.Pluvia.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.OxGames.Pluvia.SteamService
import `in`.dragonbra.javasteam.util.HardwareUtils
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.name

object SteamUtils {
    /**
     * Strips non-ASCII characters from String
     */
    fun removeSpecialChars(s: String): String {
        return s.replace(Regex("[^\\u0000-\\u007F]"), "")
    }

    /**
     * Replaces any existing `steam_api.dll` or `steam_api64.dll` in the app directory
     * with our pipe dll stored in assets
     */
    fun replaceSteamApi(context: Context, appId: Int) {
        val appDirPath = SteamService.getAppDirPath(appId)
        FileUtils.walkThroughPath(Paths.get(appDirPath), -1) {
            if (it.name == "steam_api.dll" && it.exists()) {
                Files.delete(it)
                Files.createFile(it)
                FileOutputStream(it.absolutePathString()).use { fos ->
                    context.assets.open("steampipe/steam_api.dll").use { fs ->
                        fs.copyTo(fos)
                    }
                }
            }
            if (it.name == "steam_api64.dll" && it.exists()) {
                Files.delete(it)
                Files.createFile(it)
                FileOutputStream(it.absolutePathString()).use { fos ->
                    context.assets.open("steampipe/steam_api64.dll").use { fs ->
                        fs.copyTo(fos)
                    }
                }
            }
        }
    }

    /**
     * Gets the Android user-editable device name or falls back to [HardwareUtils.getMachineName]
     */
    fun getMachineName(context: Context): String {
        return try {
            // Try different methods to get device name
            Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
                ?: Settings.System.getString(context.contentResolver, "device_name")
                // ?: Settings.Secure.getString(context.contentResolver, "bluetooth_name")
                // ?: BluetoothAdapter.getDefaultAdapter()?.name
                ?: HardwareUtils.getMachineName() // Fallback to machine name if all else fails
        } catch (e: Exception) {
            HardwareUtils.getMachineName() // Return machine name as last resort
        }
    }

    /**
     * This ID is unique to the device and app combination
     */
    @SuppressLint("HardwareIds")
    fun getUniqueDeviceId(context: Context): Int {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        return androidId.hashCode()
    }
}
