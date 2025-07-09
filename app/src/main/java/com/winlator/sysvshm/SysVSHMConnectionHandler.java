package com.winlator.sysvshm;

import com.winlator.xconnector.ConnectedClient;
import com.winlator.xconnector.ConnectionHandler;

public class SysVSHMConnectionHandler implements ConnectionHandler {
    private final SysVSharedMemory sysVSharedMemory;

    public SysVSHMConnectionHandler(SysVSharedMemory sysVSharedMemory) {
        this.sysVSharedMemory = sysVSharedMemory;
    }

    @Override
    public void handleNewConnection(ConnectedClient client) {
        client.setTag(this.sysVSharedMemory);
    }

    @Override
    public void handleConnectionShutdown(ConnectedClient client) {
    }
}
