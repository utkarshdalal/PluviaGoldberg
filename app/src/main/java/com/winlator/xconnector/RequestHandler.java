package com.winlator.xconnector;

import java.io.IOException;

public interface RequestHandler {
    boolean handleRequest(ConnectedClient connectedClient) throws IOException;
}
