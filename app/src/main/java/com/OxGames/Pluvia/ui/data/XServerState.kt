package com.OxGames.Pluvia.ui.data

import com.winlator.container.Container
import com.winlator.core.KeyValueSet
import com.winlator.core.OnExtractFileListener
import com.winlator.core.WineInfo

data class XServerState(
    var winStarted: Boolean = false,
    val dxwrapper: String = Container.DEFAULT_DXWRAPPER,
    val dxwrapperConfig: KeyValueSet? = null,
    val screenSize: String = Container.DEFAULT_SCREEN_SIZE,
    val wineInfo: WineInfo = WineInfo.MAIN_WINE_VERSION,
    val graphicsDriver: String = Container.DEFAULT_GRAPHICS_DRIVER,
    val audioDriver: String = Container.DEFAULT_AUDIO_DRIVER,
)
