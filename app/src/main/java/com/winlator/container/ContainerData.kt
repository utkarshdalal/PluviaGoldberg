package com.winlator.container

import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.ui.graphics.vector.ImageVector
import com.OxGames.Pluvia.ui.enums.DialogType
import com.winlator.box86_64.Box86_64Preset
import com.winlator.core.WineThemeManager
import kotlin.String

data class ContainerData(
    val name: String = "",
    val screenSize: String = Container.DEFAULT_SCREEN_SIZE,
    val envVars: String = Container.DEFAULT_ENV_VARS,
    val graphicsDriver: String = Container.DEFAULT_GRAPHICS_DRIVER,
    val dxwrapper: String = Container.DEFAULT_DXWRAPPER,
    val dxwrapperConfig: String = "",
    val audioDriver: String = Container.DEFAULT_AUDIO_DRIVER,
    val wincomponents: String = Container.DEFAULT_WINCOMPONENTS,
    val drives: String = "",
    val showFPS: Boolean = false,
    val cpuList: String = Container.getFallbackCPUList(),
    val cpuListWoW64: String = Container.getFallbackCPUListWoW64(),
    val wow64Mode: Boolean = false,
    val startupSelection: Byte = Container.STARTUP_SELECTION_ESSENTIAL,
    val box86Preset: String = Box86_64Preset.COMPATIBILITY,
    val box64Preset: String = Box86_64Preset.COMPATIBILITY,
    val desktopTheme: String = WineThemeManager.DEFAULT_DESKTOP_THEME,
) {
    companion object {
        val Saver = mapSaver(
            save = { state ->
                mapOf(
                    "name" to state.name,
                    "screenSize" to state.screenSize,
                    "envVars" to state.envVars,
                    "graphicsDriver" to state.graphicsDriver,
                    "dxwrapper" to state.dxwrapper,
                    "dxwrapperConfig" to state.dxwrapperConfig,
                    "audioDriver" to state.audioDriver,
                    "wincomponents" to state.wincomponents,
                    "drives" to state.drives,
                    "showFPS" to state.showFPS,
                    "cpuList" to state.cpuList,
                    "cpuListWoW64" to state.cpuListWoW64,
                    "wow64Mode" to state.wow64Mode,
                    "startupSelection" to state.startupSelection,
                    "box86Preset" to state.box86Preset,
                    "box64Preset" to state.box64Preset,
                    "desktopTheme" to state.desktopTheme,
                )
            },
            restore = { savedMap ->
                ContainerData(
                    name = savedMap["name"] as String,
                    screenSize = savedMap["screenSize"] as String,
                    envVars = savedMap["envVars"] as String,
                    graphicsDriver = savedMap["graphicsDriver"] as String,
                    dxwrapper = savedMap["dxwrapper"] as String,
                    dxwrapperConfig = savedMap["dxwrapperConfig"] as String,
                    audioDriver = savedMap["audioDriver"] as String,
                    wincomponents = savedMap["wincomponents"] as String,
                    drives = savedMap["drives"] as String,
                    showFPS = savedMap["showFPS"] as Boolean,
                    cpuList = savedMap["cpuList"] as String,
                    cpuListWoW64 = savedMap["cpuListWoW64"] as String,
                    wow64Mode = savedMap["wow64Mode"] as Boolean,
                    startupSelection = savedMap["startupSelection"] as Byte,
                    box86Preset = savedMap["box86Preset"] as String,
                    box64Preset = savedMap["box64Preset"] as String,
                    desktopTheme = savedMap["desktopTheme"] as String,
                )
            },
        )
    }

    fun copy(
        name: String? = null,
        screenSize: String? = null,
        envVars: String? = null,
        graphicsDriver: String? = null,
        dxwrapper: String? = null,
        dxwrapperConfig: String? = null,
        audioDriver: String? = null,
        wincomponents: String? = null,
        drives: String? = null,
        showFPS: Boolean? = null,
        cpuList: String? = null,
        cpuListWoW64: String? = null,
        wow64Mode: Boolean? = null,
        startupSelection: Byte? = null,
        box86Preset: String? = null,
        box64Preset: String? = null,
        desktopTheme: String? = null,
    ): ContainerData {
        return ContainerData(
            name = name ?: this.name,
            screenSize = screenSize ?: this.screenSize,
            envVars = envVars ?: this.envVars,
            graphicsDriver = graphicsDriver ?: this.graphicsDriver,
            dxwrapper = dxwrapper ?: this.dxwrapper,
            dxwrapperConfig = dxwrapperConfig ?: this.dxwrapperConfig,
            audioDriver = audioDriver ?: this.audioDriver,
            wincomponents = wincomponents ?: this.wincomponents,
            drives = drives ?: this.drives,
            showFPS = showFPS ?: this.showFPS,
            cpuList = cpuList ?: this.cpuList,
            cpuListWoW64 = cpuListWoW64 ?: this.cpuListWoW64,
            wow64Mode = wow64Mode ?: this.wow64Mode,
            startupSelection = startupSelection ?: this.startupSelection,
            box86Preset = box86Preset ?: this.box86Preset,
            box64Preset = box64Preset ?: this.box64Preset,
            desktopTheme = desktopTheme ?: this.desktopTheme,
        )
    }
}
