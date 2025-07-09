package com.winlator.xenvironment.components;

import androidx.annotation.Keep;
import com.winlator.contentdialog.VortekConfigDialog;
import com.winlator.core.GPUHelper;
import com.winlator.core.KeyValueSet;
import com.winlator.renderer.GPUImage;
import com.winlator.renderer.Texture;
import com.winlator.widget.XServerView;
import com.winlator.xconnector.ConnectedClient;
import com.winlator.xconnector.ConnectionHandler;
import com.winlator.xconnector.RequestHandler;
import com.winlator.xconnector.UnixSocketConfig;
import com.winlator.xconnector.XConnectorEpoll;
import com.winlator.xconnector.XInputStream;
import com.winlator.xenvironment.EnvironmentComponent;
import com.winlator.xserver.Drawable;
import com.winlator.xserver.Window;
import com.winlator.xserver.XServer;
import java.io.IOException;
import java.util.Objects;

public class VortekRendererComponent extends EnvironmentComponent implements ConnectionHandler, RequestHandler {
    public static final int VK_MAX_VERSION = GPUHelper.vkMakeVersion(1, 3, 128);
    private XConnectorEpoll connector;
    private final Options options;
    private final UnixSocketConfig socketConfig;
    private final XServer xServer;

    private native long createVkContext(int i, Options options);

    private native void destroyVkContext(long j);

    static {
        System.loadLibrary("vortekrenderer");
    }

    public static class Options {
        public int vkMaxVersion = VortekRendererComponent.VK_MAX_VERSION;
        public short maxDeviceMemory = 4096;
        public short imageCacheSize = 256;
        public String[] exposedDeviceExtensions = null;

        public static Options fromKeyValueSet(KeyValueSet config) {
            if (config == null || config.isEmpty()) {
                return new Options();
            }
            Options options = new Options();
            String exposedDeviceExtensions = config.get("exposedDeviceExtensions", "all");
            if (!exposedDeviceExtensions.isEmpty() && !exposedDeviceExtensions.equals("all")) {
                options.exposedDeviceExtensions = exposedDeviceExtensions.split("\\|");
            }
            String str = VortekConfigDialog.DEFAULT_VK_MAX_VERSION;
            String vkMaxVersion = config.get("vkMaxVersion", str);
            if (!vkMaxVersion.equals(str)) {
                String[] parts = vkMaxVersion.split("\\.");
                options.vkMaxVersion = GPUHelper.vkMakeVersion(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), 128);
            }
            options.maxDeviceMemory = (short) config.getInt("maxDeviceMemory", 4096);
            options.imageCacheSize = (short) config.getInt("imageCacheSize", 256);
            return options;
        }
    }

    public VortekRendererComponent(XServer xServer, UnixSocketConfig socketConfig, Options options) {
        this.xServer = xServer;
        this.socketConfig = socketConfig;
        this.options = options;
    }

    @Override // com.winlator.xenvironment.EnvironmentComponent
    public void start() {
        if (this.connector != null) {
            return;
        }
        XConnectorEpoll xConnectorEpoll = new XConnectorEpoll(this.socketConfig, this, this);
        this.connector = xConnectorEpoll;
        xConnectorEpoll.setInitialInputBufferCapacity(1);
        this.connector.setInitialOutputBufferCapacity(0);
        this.connector.start();
    }

    @Override // com.winlator.xenvironment.EnvironmentComponent
    public void stop() {
        XConnectorEpoll xConnectorEpoll = this.connector;
        if (xConnectorEpoll != null) {
            xConnectorEpoll.destroy();
            this.connector = null;
        }
    }

    @Keep
    private int getWindowWidth(int windowId) {
        Window window = this.xServer.windowManager.getWindow(windowId);
        if (window != null) {
            return window.getWidth();
        }
        return 0;
    }

    @Keep
    private int getWindowHeight(int windowId) {
        Window window = this.xServer.windowManager.getWindow(windowId);
        if (window != null) {
            return window.getHeight();
        }
        return 0;
    }

    @Keep
    private long getWindowHardwareBuffer(int windowId) {
        Window window = this.xServer.windowManager.getWindow(windowId);
        if (window != null) {
            Drawable drawable = window.getContent();
            Texture texture = drawable.getTexture();
            if (!(texture instanceof GPUImage)) {
                XServerView xServerView = this.xServer.getRenderer().xServerView;
                Objects.requireNonNull(texture);
                xServerView.queueEvent(() -> VortekRendererComponent.destroyTexture(texture));
                drawable.setTexture(new GPUImage(drawable.width, drawable.height, false, false));
            }
            return ((GPUImage) drawable.getTexture()).getHardwareBufferPtr();
        }
        return 0L;
    }

    @Keep
    private void updateWindowContent(int windowId) {
        Window window = this.xServer.windowManager.getWindow(windowId);
        if (window != null) {
            Drawable drawable = window.getContent();
            synchronized (drawable.renderLock) {
                drawable.forceUpdate();
            }
        }
    }

    @Override // com.winlator.xconnector.ConnectionHandler
    public void handleConnectionShutdown(ConnectedClient client) {
        if (client.getTag() != null) {
            long contextPtr = ((Long) client.getTag()).longValue();
            destroyVkContext(contextPtr);
        }
    }

    @Override // com.winlator.xconnector.ConnectionHandler
    public void handleNewConnection(ConnectedClient client) {
    }

    @Override // com.winlator.xconnector.RequestHandler
    public boolean handleRequest(ConnectedClient client) throws IOException {
        XInputStream inputStream = client.getInputStream();
        if (inputStream.available() < 1) {
            return false;
        }
        byte requestCode = inputStream.readByte();
        if (requestCode == 1) {
            long contextPtr = createVkContext(client.fd, this.options);
            if (contextPtr > 0) {
                client.setTag(Long.valueOf(contextPtr));
            } else {
                this.connector.killConnection(client);
            }
        }
        return true;
    }

    public static void destroyTexture(Texture texture) {
        if (texture != null) {
            texture.destroy();
        }
    }
}
