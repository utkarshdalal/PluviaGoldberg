package com.winlator.container

import androidx.compose.runtime.saveable.mapSaver
import com.winlator.box86_64.Box86_64Preset
import com.winlator.core.DefaultVersion
import com.winlator.core.WineThemeManager
import kotlin.String

data class ContainerData(
    val name: String = "",
    val screenSize: String = Container.DEFAULT_SCREEN_SIZE,
    val envVars: String = Container.DEFAULT_ENV_VARS,
    val graphicsDriver: String = Container.DEFAULT_GRAPHICS_DRIVER,
    val graphicsDriverVersion: String = "",
    val dxwrapper: String = Container.DEFAULT_DXWRAPPER,
    val dxwrapperConfig: String = "",
    val audioDriver: String = Container.DEFAULT_AUDIO_DRIVER,
    val wincomponents: String = Container.DEFAULT_WINCOMPONENTS,
    val drives: String = Container.DEFAULT_DRIVES,
    val execArgs: String = "",
    val showFPS: Boolean = false,
    val cpuList: String = Container.getFallbackCPUList(),
    val cpuListWoW64: String = Container.getFallbackCPUListWoW64(),
    val wow64Mode: Boolean = true,
    val startupSelection: Byte = Container.STARTUP_SELECTION_ESSENTIAL,
    val box86Version: String = DefaultVersion.BOX86,
    val box64Version: String = DefaultVersion.BOX64,
    val box86Preset: String = Box86_64Preset.COMPATIBILITY,
    val box64Preset: String = Box86_64Preset.COMPATIBILITY,
    val desktopTheme: String = WineThemeManager.DEFAULT_DESKTOP_THEME,
    // wine registry values
    val csmt: Boolean = true,
    val videoPciDeviceID: Int = 1728,
    val offScreenRenderingMode: String = "fbo",
    val strictShaderMath: Boolean = true,
    val videoMemorySize: String = "2048",
    val mouseWarpOverride: String = "disable",
    val shaderBackend: String = "glsl",
    val useGLSL: String = "enabled",
    val sdlControllerAPI: Boolean = false,
) {
    companion object {
        val Saver = mapSaver(
            save = { state ->
                mapOf(
                    "name" to state.name,
                    "screenSize" to state.screenSize,
                    "envVars" to state.envVars,
                    "graphicsDriver" to state.graphicsDriver,
                    "graphicsDriverVersion" to state.graphicsDriverVersion,
                    "dxwrapper" to state.dxwrapper,
                    "dxwrapperConfig" to state.dxwrapperConfig,
                    "audioDriver" to state.audioDriver,
                    "wincomponents" to state.wincomponents,
                    "drives" to state.drives,
                    "execArgs" to state.execArgs,
                    "showFPS" to state.showFPS,
                    "cpuList" to state.cpuList,
                    "cpuListWoW64" to state.cpuListWoW64,
                    "wow64Mode" to state.wow64Mode,
                    "startupSelection" to state.startupSelection,
                    "box86Version" to state.box86Version,
                    "box64Version" to state.box64Version,
                    "box86Preset" to state.box86Preset,
                    "box64Preset" to state.box64Preset,
                    "desktopTheme" to state.desktopTheme,
                    "sdlControllerAPI" to state.sdlControllerAPI,
                )
            },
            restore = { savedMap ->
                ContainerData(
                    name = savedMap["name"] as String,
                    screenSize = savedMap["screenSize"] as String,
                    envVars = savedMap["envVars"] as String,
                    graphicsDriver = savedMap["graphicsDriver"] as String,
                    graphicsDriverVersion = savedMap["graphicsDriverVersion"] as String,
                    dxwrapper = savedMap["dxwrapper"] as String,
                    dxwrapperConfig = savedMap["dxwrapperConfig"] as String,
                    audioDriver = savedMap["audioDriver"] as String,
                    wincomponents = savedMap["wincomponents"] as String,
                    drives = savedMap["drives"] as String,
                    execArgs = savedMap["execArgs"] as String,
                    showFPS = savedMap["showFPS"] as Boolean,
                    cpuList = savedMap["cpuList"] as String,
                    cpuListWoW64 = savedMap["cpuListWoW64"] as String,
                    wow64Mode = savedMap["wow64Mode"] as Boolean,
                    startupSelection = savedMap["startupSelection"] as Byte,
                    box86Version = savedMap["box86Version"] as String,
                    box64Version = savedMap["box64Version"] as String,
                    box86Preset = savedMap["box86Preset"] as String,
                    box64Preset = savedMap["box64Preset"] as String,
                    desktopTheme = savedMap["desktopTheme"] as String,
                    sdlControllerAPI = savedMap["sdlControllerAPI"] as Boolean,
                )
            },
        )
    }
}
