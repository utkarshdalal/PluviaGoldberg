package com.OxGames.Pluvia.ui.screen.xserver

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.OxGames.Pluvia.Constants
import com.OxGames.Pluvia.PluviaApp
import com.OxGames.Pluvia.PrefManager
import com.OxGames.Pluvia.R
import com.OxGames.Pluvia.data.LaunchInfo
import com.OxGames.Pluvia.events.AndroidEvent
import com.OxGames.Pluvia.events.SteamEvent
import com.OxGames.Pluvia.service.SteamService
import com.OxGames.Pluvia.ui.data.XServerState
import com.OxGames.Pluvia.utils.ContainerUtils
import com.winlator.container.Container
import com.winlator.container.ContainerManager
import com.winlator.core.AppUtils
import com.winlator.core.Callback
import com.winlator.core.DXVKHelper
import com.winlator.core.DefaultVersion
import com.winlator.core.FileUtils
import com.winlator.core.GPUInformation
import com.winlator.core.KeyValueSet
import com.winlator.core.OnExtractFileListener
import com.winlator.core.ProcessHelper
import com.winlator.core.TarCompressorUtils
import com.winlator.core.WineInfo
import com.winlator.core.WineRegistryEditor
import com.winlator.core.WineStartMenuCreator
import com.winlator.core.WineThemeManager
import com.winlator.core.WineUtils
import com.winlator.core.envvars.EnvVars
import com.winlator.inputcontrols.ExternalController
import com.winlator.inputcontrols.TouchMouse
import com.winlator.widget.XServerView
import com.winlator.winhandler.WinHandler
import com.winlator.xconnector.UnixSocketConfig
import com.winlator.xenvironment.ImageFs
import com.winlator.xenvironment.XEnvironment
import com.winlator.xenvironment.components.ALSAServerComponent
import com.winlator.xenvironment.components.GuestProgramLauncherComponent
import com.winlator.xenvironment.components.NetworkInfoUpdateComponent
import com.winlator.xenvironment.components.PulseAudioComponent
import com.winlator.xenvironment.components.SteamClientComponent
import com.winlator.xenvironment.components.SysVSharedMemoryComponent
import com.winlator.xenvironment.components.VirGLRendererComponent
import com.winlator.xenvironment.components.XServerComponent
import com.winlator.xserver.Keyboard
import com.winlator.xserver.Property
import com.winlator.xserver.ScreenInfo
import com.winlator.xserver.Window
import com.winlator.xserver.WindowManager
import com.winlator.xserver.XServer
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.name
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

// TODO logs in composables are 'unstable' which can cause recomposition (performance issues)

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun XServerScreen(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    appId: Int,
    bootToContainer: Boolean,
    navigateBack: () -> Unit,
    onExit: () -> Unit,
    onWindowMapped: ((Window) -> Unit)? = null,
    onWindowUnmapped: ((Window) -> Unit)? = null,
    // xServerViewModel: XServerViewModel = viewModel()
) {
    Timber.i("Starting up XServerScreen")
    val context = LocalContext.current

    // PluviaApp.events.emit(AndroidEvent.SetAppBarVisibility(false))
    PluviaApp.events.emit(AndroidEvent.SetSystemUIVisibility(false))
    PluviaApp.events.emit(
        AndroidEvent.SetAllowedOrientation(PrefManager.allowedOrientation),
    )

    // seems to be used to indicate when a custom wine is being installed (intent extra "generate_wineprefix")
    // val generateWinePrefix = false
    var firstTimeBoot = false
    // val frameRatingWindowId = -1
    var taskAffinityMask = 0
    var taskAffinityMaskWoW64 = 0

    val xServerState = rememberSaveable(stateSaver = XServerState.Saver) {
        if (ContainerUtils.hasContainer(context, appId)) {
            val container = ContainerUtils.getContainer(context, appId)
            mutableStateOf(
                XServerState(
                    graphicsDriver = container.graphicsDriver,
                    audioDriver = container.audioDriver,
                    dxwrapper = container.dxWrapper,
                    dxwrapperConfig = DXVKHelper.parseConfig(container.dxWrapperConfig),
                    screenSize = container.screenSize,
                ),
            )
        } else {
            mutableStateOf(XServerState())
        }
    }

    // val xServer by remember {
    //     val result = mutableStateOf(XServer(ScreenInfo(xServerState.value.screenSize)))
    //     Log.d("XServerScreen", "Remembering xServer as $result")
    //     result
    // }
    // var xEnvironment: XEnvironment? by remember {
    //     val result = mutableStateOf<XEnvironment?>(null)
    //     Log.d("XServerScreen", "Remembering xEnvironment as $result")
    //     result
    // }
    var touchMouse by remember {
        val result = mutableStateOf<TouchMouse?>(null)
        Timber.i("Remembering touchMouse as $result")
        result
    }
    var keyboard by remember { mutableStateOf<Keyboard?>(null) }
    // var pointerEventListener by remember { mutableStateOf<Callback<MotionEvent>?>(null) }

    val appLaunchInfo = SteamService.getAppInfoOf(appId)?.let { appInfo ->
        SteamService.getWindowsLaunchInfos(appId).firstOrNull()
    }

    var xServerView: XServerView? by remember {
        val result = mutableStateOf<XServerView?>(null)
        Timber.i("Remembering xServerView as $result")
        result
    }

    BackHandler {
        Timber.i("BackHandler")
        exit(xServerView!!.getxServer().winHandler, PluviaApp.xEnvironment, onExit)
    }

    DisposableEffect(lifecycleOwner) {
        val onActivityDestroyed: (AndroidEvent.ActivityDestroyed) -> Unit = {
            Timber.i("onActivityDestroyed")
            exit(xServerView!!.getxServer().winHandler, PluviaApp.xEnvironment, onExit)
        }
        val onKeyEvent: (AndroidEvent.KeyEvent) -> Boolean = {
            val isKeyboard = Keyboard.isKeyboardDevice(it.event.device)
            val isGamepad = ExternalController.isGameController(it.event.device)
            // logD("onKeyEvent(${it.event.device.sources})\n\tisGamepad: $isGamepad\n\tisKeyboard: $isKeyboard\n\t${it.event}")

            var handled = false
            if (isGamepad) {
                handled = xServerView!!.getxServer().winHandler.onKeyEvent(it.event)
                // handled = ExternalController.onKeyEvent(xServer.winHandler, it.event)
            }
            if (!handled && isKeyboard) {
                handled = keyboard?.onKeyEvent(it.event) == true
            }
            handled
        }
        val onMotionEvent: (AndroidEvent.MotionEvent) -> Boolean = {
            val isMouse = TouchMouse.isMouseDevice(it.event?.device)
            val isGamepad = ExternalController.isGameController(it.event?.device)
            // logD("onMotionEvent(${it.event?.device?.sources})\n\tisGamepad: $isGamepad\n\tisMouse: $isMouse\n\t${it.event}")

            var handled = false
            if (isGamepad) {
                handled = xServerView!!.getxServer().winHandler.onGenericMotionEvent(it.event)
                // handled = ExternalController.onMotionEvent(xServer.winHandler, it.event)
            }
            if (!handled && isMouse) {
                handled = touchMouse?.onExternalMouseEvent(it.event) == true
            }
            handled
        }
        val onGuestProgramTerminated: (AndroidEvent.GuestProgramTerminated) -> Unit = {
            Timber.i("onGuestProgramTerminated")
            exit(xServerView!!.getxServer().winHandler, PluviaApp.xEnvironment, onExit)
            navigateBack()
        }
        val onForceCloseApp: (SteamEvent.ForceCloseApp) -> Unit = {
            Timber.i("onForceCloseApp")
            exit(xServerView!!.getxServer().winHandler, PluviaApp.xEnvironment, onExit)
            navigateBack()
        }
        val debugCallback = Callback<String> { outputLine ->
            Timber.i(outputLine ?: "")
        }

        PluviaApp.events.on<AndroidEvent.ActivityDestroyed, Unit>(onActivityDestroyed)
        PluviaApp.events.on<AndroidEvent.KeyEvent, Boolean>(onKeyEvent)
        PluviaApp.events.on<AndroidEvent.MotionEvent, Boolean>(onMotionEvent)
        PluviaApp.events.on<AndroidEvent.GuestProgramTerminated, Unit>(onGuestProgramTerminated)
        PluviaApp.events.on<SteamEvent.ForceCloseApp, Unit>(onForceCloseApp)
        ProcessHelper.addDebugCallback(debugCallback)

        onDispose {
            PluviaApp.events.off<AndroidEvent.ActivityDestroyed, Unit>(onActivityDestroyed)
            PluviaApp.events.off<AndroidEvent.KeyEvent, Boolean>(onKeyEvent)
            PluviaApp.events.off<AndroidEvent.MotionEvent, Boolean>(onMotionEvent)
            PluviaApp.events.off<AndroidEvent.GuestProgramTerminated, Unit>(onGuestProgramTerminated)
            PluviaApp.events.off<SteamEvent.ForceCloseApp, Unit>(onForceCloseApp)
            ProcessHelper.removeDebugCallback(debugCallback)
        }
    }

    // var launchedView by rememberSaveable { mutableStateOf(false) }
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .pointerHoverIcon(PointerIcon(0))
            .pointerInteropFilter {
                // Log.d("XServerScreen", "PointerInteropFilter:\n\t$it")
                touchMouse?.onTouchEvent(it)
                true
            },
        factory = { context ->
            // Creates view
            // if (PluviaApp.xServer == null) {
            Timber.i("Creating XServerView and XServer")
            // if (PluviaApp.xServerState == null) {
            //     PluviaApp.xServerState = XServerState()
            // }
            // if (PluviaApp.xServer == null) {
            //     PluviaApp.xServer = XServer(ScreenInfo(xServerState.value.screenSize))
            // }
            XServerView(
                context,
                XServer(ScreenInfo(xServerState.value.screenSize)),
            ).apply {
                xServerView = this
                // pointerEventListener = object: Callback<MotionEvent> {
                //     override fun call(event: MotionEvent) {
                //         Log.d("XServerScreen", "onMotionEvent:\n\t$event")
                //         if (TouchMouse.isMouseDevice(event.device)) {
                //             touchMouse?.onExternalMouseEvent(event) == true
                //         }
                //     }
                // }
                // this.addPointerEventListener(pointerEventListener)
                // this.requestFocus()
                // this.setOnCapturedPointerListener(object: View.OnCapturedPointerListener {
                //     override fun onCapturedPointer(
                //         view: View,
                //         event: MotionEvent
                //     ): Boolean {
                //         Log.d("XServerScreen", "onMotionEvent:\n\t$event")
                //         return if (TouchMouse.isMouseDevice(event.device)) {
                //             touchMouse?.onExternalMouseEvent(event) == true
                //         } else false
                //     }
                //
                // })
                // this.requestPointerCapture()
                // this.background = ColorDrawable(Color.Green.toArgb())
                val renderer = this.renderer
                renderer.isCursorVisible = false
                getxServer().renderer = renderer
                getxServer().winHandler = WinHandler(getxServer(), this)
                touchMouse = TouchMouse(getxServer())
                keyboard = Keyboard(getxServer())
                if (!bootToContainer) {
                    renderer.setUnviewableWMClasses("explorer.exe")
                    // TODO: make 'force fullscreen' be an option of the app being launched
                    appLaunchInfo?.let { renderer.forceFullscreenWMClass = Paths.get(it.executable).name }
                }

                getxServer().windowManager.addOnWindowModificationListener(object : WindowManager.OnWindowModificationListener {
                    override fun onUpdateWindowContent(window: Window) {
                        // Timber.v("onUpdateWindowContent:" +
                        //     "\n\twindowName: ${window.name}" +
                        //     "\n\tprocessId: ${window.processId}" +
                        //     "\n\thasParent: ${window.parent != null}" +
                        //     "\n\tchildrenSize: ${window.children.size}"
                        // )
                        if (!xServerState.value.winStarted && window.isApplicationWindow()) {
                            renderer?.setCursorVisible(true)
                            xServerState.value.winStarted = true
                        }
                        // if (window.id == frameRatingWindowId) frameRating.update()
                    }

                    override fun onModifyWindowProperty(window: Window, property: Property) {
                        // Timber.v("onModifyWindowProperty:" +
                        //     "\n\twindowName: ${window.name}" +
                        //     "\n\tprocessId: ${window.processId}" +
                        //     "\n\thasParent: ${window.parent != null}" +
                        //     "\n\tchildrenSize: ${window.children.size}" +
                        //     "\n\tpropertyName${property.name}"
                        // )
                        // changeFrameRatingVisibility(window, property)
                    }

                    override fun onMapWindow(window: Window) {
                        Timber.i(
                            "onMapWindow:" +
                                "\n\twindowName: ${window.name}" +
                                "\n\twindowClassName: ${window.className}" +
                                "\n\tprocessId: ${window.processId}" +
                                "\n\thasParent: ${window.parent != null}" +
                                "\n\tchildrenSize: ${window.children.size}",
                        )
                        assignTaskAffinity(window, getxServer().winHandler, taskAffinityMask, taskAffinityMaskWoW64)
                        onWindowMapped?.invoke(window)
                    }

                    override fun onUnmapWindow(window: Window) {
                        Timber.i(
                            "onUnmapWindow:" +
                                "\n\twindowName: ${window.name}" +
                                "\n\twindowClassName: ${window.className}" +
                                "\n\tprocessId: ${window.processId}" +
                                "\n\thasParent: ${window.parent != null}" +
                                "\n\tchildrenSize: ${window.children.size}",
                        )
                        // changeFrameRatingVisibility(window, null)
                        onWindowUnmapped?.invoke(window)
                    }
                })

                if (PluviaApp.xEnvironment != null) {
                    PluviaApp.xEnvironment = shiftXEnvironmentToContext(
                        context,
                        xEnvironment = PluviaApp.xEnvironment!!,
                        getxServer(),
                    )
                } else {
                    val containerManager = ContainerManager(context)
                    val container = ContainerUtils.getContainer(context, appId)
                    // Timber.d("1 Container drives: ${container.drives}")
                    containerManager.activateContainer(container)
                    // Timber.d("2 Container drives: ${container.drives}")

                    taskAffinityMask = ProcessHelper.getAffinityMask(container.getCPUList(true)).toShort().toInt()
                    taskAffinityMaskWoW64 = ProcessHelper.getAffinityMask(container.getCPUListWoW64(true)).toShort().toInt()
                    firstTimeBoot = container.getExtra("appVersion").isEmpty()
                    Timber.i("First time boot: $firstTimeBoot")

                    val wineVersion = container.wineVersion
                    xServerState.value = xServerState.value.copy(
                        wineInfo = WineInfo.fromIdentifier(context, wineVersion),
                    )

                    if (xServerState.value.wineInfo != WineInfo.MAIN_WINE_VERSION) {
                        ImageFs.find(context).winePath = xServerState.value.wineInfo.path
                    }

                    val onExtractFileListener = if (!xServerState.value.wineInfo.isWin64) {
                        object : OnExtractFileListener {
                            override fun onExtractFile(destination: File?, size: Long): File? {
                                return destination?.path?.let {
                                    if (it.contains("system32/")) {
                                        null
                                    } else {
                                        File(it.replace("syswow64/", "system32/"))
                                    }
                                }
                            }
                        }
                    } else {
                        null
                    }

                    Timber.i("Doing things once")
                    val envVars = EnvVars()

                    setupWineSystemFiles(
                        context,
                        firstTimeBoot,
                        xServerView!!.getxServer().screenInfo,
                        xServerState,
                        container,
                        containerManager,
                        envVars,
                        onExtractFileListener,
                    )
                    extractGraphicsDriverFiles(
                        context,
                        xServerState.value.graphicsDriver,
                        xServerState.value.dxwrapper,
                        xServerState.value.dxwrapperConfig!!,
                        container,
                        envVars,
                    )
                    changeWineAudioDriver(xServerState.value.audioDriver, container, ImageFs.find(context))
                    PluviaApp.xEnvironment = setupXEnvironment(
                        context,
                        appId,
                        bootToContainer,
                        xServerState,
                        envVars,
                        container,
                        appLaunchInfo,
                        xServerView!!.getxServer(),
                    )
                }
            }

            // } else {
            //     Log.d("XServerScreen", "Creating XServerView without creating XServer")
            //     xServerView = XServerView(context, PluviaApp.xServer)
            // }
            // xServerView
        },
        update = { view ->
            // View's been inflated or state read in this block has been updated
            // Add logic here if necessary
            // view.requestFocus()
        },
        onRelease = { view ->
            // view.releasePointerCapture()
            // pointerEventListener?.let {
            //     view.removePointerEventListener(pointerEventListener)
            //     view.onRelease()
            // }
        },
    )

    // var ranSetup by rememberSaveable { mutableStateOf(false) }
    // LaunchedEffect(lifecycleOwner) {
    //     if (!ranSetup) {
    //         ranSetup = true
    //
    //
    //     }
    // }
}

private fun assignTaskAffinity(
    window: Window,
    winHandler: WinHandler,
    taskAffinityMask: Int,
    taskAffinityMaskWoW64: Int,
) {
    if (taskAffinityMask == 0) return
    val processId = window.getProcessId()
    val className = window.getClassName()
    val processAffinity = if (window.isWoW64()) taskAffinityMaskWoW64 else taskAffinityMask

    if (processId > 0) {
        winHandler.setProcessAffinity(processId, processAffinity)
    } else if (!className.isEmpty()) {
        winHandler.setProcessAffinity(window.getClassName(), processAffinity)
    }
}

private fun shiftXEnvironmentToContext(
    context: Context,
    xEnvironment: XEnvironment,
    xServer: XServer,
): XEnvironment {
    val environment = XEnvironment(context, xEnvironment.imageFs)
    val rootPath = xEnvironment.imageFs.rootDir.path
    xEnvironment.getComponent<SysVSharedMemoryComponent>(SysVSharedMemoryComponent::class.java).stop()
    val sysVSharedMemoryComponent = SysVSharedMemoryComponent(
        xServer,
        UnixSocketConfig.createSocket(rootPath, UnixSocketConfig.SYSVSHM_SERVER_PATH),
    )
    // val sysVSharedMemoryComponent = xEnvironment.getComponent<SysVSharedMemoryComponent>(SysVSharedMemoryComponent::class.java)
    // sysVSharedMemoryComponent.connectToXServer(xServer)
    environment.addComponent(sysVSharedMemoryComponent)
    xEnvironment.getComponent<XServerComponent>(XServerComponent::class.java).stop()
    val xServerComponent = XServerComponent(xServer, UnixSocketConfig.createSocket(rootPath, UnixSocketConfig.XSERVER_PATH))
    // val xServerComponent = xEnvironment.getComponent<XServerComponent>(XServerComponent::class.java)
    // xServerComponent.connectToXServer(xServer)
    environment.addComponent(xServerComponent)
    xEnvironment.getComponent<NetworkInfoUpdateComponent>(NetworkInfoUpdateComponent::class.java).stop()
    val networkInfoComponent = NetworkInfoUpdateComponent()
    environment.addComponent(networkInfoComponent)
    // environment.addComponent(xEnvironment.getComponent<NetworkInfoUpdateComponent>(NetworkInfoUpdateComponent::class.java))
    environment.addComponent(xEnvironment.getComponent<SteamClientComponent>(SteamClientComponent::class.java))
    val alsaComponent = xEnvironment.getComponent<ALSAServerComponent>(ALSAServerComponent::class.java)
    if (alsaComponent != null) {
        environment.addComponent(alsaComponent)
    }
    val pulseComponent = xEnvironment.getComponent<PulseAudioComponent>(PulseAudioComponent::class.java)
    if (pulseComponent != null) {
        environment.addComponent(pulseComponent)
    }
    var virglComponent: VirGLRendererComponent? =
        xEnvironment.getComponent<VirGLRendererComponent>(VirGLRendererComponent::class.java)
    if (virglComponent != null) {
        virglComponent.stop()
        virglComponent = VirGLRendererComponent(
            xServer,
            UnixSocketConfig.createSocket(rootPath, UnixSocketConfig.VIRGL_SERVER_PATH),
        )
        environment.addComponent(virglComponent)
    }
    environment.addComponent(xEnvironment.getComponent<GuestProgramLauncherComponent>(GuestProgramLauncherComponent::class.java))

    FileUtils.clear(XEnvironment.getTmpDir(context))
    sysVSharedMemoryComponent.start()
    xServerComponent.start()
    networkInfoComponent.start()
    virglComponent?.start()
    // environment.startEnvironmentComponents()

    return environment
}
private fun setupXEnvironment(
    context: Context,
    appId: Int,
    bootToContainer: Boolean,
    xServerState: MutableState<XServerState>,
    // xServerViewModel: XServerViewModel,
    envVars: EnvVars,
    // generateWinePrefix: Boolean,
    container: Container?,
    appLaunchInfo: LaunchInfo?,
    // shortcut: Shortcut?,
    xServer: XServer,
): XEnvironment {
    envVars.put("MESA_DEBUG", "silent")
    envVars.put("MESA_NO_ERROR", "1")
    envVars.put("WINEPREFIX", ImageFs.WINEPREFIX)

    val enableWineDebug = true // preferences.getBoolean("enable_wine_debug", false)
    val wineDebugChannels = PrefManager.getString("wine_debug_channels", Constants.XServer.DEFAULT_WINE_DEBUG_CHANNELS)
    envVars.put("WINEDEBUG", if (enableWineDebug && !wineDebugChannels.isEmpty()) "+" + wineDebugChannels.replace(",", ",+") else "-all")

    val imageFs = ImageFs.find(context)
    val rootPath = imageFs.rootDir.path
    FileUtils.clear(imageFs.tmpDir)

    val guestProgramLauncherComponent = GuestProgramLauncherComponent()

    if (container != null) {
        if (container.startupSelection == Container.STARTUP_SELECTION_AGGRESSIVE) xServer.winHandler.killProcess("services.exe")

        val wow64Mode = container.isWoW64Mode
        val guestExecutable =
            xServerState.value.wineInfo.getExecutable(context, wow64Mode) + " explorer /desktop=shell," + xServer.screenInfo + " " +
                getWineStartCommand(appId, container, bootToContainer, appLaunchInfo)
        guestProgramLauncherComponent.isWoW64Mode = wow64Mode
        guestProgramLauncherComponent.guestExecutable = guestExecutable

        envVars.putAll(container.envVars)
        if (!envVars.has("WINEESYNC")) envVars.put("WINEESYNC", "1")

        // Timber.d("3 Container drives: ${container.drives}")
        val bindingPaths = mutableListOf<String>()
        for (drive in container.drivesIterator()) {
            Timber.i("Binding drive ${drive[0]} with path of ${drive[1]}")
            bindingPaths.add(drive[1])
        }
        guestProgramLauncherComponent.bindingPaths = bindingPaths.toTypedArray()
        guestProgramLauncherComponent.box86Version = container.box86Version
        guestProgramLauncherComponent.box64Version = container.box64Version
        guestProgramLauncherComponent.box86Preset = container.box86Preset
        guestProgramLauncherComponent.box64Preset = container.box64Preset
    }

    val environment = XEnvironment(context, imageFs)
    environment.addComponent(
        SysVSharedMemoryComponent(
            xServer,
            UnixSocketConfig.createSocket(rootPath, UnixSocketConfig.SYSVSHM_SERVER_PATH),
        ),
    )
    environment.addComponent(XServerComponent(xServer, UnixSocketConfig.createSocket(rootPath, UnixSocketConfig.XSERVER_PATH)))
    environment.addComponent(NetworkInfoUpdateComponent())
    environment.addComponent(SteamClientComponent())

    // environment.addComponent(SteamClientComponent(UnixSocketConfig.createSocket(
    //     rootPath,
    //     Paths.get(ImageFs.WINEPREFIX, "drive_c", UnixSocketConfig.STEAM_PIPE_PATH).toString()
    // )))
    // environment.addComponent(SteamClientComponent(UnixSocketConfig.createSocket(SteamService.getAppDirPath(appId), "/steam_pipe")))
    // environment.addComponent(SteamClientComponent(UnixSocketConfig.createSocket(rootPath, UnixSocketConfig.STEAM_PIPE_PATH)))

    if (xServerState.value.audioDriver == "alsa") {
        envVars.put("ANDROID_ALSA_SERVER", UnixSocketConfig.ALSA_SERVER_PATH)
        envVars.put("ANDROID_ASERVER_USE_SHM", "true")
        environment.addComponent(ALSAServerComponent(UnixSocketConfig.createSocket(rootPath, UnixSocketConfig.ALSA_SERVER_PATH)))
    } else if (xServerState.value.audioDriver == "pulseaudio") {
        envVars.put("PULSE_SERVER", UnixSocketConfig.PULSE_SERVER_PATH)
        environment.addComponent(PulseAudioComponent(UnixSocketConfig.createSocket(rootPath, UnixSocketConfig.PULSE_SERVER_PATH)))
    }

    if (xServerState.value.graphicsDriver == "virgl") {
        environment.addComponent(
            VirGLRendererComponent(
                xServer,
                UnixSocketConfig.createSocket(rootPath, UnixSocketConfig.VIRGL_SERVER_PATH),
            ),
        )
    }

    guestProgramLauncherComponent.envVars = envVars
    guestProgramLauncherComponent.setTerminationCallback { status ->
        PluviaApp.events.emit(AndroidEvent.GuestProgramTerminated)
    }
    environment.addComponent(guestProgramLauncherComponent)

    // if (generateWinePrefix) {
    //     generateWineprefix(
    //         context,
    //         imageFs,
    //         xServerState.value.wineInfo,
    //         envVars,
    //         environment,
    //     )
    // }
    environment.startEnvironmentComponents()

    // put in separate scope since winhandler start method does some network stuff
    CoroutineScope(Dispatchers.IO).launch {
        xServer.winHandler.start()
    }
    envVars.clear()
    xServerState.value = xServerState.value.copy(
        dxwrapperConfig = null,
    )
    return environment
}
private fun getWineStartCommand(
    appId: Int,
    container: Container,
    bootToContainer: Boolean,
    appLaunchInfo: LaunchInfo?,
): String {
    val tempDir = File(container.rootDir, ".wine/drive_c/windows/temp")
    FileUtils.clear(tempDir)

    // Log.d("XServerScreen", "Converting $appLocalExe to wine start command")
    val args = if (bootToContainer || appLaunchInfo == null) {
        "\"wfm.exe\""
    } else {
        val appDirPath = SteamService.getAppDirPath(appId)
        val drives = container.drives
        val driveIndex = drives.indexOf(appDirPath)
        // greater than 1 since there is the drive character and the colon before the app dir path
        val drive = if (driveIndex > 1) {
            drives[driveIndex - 2]
        } else {
            Timber.e("Could not locate game drive")
            'D'
        }
        "/dir $drive:/${appLaunchInfo.workingDir} \"${appLaunchInfo.executable}\""
    }

    return "winhandler.exe $args"
}
private fun exit(winHandler: WinHandler?, environment: XEnvironment?, onExit: () -> Unit) {
    Timber.i("Exit called")
    winHandler?.stop()
    environment?.stopEnvironmentComponents()
    // AppUtils.restartApplication(this)
    // PluviaApp.xServerState = null
    // PluviaApp.xServer = null
    // PluviaApp.xServerView = null
    PluviaApp.xEnvironment = null
    // PluviaApp.touchMouse = null
    // PluviaApp.keyboard = null
    onExit()
}

private fun generateWineprefix(
    context: Context,
    imageFs: ImageFs,
    wineInfo: WineInfo,
    envVars: EnvVars,
    environment: XEnvironment,
) {
    // Intent intent = getIntent()
    // public static final @IntRange(from = 1, to = 19) byte CONTAINER_PATTERN_COMPRESSION_LEVEL = 9

    val rootDir = imageFs.rootDir
    val installedWineDir = imageFs.installedWineDir
    // wineInfo = intent.getParcelableExtra("wine_info")
    // WineUtils.extractWineFileForInstallAsync(context, )
    // WineUtils.findWineVersionAsync(context, )
    envVars.put("WINEARCH", if (wineInfo.isWin64) "win64" else "win32")
    imageFs.winePath = wineInfo.path

    val containerPatternDir = File(installedWineDir, "/preinstall/container-pattern")
    if (containerPatternDir.isDirectory) FileUtils.delete(containerPatternDir)
    containerPatternDir.mkdirs()

    val linkFile = File(rootDir, ImageFs.HOME_PATH)
    linkFile.delete()
    FileUtils.symlink(".." + FileUtils.toRelativePath(rootDir.path, containerPatternDir.path), linkFile.path)

    val guestProgramLauncherComponent = environment.getComponent(GuestProgramLauncherComponent::class.java)
    guestProgramLauncherComponent.guestExecutable =
        wineInfo.getExecutable(context, false) + " explorer /desktop=shell," + Container.DEFAULT_SCREEN_SIZE + " winecfg"

    // val preloaderDialog = PreloaderDialog(context)
    guestProgramLauncherComponent.terminationCallback = (
        object : Callback<Int> {
            override fun call(status: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    if (status > 0) {
                        // AppUtils.showToast(context, R.string.unable_to_install_wine)
                        FileUtils.delete(File(installedWineDir, "/preinstall"))
                        AppUtils.restartApplication(context)
                        return@launch
                    }

                    // preloaderDialog.showOnUiThread(R.string.finishing_installation)
                    // TODO: show loading modal
                    FileUtils.writeString(File(rootDir, ImageFs.WINEPREFIX + "/.update-timestamp"), "disable\n")

                    val userDir = File(rootDir, ImageFs.WINEPREFIX + "/drive_c/users/xuser")
                    val userFiles = userDir.listFiles()
                    if (userFiles != null) {
                        for (userFile in userFiles) {
                            if (FileUtils.isSymlink(userFile)) {
                                val path = userFile.path
                                userFile.delete()
                                File(path).mkdirs()
                            }
                        }
                    }

                    val suffix = wineInfo.fullVersion() + "-" + wineInfo.arch
                    val containerPatternFile = File(installedWineDir, "/preinstall/container-pattern-$suffix.tzst")
                    TarCompressorUtils.compress(
                        TarCompressorUtils.Type.ZSTD,
                        File(rootDir, ImageFs.WINEPREFIX),
                        containerPatternFile,
                        Constants.XServer.CONTAINER_PATTERN_COMPRESSION_LEVEL,
                    )

                    if (!containerPatternFile.renameTo(File(installedWineDir, containerPatternFile.name)) ||
                        !(File(wineInfo.path)).renameTo(File(installedWineDir, wineInfo.identifier()))
                    ) {
                        containerPatternFile.delete()
                    }

                    FileUtils.delete(File(installedWineDir, "/preinstall"))

                    // preloaderDialog.closeOnUiThread()
                    // TODO: put away loading modal
                    // AppUtils.restartApplication(context, R.id.main_menu_settings)
                }
            }
        }
        )
}

private fun setupWineSystemFiles(
    context: Context,
    firstTimeBoot: Boolean,
    screenInfo: ScreenInfo,
    xServerState: MutableState<XServerState>,
    // xServerViewModel: XServerViewModel,
    container: Container,
    containerManager: ContainerManager,
    // shortcut: Shortcut?,
    envVars: EnvVars,
    onExtractFileListener: OnExtractFileListener?,
) {
    val imageFs = ImageFs.find(context)
    val appVersion = AppUtils.getVersionCode(context).toString()
    val imgVersion = imageFs.getVersion().toString()
    var containerDataChanged = false

    if (!container.getExtra("appVersion").equals(appVersion) || !container.getExtra("imgVersion").equals(imgVersion)) {
        applyGeneralPatches(context, container, imageFs, xServerState.value.wineInfo, onExtractFileListener)
        container.putExtra("appVersion", appVersion)
        container.putExtra("imgVersion", imgVersion)
        containerDataChanged = true
    }

    // val dxwrapper = this.dxwrapper
    if (xServerState.value.dxwrapper == "dxvk") {
        xServerState.value = xServerState.value.copy(
            dxwrapper = "dxvk-" + xServerState.value.dxwrapperConfig?.get("version"),
        )
    }

    if (xServerState.value.dxwrapper != container.getExtra("dxwrapper")) {
        extractDXWrapperFiles(
            context,
            firstTimeBoot,
            container,
            containerManager,
            xServerState.value.dxwrapper,
            imageFs,
            onExtractFileListener,
        )
        container.putExtra("dxwrapper", xServerState.value.dxwrapper)
        containerDataChanged = true
    }

    if (xServerState.value.dxwrapper == "cnc-ddraw") envVars.put("CNC_DDRAW_CONFIG_FILE", "C:\\ProgramData\\cnc-ddraw\\ddraw.ini")

    // val wincomponents = if (shortcut != null) shortcut.getExtra("wincomponents", container.winComponents) else container.winComponents
    val wincomponents = container.winComponents
    if (!wincomponents.equals(container.getExtra("wincomponents"))) {
        // extractWinComponentFiles(context, firstTimeBoot, imageFs, container, containerManager, shortcut, onExtractFileListener)
        extractWinComponentFiles(context, firstTimeBoot, imageFs, container, containerManager, onExtractFileListener)
        container.putExtra("wincomponents", wincomponents)
        containerDataChanged = true
    }

    val desktopTheme = container.desktopTheme
    if ((desktopTheme + "," + screenInfo) != container.getExtra("desktopTheme")) {
        WineThemeManager.apply(context, WineThemeManager.ThemeInfo(desktopTheme), screenInfo)
        container.putExtra("desktopTheme", desktopTheme + "," + screenInfo)
        containerDataChanged = true
    }

    WineStartMenuCreator.create(context, container)
    WineUtils.createDosdevicesSymlinks(container)

    val startupSelection = container.startupSelection.toString()
    if (startupSelection != container.getExtra("startupSelection")) {
        WineUtils.changeServicesStatus(container, container.startupSelection != Container.STARTUP_SELECTION_NORMAL)
        container.putExtra("startupSelection", startupSelection)
        containerDataChanged = true
    }

    if (containerDataChanged) container.saveData()
}

private fun applyGeneralPatches(
    context: Context,
    container: Container,
    imageFs: ImageFs,
    wineInfo: WineInfo,
    onExtractFileListener: OnExtractFileListener?,
) {
    val rootDir = imageFs.rootDir
    FileUtils.delete(File(rootDir, "/opt/apps"))
    TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, context.assets, "imagefs_patches.tzst", rootDir, onExtractFileListener)
    TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, context.assets, "pulseaudio.tzst", File(context.filesDir, "pulseaudio"))
    WineUtils.applySystemTweaks(context, wineInfo)
    container.putExtra("graphicsDriver", null)
    container.putExtra("desktopTheme", null)
    // SettingsFragment.resetBox86_64Version(context)
}
private fun extractDXWrapperFiles(
    context: Context,
    firstTimeBoot: Boolean,
    container: Container,
    containerManager: ContainerManager,
    dxwrapper: String,
    imageFs: ImageFs,
    onExtractFileListener: OnExtractFileListener?,
) {
    val dlls = arrayOf(
        "d3d10.dll",
        "d3d10_1.dll",
        "d3d10core.dll",
        "d3d11.dll",
        "d3d12.dll",
        "d3d12core.dll",
        "d3d8.dll",
        "d3d9.dll",
        "dxgi.dll",
        "ddraw.dll",
    )
    if (firstTimeBoot && dxwrapper != "vkd3d") cloneOriginalDllFiles(imageFs, *dlls)
    val rootDir = imageFs.rootDir
    val windowsDir = File(rootDir, ImageFs.WINEPREFIX + "/drive_c/windows")

    when (dxwrapper) {
        "wined3d" -> {
            restoreOriginalDllFiles(container, containerManager, imageFs, *dlls)
        }
        "cnc-ddraw" -> {
            restoreOriginalDllFiles(container, containerManager, imageFs, *dlls)
            val assetDir = "dxwrapper/cnc-ddraw-" + DefaultVersion.CNC_DDRAW
            val configFile = File(rootDir, ImageFs.WINEPREFIX + "/drive_c/ProgramData/cnc-ddraw/ddraw.ini")
            if (!configFile.isFile) FileUtils.copy(context, "$assetDir/ddraw.ini", configFile)
            val shadersDir = File(rootDir, ImageFs.WINEPREFIX + "/drive_c/ProgramData/cnc-ddraw/Shaders")
            FileUtils.delete(shadersDir)
            FileUtils.copy(context, "$assetDir/Shaders", shadersDir)
            TarCompressorUtils.extract(
                TarCompressorUtils.Type.ZSTD, context.assets,
                "$assetDir/ddraw.tzst", windowsDir, onExtractFileListener,
            )
        }
        "vkd3d" -> {
            val dxvkVersions = context.resources.getStringArray(R.array.dxvk_version_entries)
            TarCompressorUtils.extract(
                TarCompressorUtils.Type.ZSTD, context.assets,
                "dxwrapper/dxvk-" + (dxvkVersions[dxvkVersions.size - 1]) + ".tzst", windowsDir, onExtractFileListener,
            )
            TarCompressorUtils.extract(
                TarCompressorUtils.Type.ZSTD,
                context.assets,
                "dxwrapper/vkd3d-" + DefaultVersion.VKD3D + ".tzst",
                windowsDir,
                onExtractFileListener,
            )
        }
        else -> {
            restoreOriginalDllFiles(container, containerManager, imageFs, "d3d12.dll", "d3d12core.dll", "ddraw.dll")
            TarCompressorUtils.extract(
                TarCompressorUtils.Type.ZSTD, context.assets,
                "dxwrapper/$dxwrapper.tzst", windowsDir, onExtractFileListener,
            )
            TarCompressorUtils.extract(
                TarCompressorUtils.Type.ZSTD,
                context.assets,
                "dxwrapper/d8vk-${DefaultVersion.D8VK}.tzst",
                windowsDir,
                onExtractFileListener,
            )
        }
    }
}
private fun cloneOriginalDllFiles(imageFs: ImageFs, vararg dlls: String) {
    val rootDir = imageFs.rootDir
    val cacheDir = File(rootDir, ImageFs.CACHE_PATH + "/original_dlls")
    if (!cacheDir.isDirectory) cacheDir.mkdirs()
    val windowsDir = File(rootDir, ImageFs.WINEPREFIX + "/drive_c/windows")
    val dirnames = arrayOf("system32", "syswow64")

    for (dll in dlls) {
        for (dirname in dirnames) {
            val dllFile = File(windowsDir, "$dirname/$dll")
            if (dllFile.isFile) FileUtils.copy(dllFile, File(cacheDir, "$dirname/$dll"))
        }
    }
}
private fun restoreOriginalDllFiles(
    container: Container,
    containerManager: ContainerManager,
    imageFs: ImageFs,
    vararg dlls: String,
) {
    val rootDir = imageFs.rootDir
    val cacheDir = File(rootDir, ImageFs.CACHE_PATH + "/original_dlls")
    if (cacheDir.isDirectory) {
        val windowsDir = File(rootDir, ImageFs.WINEPREFIX + "/drive_c/windows")
        val dirnames = cacheDir.list()
        var filesCopied = 0

        for (dll in dlls) {
            var success = false
            for (dirname in dirnames!!) {
                val srcFile = File(cacheDir, "$dirname/$dll")
                val dstFile = File(windowsDir, "$dirname/$dll")
                if (FileUtils.copy(srcFile, dstFile)) success = true
            }
            if (success) filesCopied++
        }

        if (filesCopied == dlls.size) return
    }

    containerManager.extractContainerPatternFile(
        container.wineVersion, container.rootDir,
        object : OnExtractFileListener {
            override fun onExtractFile(file: File, size: Long): File? {
                val path = file.path
                if (path.contains("system32/") || path.contains("syswow64/")) {
                    for (dll in dlls) {
                        if (path.endsWith("system32/$dll") || path.endsWith("syswow64/$dll")) return file
                    }
                }
                return null
            }
        },
    )

    cloneOriginalDllFiles(imageFs, *dlls)
}
private fun extractWinComponentFiles(
    context: Context,
    firstTimeBoot: Boolean,
    imageFs: ImageFs,
    container: Container,
    containerManager: ContainerManager,
    // shortcut: Shortcut?,
    onExtractFileListener: OnExtractFileListener?,
) {
    val rootDir = imageFs.rootDir
    val windowsDir = File(rootDir, ImageFs.WINEPREFIX + "/drive_c/windows")
    val systemRegFile = File(rootDir, ImageFs.WINEPREFIX + "/system.reg")

    try {
        val wincomponentsJSONObject = JSONObject(FileUtils.readString(context, "wincomponents/wincomponents.json"))
        val dlls = mutableListOf<String>()
        // val wincomponents = if (shortcut != null) shortcut.getExtra("wincomponents", container.winComponents) else container.winComponents
        val wincomponents = container.winComponents

        if (firstTimeBoot) {
            for (wincomponent in KeyValueSet(wincomponents)) {
                val dlnames = wincomponentsJSONObject.getJSONArray(wincomponent[0])
                for (i in 0 until dlnames.length()) {
                    val dlname = dlnames.getString(i)
                    dlls.add(if (!dlname.endsWith(".exe")) "$dlname.dll" else dlname)
                }
            }

            cloneOriginalDllFiles(imageFs, *dlls.toTypedArray())
            dlls.clear()
        }

        val oldWinComponentsIter = KeyValueSet(container.getExtra("wincomponents", Container.FALLBACK_WINCOMPONENTS)).iterator()

        for (wincomponent in KeyValueSet(wincomponents)) {
            if (wincomponent[1].equals(oldWinComponentsIter.next()[1])) continue
            val identifier = wincomponent[0]
            val useNative = wincomponent[1].equals("1")

            if (useNative) {
                TarCompressorUtils.extract(
                    TarCompressorUtils.Type.ZSTD, context.assets,
                    "wincomponents/$identifier.tzst", windowsDir, onExtractFileListener,
                )
            } else {
                val dlnames = wincomponentsJSONObject.getJSONArray(identifier)
                for (i in 0 until dlnames.length()) {
                    val dlname = dlnames.getString(i)
                    dlls.add(if (!dlname.endsWith(".exe")) "$dlname.dll" else dlname)
                }
            }

            WineUtils.setWinComponentRegistryKeys(systemRegFile, identifier, useNative)
        }

        if (!dlls.isEmpty()) restoreOriginalDllFiles(container, containerManager, imageFs, *dlls.toTypedArray())
        WineUtils.overrideWinComponentDlls(context, container, wincomponents)
    } catch (e: JSONException) {
        Timber.e("Failed to read JSON: $e")
    }
}

private fun extractGraphicsDriverFiles(
    context: Context,
    graphicsDriver: String,
    dxwrapper: String,
    dxwrapperConfig: KeyValueSet,
    container: Container,
    envVars: EnvVars,
) {
    var cacheId = graphicsDriver
    if (graphicsDriver == "turnip") {
        cacheId += "-" + DefaultVersion.TURNIP + "-" + DefaultVersion.ZINK
    } else if (graphicsDriver == "virgl") {
        cacheId += "-" + DefaultVersion.VIRGL
    }

    val changed = cacheId != container.getExtra("graphicsDriver")
    val imageFs = ImageFs.find(context)
    val rootDir = imageFs.rootDir

    if (changed) {
        FileUtils.delete(File(imageFs.lib32Dir, "libvulkan_freedreno.so"))
        FileUtils.delete(File(imageFs.lib64Dir, "libvulkan_freedreno.so"))
        FileUtils.delete(File(imageFs.lib32Dir, "libGL.so.1.7.0"))
        FileUtils.delete(File(imageFs.lib64Dir, "libGL.so.1.7.0"))
        container.putExtra("graphicsDriver", cacheId)
        container.saveData()
    }

    if (graphicsDriver == "turnip") {
        if (dxwrapper == "dxvk") {
            DXVKHelper.setEnvVars(context, dxwrapperConfig, envVars)
        } else if (dxwrapper == "vkd3d") {
            envVars.put("VKD3D_FEATURE_LEVEL", "12_1")
        }

        envVars.put("GALLIUM_DRIVER", "zink")
        envVars.put("TU_OVERRIDE_HEAP_SIZE", "4096")
        if (!envVars.has("MESA_VK_WSI_PRESENT_MODE")) envVars.put("MESA_VK_WSI_PRESENT_MODE", "mailbox")
        envVars.put("vblank_mode", "0")

        if (!GPUInformation.isAdreno6xx(context)) {
            val userEnvVars = EnvVars(container.envVars)
            val tuDebug = userEnvVars.get("TU_DEBUG")
            if (!tuDebug.contains("sysmem")) userEnvVars.put("TU_DEBUG", (if (!tuDebug.isEmpty()) "$tuDebug," else "") + "sysmem")
            container.envVars = userEnvVars.toString()
        }

        val useDRI3 = PrefManager.getBoolean("use_dri3", true)
        if (!useDRI3) {
            envVars.put("MESA_VK_WSI_PRESENT_MODE", "immediate")
            envVars.put("MESA_VK_WSI_DEBUG", "sw")
        }

        if (changed) {
            TarCompressorUtils.extract(
                TarCompressorUtils.Type.ZSTD,
                context.assets,
                "graphics_driver/turnip-${DefaultVersion.TURNIP}.tzst",
                rootDir,
            )
            TarCompressorUtils.extract(
                TarCompressorUtils.Type.ZSTD,
                context.assets,
                "graphics_driver/zink-${DefaultVersion.ZINK}.tzst",
                rootDir,
            )
        }
    } else if (graphicsDriver == "virgl") {
        envVars.put("GALLIUM_DRIVER", "virpipe")
        envVars.put("VIRGL_NO_READBACK", "true")
        envVars.put("VIRGL_SERVER_PATH", UnixSocketConfig.VIRGL_SERVER_PATH)
        envVars.put("MESA_EXTENSION_OVERRIDE", "-GL_EXT_vertex_array_bgra")
        envVars.put("MESA_GL_VERSION_OVERRIDE", "3.1")
        envVars.put("vblank_mode", "0")
        if (changed) {
            TarCompressorUtils.extract(
                TarCompressorUtils.Type.ZSTD, context.assets,
                "graphics_driver/virgl-" + DefaultVersion.VIRGL + ".tzst", rootDir,
            )
        }
    }
}
private fun changeWineAudioDriver(audioDriver: String, container: Container, imageFs: ImageFs) {
    if (audioDriver != container.getExtra("audioDriver")) {
        val rootDir = imageFs.rootDir
        val userRegFile = File(rootDir, ImageFs.WINEPREFIX + "/user.reg")
        WineRegistryEditor(userRegFile).use { registryEditor ->
            if (audioDriver == "alsa") {
                registryEditor.setStringValue("Software\\Wine\\Drivers", "Audio", "alsa")
            } else if (audioDriver == "pulseaudio") {
                registryEditor.setStringValue("Software\\Wine\\Drivers", "Audio", "pulse")
            }
        }
        container.putExtra("audioDriver", audioDriver)
        container.saveData()
    }
}
