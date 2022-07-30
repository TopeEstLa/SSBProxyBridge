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

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void registerListener(String channel, IListener listener) {

    }

    @Override
    public void sendData(String channel, String data) {

    }

}
