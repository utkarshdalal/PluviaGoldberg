package com.winlator.xenvironment.components;

import android.util.Log;
import com.winlator.alsaserver.ALSAClient;
import com.winlator.alsaserver.ALSAClientConnectionHandler;
import com.winlator.alsaserver.ALSARequestHandler;
import com.winlator.xconnector.UnixSocketConfig;
import com.winlator.xconnector.XConnectorEpoll;
import com.winlator.xenvironment.EnvironmentComponent;

public class ALSAServerComponent extends EnvironmentComponent {
    private XConnectorEpoll connector;
    private final ALSAClient.Options options;
    private final UnixSocketConfig socketConfig;

    public ALSAServerComponent(UnixSocketConfig socketConfig, ALSAClient.Options options) {
        this.socketConfig = socketConfig;
        this.options = options;
    }

    @Override // com.winlator.xenvironment.EnvironmentComponent
    public void start() throws NumberFormatException {
        if (this.connector != null) {
            return;
        }
        ALSAClient.assignFramesPerBuffer(this.environment.getContext());
        XConnectorEpoll xConnectorEpoll = new XConnectorEpoll(this.socketConfig, new ALSAClientConnectionHandler(this.options), new ALSARequestHandler());
        this.connector = xConnectorEpoll;
        xConnectorEpoll.setMultithreadedClients(true);
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
}
