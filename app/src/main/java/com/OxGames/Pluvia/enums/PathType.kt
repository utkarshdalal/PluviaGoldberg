package com.OxGames.Pluvia.enums

import android.content.Context
import com.OxGames.Pluvia.SteamService
import com.winlator.xenvironment.ImageFs
import timber.log.Timber
import java.nio.file.Paths

enum class PathType {
    GameInstall,
    WinMyDocuments,
    WinAppDataLocal,
    WinAppDataLocalLow,
    WinAppDataRoaming,
    WinSavedGames,
    LinuxHome,
    LinuxXdgDataHome,
    LinuxXdgConfigHome,
    MacHome,
    MacAppSupport,
    None,
    ;

    /**
     * Turns a path type to a full path through the android system to the expected directory in
     * the wine prefix or the steam common app dir. Make sure to run
     * [com.winlator.container.ContainerManager.activateContainer] on the proper
     * [com.winlator.container.Container] beforehand.
     */
    fun toAbsPath(context: Context, appId: Int): String {
        var path = when (this) {
            GameInstall -> SteamService.getAppDirPath(appId)
            WinMyDocuments -> Paths.get(
                ImageFs.find(context).rootDir.absolutePath,
                ImageFs.WINEPREFIX,
                "/drive_c/users/",
                ImageFs.USER,
                "Documents/",
            ).toString()
            WinAppDataLocal -> Paths.get(
                ImageFs.find(context).rootDir.absolutePath,
                ImageFs.WINEPREFIX,
                "/drive_c/users/",
                ImageFs.USER,
                "AppData/Local/",
            ).toString()
            WinAppDataLocalLow -> Paths.get(
                ImageFs.find(context).rootDir.absolutePath,
                ImageFs.WINEPREFIX,
                "/drive_c/users/",
                ImageFs.USER,
                "AppData/LocalLow/",
            ).toString()
            WinAppDataRoaming -> Paths.get(
                ImageFs.find(context).rootDir.absolutePath,
                ImageFs.WINEPREFIX,
                "/drive_c/users/",
                ImageFs.USER,
                "AppData/Roaming/",
            ).toString()
            WinSavedGames -> Paths.get(
                ImageFs.find(context).rootDir.absolutePath,
                ImageFs.WINEPREFIX,
                "/drive_c/users/",
                ImageFs.USER,
                "Saved Games/",
            ).toString()
            else -> {
                Timber.e("Did not recognize or unsupported path type $this")
                SteamService.getAppDirPath(appId)
            }
        }
        return if (!path.endsWith("/")) "$path/" else path
    }

    fun isWindows(): Boolean = when (this) {
        GameInstall,
        WinMyDocuments,
        WinAppDataLocal,
        WinAppDataLocalLow,
        WinAppDataRoaming,
        WinSavedGames,
        -> true
        else -> false
    }

    companion object {
        fun from(keyValue: String?): PathType {
            return when (keyValue?.lowercase()) {
                "%${GameInstall.name.lowercase()}%",
                GameInstall.name.lowercase(),
                -> GameInstall
                "%${WinMyDocuments.name.lowercase()}%",
                WinMyDocuments.name.lowercase(),
                -> WinMyDocuments
                "%${WinAppDataLocal.name.lowercase()}%",
                WinAppDataLocal.name.lowercase(),
                -> WinAppDataLocal
                "%${WinAppDataLocalLow.name.lowercase()}%",
                WinAppDataLocalLow.name.lowercase(),
                -> WinAppDataLocalLow
                "%${WinAppDataRoaming.name.lowercase()}%",
                WinAppDataRoaming.name.lowercase(),
                -> WinAppDataRoaming
                "%${WinSavedGames.name.lowercase()}%",
                WinSavedGames.name.lowercase(),
                -> WinSavedGames
                "%${LinuxHome.name.lowercase()}%",
                LinuxHome.name.lowercase(),
                -> LinuxHome
                "%${LinuxXdgDataHome.name.lowercase()}%",
                LinuxXdgDataHome.name.lowercase(),
                -> LinuxXdgDataHome
                "%${LinuxXdgConfigHome.name.lowercase()}%",
                LinuxXdgConfigHome.name.lowercase(),
                -> LinuxXdgConfigHome
                "%${MacHome.name.lowercase()}%",
                MacHome.name.lowercase(),
                -> MacHome
                "%${MacAppSupport.name.lowercase()}%",
                MacAppSupport.name.lowercase(),
                -> MacAppSupport
                else -> {
                    if (keyValue != null) {
                        Timber.w("Could not identify $keyValue as PathType")
                    }
                    None
                }
            }
        }
    }
}
