package com.winlator.xserver;

import com.winlator.xconnector.ConnectedClient;
import com.winlator.xconnector.ConnectionHandler;

public class XClientConnectionHandler implements ConnectionHandler {
    private final XServer xServer;

    public XClientConnectionHandler(XServer xServer) {
        this.xServer = xServer;
    }

    @Override
    public void handleNewConnection(ConnectedClient client) {
        client.setTag(new XClient(this.xServer, client.getInputStream(), client.getOutputStream()));
    }

    @Override
    public void handleConnectionShutdown(ConnectedClient client) {
        ((XClient) client.getTag()).freeResources();
    }
}
