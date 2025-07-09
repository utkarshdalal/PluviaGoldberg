package com.winlator.alsaserver;

import com.winlator.xconnector.ConnectedClient;
import com.winlator.xconnector.ConnectionHandler;

public class ALSAClientConnectionHandler implements ConnectionHandler {
    private final ALSAClient.Options options;

    public ALSAClientConnectionHandler(ALSAClient.Options options) {
        this.options = options;
    }

    @Override
    public void handleNewConnection(ConnectedClient client) {
        client.setTag(new ALSAClient(this.options));
    }

    @Override
    public void handleConnectionShutdown(ConnectedClient client) throws IllegalStateException {
        if (client.getTag() != null) {
            ((ALSAClient) client.getTag()).release();
        }
    }
}
