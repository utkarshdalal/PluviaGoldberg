package com.OxGames.Pluvia.ui.component

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.events.AndroidEvent
import com.OxGames.Pluvia.ui.enums.Orientation
import com.winlator.container.Container
import com.winlator.widget.XServerView
import com.winlator.winhandler.WinHandler
import com.winlator.xserver.ScreenInfo
import com.winlator.xserver.XServer
import java.util.EnumSet

@Composable
fun XServerScreen(lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current) {
    // Log.d("XServerScreen", "Starting up XServer")
    PluviaApp.events.emit(AndroidEvent.SetAppBarVisibility(false))
    PluviaApp.events.emit(AndroidEvent.SetSystemUIVisibility(false))
    PluviaApp.events.emit(AndroidEvent.SetAllowedOrientation(
        EnumSet.of(Orientation.LANDSCAPE, Orientation.REVERSE_LANDSCAPE)) // TOOD: add option for user to pick
    )

    val screenSize = Container.DEFAULT_SCREEN_SIZE

    val xServer = XServer(ScreenInfo(screenSize))
    xServer.winHandler = WinHandler()

    var xServerView: XServerView? = null
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            // Creates view
            XServerView(context, xServer).apply {
                xServerView = this
            }
        },
        update = { view ->
            // View's been inflated or state read in this block has been updated
            // Add logic here if necessary
        }
    )
}

// @Composable
// fun XServerScreen(lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current) {
//     val context = LocalContext.current
//
//     val imageFs = ImageFs.find(context)
//     var screenSize = Container.DEFAULT_SCREEN_SIZE
//     val generateWinePrefix = true // boolean indicating if should install wine (received by intent extra "wineprefixNeedsUpdate")
//     val containerId = 0 // received by intent extra "container_id"
//     val winHandler = WinHandler() // TODO: fix communication which used to be via activity
//
//     if (!generateWinePrefix) {
//         val containerManager = ContainerManager(context)
//         val container = containerManager.getContainerById(containerId)
//
//         val winePrefixNeedsUpdate = container.getExtra("wineprefixNeedsUpdate").equals("t")
//         if (winePrefixNeedsUpdate) {
//             // TODO: show pre-loading dialog
//             WineUtils.updateWineprefix(context) { status ->
//                 if (status == 0) {
//                     container.putExtra("wineprefixNeedsUpdate", null)
//                     container.putExtra("wincomponents", null)
//                     container.saveData()
//                     // TODO: restart activity alternative?
//                 } else {
//                     // TODO: end activity alternative?
//                 }
//             }
//             return
//         }
//
//         val taskAffinityMask = ProcessHelper.getAffinityMask(container.getCPUList(true)).toShort()
//         val taskAffinityMaskWoW64 = ProcessHelper.getAffinityMask(container.getCPUListWoW64(true)).toShort()
//         val firstTimeBoot = container.getExtra("appVersion").isEmpty()
//
//         val wineVersion = container.wineVersion
//         val wineInfo = WineInfo.fromIdentifier(context, wineVersion)
//
//         if (wineInfo != WineInfo.MAIN_WINE_VERSION) imageFs.winePath = wineInfo.path
//
//         val shortcutPath: String? = null // probably the path of the exe to be launched when provided (intent extra "shortcut_path")
//         val shortcut = if (shortcutPath != null && !shortcutPath.isEmpty()) Shortcut(container, File(shortcutPath)) else null
//
//         var graphicsDriver = container.graphicsDriver
//         var audioDriver = container.audioDriver
//         var dxwrapper = container.dxWrapper
//         var dxwrapperConfigRaw = container.dxWrapperConfig
//         screenSize = container.screenSize
//
//         if (shortcut != null) {
//             graphicsDriver = shortcut.getExtra("graphicsDriver", container.graphicsDriver)
//             audioDriver = shortcut.getExtra("audioDriver", container.audioDriver)
//             dxwrapper = shortcut.getExtra("dxwrapper", container.dxWrapper)
//             dxwrapperConfigRaw = shortcut.getExtra("dxwrapperConfig", container.dxWrapperConfig)
//             screenSize = shortcut.getExtra("screenSize", container.screenSize)
//
//             val dinputMapperType = "" // received by intent extra "dinputMapperType"
//             if (!dinputMapperType.isEmpty()) winHandler.dInputMapperType = dinputMapperType.toByte()
//         }
//
//         val dxwrapperConfig = if (dxwrapper.equals("dxvk")) DXVKHelper.parseConfig(dxwrapperConfigRaw) else null
//
//         val onExtractFileListener = if (!wineInfo.isWin64) {
//            object: OnExtractFileListener {
//                 override fun onExtractFile(destination: File?, size: Long): File? {
//                     return destination?.path?.let {
//                         if (it.contains("system32/")) null
//                         else File(it.replace("syswow64/", "system32/"))
//                     }
//                 }
//             }
//         } else { null }
//     }
//
//     // TODO: show loading dialog
//
//     val xServer = XServer(ScreenInfo(screenSize))
//     xServer.winHandler = winHandler
//     val winStarted: BooleanArray = BooleanArray(1) { false }
//
//     var xServerView: XServerView? = null
//     AndroidView(
//         modifier = Modifier.fillMaxSize(),
//         factory = { context ->
//             // Creates view
//             XServerView(context, xServer).apply {
//                 xServerView = this
//             }
//         },
//         update = { view ->
//             // View's been inflated or state read in this block has been updated
//             // Add logic here if necessary
//         }
//     )
//
//     // xServer.windowManager.addOnWindowModificationListener(object: WindowManager.OnWindowModificationListener {
//     //     override fun onUpdateWindowContent(window: Window) {
//     //         if (!winStarted[0] && window.isApplicationWindow()) {
//     //             xServerView?.renderer?.setCursorVisible(true)
//     //             // TODO: close pre-loading dialog
//     //             winStarted[0] = true
//     //         }
//     //
//     //         if (window.id == frameRatingWindowId) frameRating.update()
//     //     }
//     //
//     //     override fun onModifyWindowProperty(window: Window, property: Property) {
//     //         changeFrameRatingVisibility(window, property)
//     //     }
//     //
//     //     override fun onMapWindow(window: Window) {
//     //         assignTaskAffinity(window)
//     //     }
//     //
//     //     override fun onUnmapWindow(window: Window) {
//     //         changeFrameRatingVisibility(window, null)
//     //     }
//     // })
//     //
//     // setupUI()
//     //
//     // CoroutineScope(Dispatchers.IO).launch {
//     //     if (!isGenerateWineprefix()) {
//     //         setupWineSystemFiles()
//     //         extractGraphicsDriverFiles()
//     //         changeWineAudioDriver()
//     //     }
//     //     setupXEnvironment()
//     // }
// }