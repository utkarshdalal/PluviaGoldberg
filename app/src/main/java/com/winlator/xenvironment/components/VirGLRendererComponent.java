package com.winlator.xenvironment.components;

import android.util.Log;

import androidx.annotation.Keep;

import com.winlator.renderer.GLRenderer;
import com.winlator.renderer.Texture;
import com.winlator.xconnector.ConnectedClient;
import com.winlator.xconnector.ConnectionHandler;
import com.winlator.xconnector.RequestHandler;
import com.winlator.xconnector.UnixSocketConfig;
import com.winlator.xconnector.XConnectorEpoll;
import com.winlator.xenvironment.EnvironmentComponent;
import com.winlator.xserver.Drawable;
import com.winlator.xserver.XServer;

import java.io.IOException;

public class VirGLRendererComponent extends EnvironmentComponent implements ConnectionHandler, RequestHandler {
    private XConnectorEpoll connector;
    private long sharedEGLContextPtr;
    private final UnixSocketConfig socketConfig;
    private final XServer xServer;

    private native void destroyClient(long j);

    private native long getCurrentEGLContextPtr();

    private native long handleNewConnection(int i);

    private native void handleRequest(long j);

    static {
        System.loadLibrary("virglrenderer");
    }

    public VirGLRendererComponent(XServer xServer, UnixSocketConfig socketConfig) {
        this.xServer = xServer;
        this.socketConfig = socketConfig;
    }

    @Override
    public void start() {
        Log.d("VirGLRendererComponent", "Starting...");
        if (this.connector != null) return;
        XConnectorEpoll xConnectorEpoll = new XConnectorEpoll(this.socketConfig, this, this);
        this.connector = xConnectorEpoll;
        xConnectorEpoll.setInitialInputBufferCapacity(0);
        this.connector.setInitialOutputBufferCapacity(0);
        this.connector.start();
    }

    @Override
    public void stop() {
        Log.d("VirGLRendererComponent", "Stopping...");
        XConnectorEpoll xConnectorEpoll = this.connector;
        if (xConnectorEpoll != null) {
            xConnectorEpoll.destroy();
            this.connector = null;
        }
    }

    @Keep
    private void killConnection(int fd) {
        XConnectorEpoll xConnectorEpoll = this.connector;
        xConnectorEpoll.killConnection(xConnectorEpoll.getClientWidthFd(fd));
    }

    @Keep
    private long getSharedEGLContext() {
        Log.d("VirGLRendererComponent", "Calling getSharedEGLContext");
        long j = this.sharedEGLContextPtr;
        if (j != 0) {
            return j;
        }
        final Thread thread = Thread.currentThread();
        try {
            GLRenderer renderer = this.xServer.getRenderer();
            renderer.xServerView.queueEvent(() -> {
                sharedEGLContextPtr = getCurrentEGLContextPtr();

                synchronized(thread) {
                    thread.notify();
                }
            });
            synchronized (thread) {
                thread.wait();
            }
        }
        catch (Exception e) {
            return 0;
        }
        Log.d("VirGLRendererComponent", "Finished getSharedEGLContext");
        return sharedEGLContextPtr;
    }

    @Override
    public void handleConnectionShutdown(ConnectedClient client) {
        long clientPtr = ((Long) client.getTag()).longValue();
        destroyClient(clientPtr);
    }

    @Override
    public void handleNewConnection(ConnectedClient client) {
        Log.d("VirGLRendererComponent", "Calling handleNewConnection");
        getSharedEGLContext();
        long clientPtr = handleNewConnection(client.fd);
        client.setTag(Long.valueOf(clientPtr));
        Log.d("VirGLRendererComponent", "Finished handleNewConnection");
    }

    @Override
    public boolean handleRequest(ConnectedClient client) throws IOException {
        Log.d("VirGLRendererComponent", "Calling handleRequest");
        long clientPtr = ((Long) client.getTag()).longValue();
        handleRequest(clientPtr);
        Log.d("VirGLRendererComponent", "Finished handleRequest");
        return true;
    }

    @Keep
    private void flushFrontbuffer(int drawableId, int framebuffer) {
        Log.d("VirGLRendererComponent", "Calling flushFrontbuffer");
        Drawable drawable = this.xServer.drawableManager.getDrawable(drawableId);
        if (drawable == null) {
            return;
        }
        synchronized (drawable.renderLock) {
            drawable.setData(null);
            Texture texture = drawable.getTexture();
            texture.copyFromFramebuffer(framebuffer, drawable.width, drawable.height);
        }
        Runnable onDrawListener = drawable.getOnDrawListener();
        if (onDrawListener != null) {
            onDrawListener.run();
        }
        Log.d("VirGLRendererComponent", "Finished flushFrontbuffer");
    }
}
