package com.OxGames.Pluvia.utils

import android.content.Context
import com.OxGames.Pluvia.SteamService
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.name

class SteamUtils {
    companion object {
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
    }
}