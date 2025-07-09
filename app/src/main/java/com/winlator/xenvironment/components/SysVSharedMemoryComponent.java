package com.winlator.xenvironment.components;

import android.util.Log;

import com.winlator.sysvshm.SysVSHMConnectionHandler;
import com.winlator.sysvshm.SysVSHMRequestHandler;
import com.winlator.sysvshm.SysVSharedMemory;
import com.winlator.xconnector.UnixSocketConfig;
import com.winlator.xconnector.XConnectorEpoll;
import com.winlator.xenvironment.EnvironmentComponent;
import com.winlator.xserver.SHMSegmentManager;
import com.winlator.xserver.XServer;

public class SysVSharedMemoryComponent extends EnvironmentComponent {
    private XConnectorEpoll connector;
    public final UnixSocketConfig socketConfig;
    private SysVSharedMemory sysVSharedMemory;
    private final XServer xServer;

    public SysVSharedMemoryComponent(XServer xServer, UnixSocketConfig socketConfig) {
        this.xServer = xServer;
        this.socketConfig = socketConfig;
    }

    @Override
    public void start() {
        Log.d("SysVSharedMemoryComponent", "Starting...");
        if (this.connector != null) return;
        this.sysVSharedMemory = new SysVSharedMemory();
        XConnectorEpoll xConnectorEpoll = new XConnectorEpoll(this.socketConfig, new SysVSHMConnectionHandler(this.sysVSharedMemory), new SysVSHMRequestHandler());
        this.connector = xConnectorEpoll;
        xConnectorEpoll.start();
        this.xServer.setSHMSegmentManager(new SHMSegmentManager(this.sysVSharedMemory));
    }

    @Override
    public void stop() {
        Log.d("SysVSharedMemoryComponent", "Stopping...");
        XConnectorEpoll xConnectorEpoll = this.connector;
        if (xConnectorEpoll != null) {
            xConnectorEpoll.destroy();
            this.connector = null;
        }
        this.sysVSharedMemory.deleteAll();
    }
}
