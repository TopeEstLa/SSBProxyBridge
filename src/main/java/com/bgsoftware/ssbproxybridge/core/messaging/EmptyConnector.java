package com.bgsoftware.ssbproxybridge.core.messaging;

public class EmptyConnector implements IConnector {

    private static final EmptyConnector INSTANCE = new EmptyConnector();

    public static EmptyConnector getInstance() {
        return INSTANCE;
    }

    private EmptyConnector() {

    }

    @Override
    public void connect() {
        // Do nothing.
    }

    @Override
    public void shutdown() {
        // Do nothing.
    }

    @Override
    public void registerListener(String channel, IListener listener) {
        // Do nothing.
    }

    @Override
    public void unregisterListener(String channel, IListener listener) {
        // Do nothing.
    }

    @Override
    public void sendData(String channel, String data) {
        // Do nothing.
    }

}
