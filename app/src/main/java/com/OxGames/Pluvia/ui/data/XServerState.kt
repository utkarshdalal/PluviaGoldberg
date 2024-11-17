package com.OxGames.Pluvia.ui.data

import com.winlator.container.Container
import com.winlator.container.Shortcut
import com.winlator.core.EnvVars
import com.winlator.core.KeyValueSet
import com.winlator.core.OnExtractFileListener
import com.winlator.core.WineInfo

data class XServerState(
    // val envVars: EnvVars = EnvVars(),
    val dxwrapper: String = Container.DEFAULT_DXWRAPPER,
    var shortcut: Shortcut? = null,
    var onExtractFileListener: OnExtractFileListener? = null,
    val dxwrapperConfig: KeyValueSet? = null,
    val screenSize: String = Container.DEFAULT_SCREEN_SIZE,
    val wineInfo: WineInfo = WineInfo.MAIN_WINE_VERSION,
    val graphicsDriver: String = Container.DEFAULT_GRAPHICS_DRIVER,
    val audioDriver: String = Container.DEFAULT_AUDIO_DRIVER,
)