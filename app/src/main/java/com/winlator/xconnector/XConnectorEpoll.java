package com.winlator.xconnector;
import android.util.Log;

import android.util.SparseArray;
import androidx.annotation.Keep;
import dalvik.annotation.optimization.CriticalNative;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

public class XConnectorEpoll {
    private final ConnectionHandler connectionHandler;
    private long nativePtr;
    private final RequestHandler requestHandler;
    private final ArrayList<ConnectedClient> connectedClients = new ArrayList<>();
    private boolean canReceiveAncillaryMessages = false;
    private boolean multithreadedClients = false;
    private int initialInputBufferCapacity = 64;
    private int initialOutputBufferCapacity = 64;

    private native boolean addFdToEpoll(int i, int i2);

    @CriticalNative
    public static native void closeFd(int i);

    private static native void destroy(long j);

    private static native void killConnection(long j, long j2);

    private native long nativeAllocate(String str);

    private static native void startEpollThread(long j, boolean z);

    private static native void stopEpollThread(long j);

    static {
        System.loadLibrary("winlator");
    }

    public XConnectorEpoll(UnixSocketConfig socketConfig, ConnectionHandler connectionHandler, RequestHandler requestHandler) {
        this.connectionHandler = connectionHandler;
        this.requestHandler = requestHandler;
        long jNativeAllocate = nativeAllocate(socketConfig.path);
        this.nativePtr = jNativeAllocate;
        if (jNativeAllocate == 0) {
            throw new RuntimeException("Failed to allocate XConnectorEpoll.");
        }
    }

    public void start() {
        long j = this.nativePtr;
        if (j != 0) {
            startEpollThread(j, this.multithreadedClients);
        }
    }

    public void destroy() {
        long j = this.nativePtr;
        if (j != 0) {
            stopEpollThread(j);
            destroy(this.nativePtr);
            this.nativePtr = 0L;
        }
    }

    @Keep
    private Object handleNewConnection(long clientPtr, int fd) {
        ConnectedClient client = new ConnectedClient(clientPtr, fd);
        client.createInputStream(this.initialInputBufferCapacity);
        client.createOutputStream(this.initialOutputBufferCapacity);
        this.connectionHandler.handleNewConnection(client);
        synchronized (this.connectedClients) {
            this.connectedClients.add(client);
        }
        return client;
    }

    @Keep
    private void handleExistingConnection(Object tag) {
        ConnectedClient client = (ConnectedClient) tag;
        XInputStream inputStream = client.getInputStream();
        try {
            if (inputStream != null) {
                if (inputStream.readMoreData(this.canReceiveAncillaryMessages) > 0) {
                    int activePosition = 0;
                    while (this.requestHandler.handleRequest(client)) {
                        activePosition = inputStream.getActivePosition();
                    }
                    inputStream.setActivePosition(activePosition);
                    return;
                }
                killConnection(client);
                return;
            }
            this.requestHandler.handleRequest(client);
        } catch (IOException e) {
            killConnection(client);
        }
    }

    @Keep
    private void killAllConnections() {
        while (!this.connectedClients.isEmpty()) {
            killConnection(this.connectedClients.remove(0));
        }
    }

    public ConnectedClient getClientWidthFd(int fd) {
        synchronized (this.connectedClients) {
            Iterator<ConnectedClient> it = this.connectedClients.iterator();
            while (it.hasNext()) {
                ConnectedClient client = it.next();
                if (client.fd == fd) {
                    return client;
                }
            }
            return null;
        }
    }

    public void killConnection(ConnectedClient client) {
        long j = this.nativePtr;
        if (j != 0) {
            killConnection(j, client.nativePtr);
        }
    }

    public void setCanReceiveAncillaryMessages(boolean canReceiveAncillaryMessages) {
        this.canReceiveAncillaryMessages = canReceiveAncillaryMessages;
    }

    public void setInitialInputBufferCapacity(int initialInputBufferCapacity) {
        this.initialInputBufferCapacity = initialInputBufferCapacity;
    }

    public void setInitialOutputBufferCapacity(int initialOutputBufferCapacity) {
        this.initialOutputBufferCapacity = initialOutputBufferCapacity;
    }

    public void setMultithreadedClients(boolean multithreadedClients) {
        this.multithreadedClients = multithreadedClients;
    }
}
