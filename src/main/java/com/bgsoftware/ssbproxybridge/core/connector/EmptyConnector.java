package com.bgsoftware.ssbproxybridge.core.connector;

public class EmptyConnector implements IConnector<EmptyConnectionArguments> {

    private static final EmptyConnector INSTANCE = new EmptyConnector();

    public static EmptyConnector getInstance() {
        return INSTANCE;
    }

    private EmptyConnector() {

    }

    @Override
    public void connect(EmptyConnectionArguments unused) {
        // Do nothing.
    }

    @Override
    public void shutdown() {
        // Do nothing.
    }

    @Override
    public boolean registerListener(String channel, IListener listener) {
        return false;
    }

    @Override
    public boolean unregisterListener(String channel, IListener listener) {
        return false;
    }

    @Override
    public void sendData(String channel, String data) {
        // Do nothing.
    }

}
