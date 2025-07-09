package com.winlator.xconnector;

public interface ConnectionHandler {
    void handleConnectionShutdown(ConnectedClient connectedClient);

    void handleNewConnection(ConnectedClient connectedClient);
}
