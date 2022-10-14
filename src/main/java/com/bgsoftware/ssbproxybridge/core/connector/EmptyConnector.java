package com.bgsoftware.ssbproxybridge.core.connector;

import java.util.function.Consumer;

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
    public boolean unregisterListeners(String channel) {
        return false;
    }

    @Override
    public void listenOnce(String channel, IOneTimeListener listener) {
        listener.onReceive("");
    }

    @Override
    public void sendData(String channel, String data, Consumer<Throwable> errorCallback) {
        // Do nothing.
    }

}
