package com.OxGames.Pluvia.utils

import android.content.Context
import com.OxGames.Pluvia.PrefManager
import com.OxGames.Pluvia.SteamService
import com.winlator.container.Container
import com.winlator.container.ContainerData
import com.winlator.container.ContainerManager
import com.winlator.core.WineThemeManager
import org.json.JSONObject
import timber.log.Timber

object ContainerUtils {
    fun getDefaultContainerData(): ContainerData {
        return ContainerData(
            screenSize = PrefManager.screenSize,
            envVars = PrefManager.envVars,
            graphicsDriver = PrefManager.graphicsDriver,
            dxwrapper = PrefManager.dxWrapper,
            dxwrapperConfig = PrefManager.dxWrapperConfig,
            audioDriver = PrefManager.audioDriver,
            wincomponents = PrefManager.winComponents,
            drives = PrefManager.drives,
            showFPS = PrefManager.showFps,
            cpuList = PrefManager.cpuList,
            cpuListWoW64 = PrefManager.cpuListWoW64,
            wow64Mode = PrefManager.wow64Mode,
            startupSelection = PrefManager.startupSelection.toByte(),
            box86Preset = PrefManager.box86Preset,
            box64Preset = PrefManager.box64Preset,
        )
    }
    fun setDefaultContainerData(containerData: ContainerData) {
        PrefManager.screenSize = containerData.screenSize
        PrefManager.envVars = containerData.envVars
        PrefManager.graphicsDriver = containerData.graphicsDriver
        PrefManager.dxWrapper = containerData.dxwrapper
        PrefManager.dxWrapperConfig = containerData.dxwrapperConfig
        PrefManager.audioDriver = containerData.audioDriver
        PrefManager.winComponents = containerData.wincomponents
        PrefManager.drives = containerData.drives
        PrefManager.showFps = containerData.showFPS
        PrefManager.cpuList = containerData.cpuList
        PrefManager.cpuListWoW64 = containerData.cpuListWoW64
        PrefManager.wow64Mode = containerData.wow64Mode
        PrefManager.startupSelection = containerData.startupSelection.toInt()
        PrefManager.box86Preset = containerData.box86Preset
        PrefManager.box64Preset = containerData.box64Preset
    }

    fun toContainerData(container: Container): ContainerData {
        return ContainerData(
            name = container.name,
            screenSize = container.screenSize,
            envVars = container.envVars,
            graphicsDriver = container.graphicsDriver,
            dxwrapper = container.dxWrapper,
            dxwrapperConfig = container.dxWrapperConfig,
            audioDriver = container.audioDriver,
            wincomponents = container.winComponents,
            drives = container.drives,
            showFPS = container.isShowFPS,
            cpuList = container.cpuList,
            cpuListWoW64 = container.cpuListWoW64,
            wow64Mode = container.isWoW64Mode,
            startupSelection = container.startupSelection.toByte(),
            box86Preset = container.box86Preset,
            box64Preset = container.box64Preset,
            desktopTheme = container.desktopTheme,
        )
    }
    fun applyToContainer(context: Context, appId: Int, containerData: ContainerData) {
        val containerManager = ContainerManager(context)
        if (containerManager.hasContainer(appId)) {
            val container = containerManager.getContainerById(appId)
            container.name = containerData.name
            container.screenSize = containerData.screenSize
            container.envVars = containerData.envVars
            container.graphicsDriver = containerData.graphicsDriver
            container.dxWrapper = containerData.dxwrapper
            container.dxWrapperConfig = containerData.dxwrapperConfig
            container.audioDriver = containerData.audioDriver
            container.winComponents = containerData.wincomponents
            container.drives = containerData.drives
            container.isShowFPS = containerData.showFPS
            container.cpuList = containerData.cpuList
            container.cpuListWoW64 = containerData.cpuListWoW64
            container.isWoW64Mode = containerData.wow64Mode
            container.startupSelection = containerData.startupSelection
            container.box86Preset = containerData.box86Preset
            container.box64Preset = containerData.box64Preset
            container.desktopTheme = containerData.desktopTheme
            container.saveData()
        } else {
            throw Exception("Container does not exist for $appId")
        }
    }

    fun getContainerId(appId: Int): Int {
        // TODO: set up containers for each appId+depotId combo (intent extra "container_id")
        return appId
    }
    fun hasContainer(context: Context, appId: Int): Boolean {
        val containerId = getContainerId(appId)
        val containerManager = ContainerManager(context)
        return containerManager.hasContainer(containerId)
    }
    fun getContainer(context: Context, appId: Int): Container {
        val containerId = getContainerId(appId)

        val containerManager = ContainerManager(context)
        return if (containerManager.hasContainer(containerId)) {
            containerManager.getContainerById(containerId)
        } else {
            throw Exception("Container does not exist for game $appId")
        }
    }
    fun getOrCreateContainer(context: Context, appId: Int): Container {
        val containerId = getContainerId(appId)

        val containerManager = ContainerManager(context)
        return if (containerManager.hasContainer(containerId)) {
            containerManager.getContainerById(containerId)
        } else {
            // set up container drives to include app
            val defaultDrives = PrefManager.drives
            val appDirPath = SteamService.getAppDirPath(appId)
            var drive: Char = 'A'
            while (defaultDrives.contains("$drive:")) {
                drive += 1
                if (drive > 'Z') {
                    throw Exception("Could not find suitable drive for app dir path")
                }
            }
            val drives = "$defaultDrives$drive:$appDirPath"
            Timber.d("Prepared container drives: $drives")

            val data = JSONObject()
            data.put("name", "container_$containerId")
            data.put("screenSize", PrefManager.screenSize)
            data.put("envVars", PrefManager.envVars)
            data.put("cpuList", PrefManager.cpuList)
            data.put("cpuListWoW64", PrefManager.cpuListWoW64)
            data.put("graphicsDriver", PrefManager.graphicsDriver)
            data.put("dxwrapper", PrefManager.dxWrapper)
            data.put("dxwrapperConfig", PrefManager.dxWrapperConfig)
            data.put("audioDriver", PrefManager.audioDriver)
            data.put("wincomponents", PrefManager.winComponents)
            data.put("drives", drives)
            data.put("showFPS", PrefManager.showFps)
            data.put("wow64Mode", PrefManager.wow64Mode)
            data.put("startupSelection", PrefManager.startupSelection)
            data.put("box86Preset", PrefManager.box86Preset)
            data.put("box64Preset", PrefManager.box64Preset)
            data.put("desktopTheme", WineThemeManager.DEFAULT_DESKTOP_THEME)
            containerManager.createContainerFuture(containerId, data).get()
        }
    }
}
