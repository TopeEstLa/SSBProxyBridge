package com.bgsoftware.ssbproxybridge.core.connector;

import java.util.function.Consumer;

public interface IConnector<Args extends IConnectionArguments> {

    void connect(Args args) throws ConnectionFailureException;

    void shutdown();

    boolean registerListener(String channel, IListener listener);

    boolean unregisterListener(String channel, IListener listener);

    boolean unregisterListeners(String channel);

    void sendData(String channel, String data, Consumer<Throwable> errorCallback);

    default void listenOnce(String name, IOneTimeListener listener) {
        registerListener(name, listener);
    }

    interface IListener {

        void onReceive(String data);

    }

    interface IOneTimeListener extends IListener {
    }

}
