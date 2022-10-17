package com.bgsoftware.ssbproxybridge.core.connector;

import com.bgsoftware.ssbproxybridge.core.bundle.Bundle;

import java.util.function.Consumer;

public interface IConnector<Args extends IConnectionArguments> {

    void connect(Args args) throws ConnectionFailureException;

    void shutdown();

    boolean registerListener(String channel, IListener listener);

    boolean unregisterListener(String channel, IListener listener);

    boolean unregisterListeners(String channel);

    void sendBundle(Bundle bundle, Consumer<Throwable> errorCallback);

    default void listenOnce(String name, IOneTimeListener listener) {
        registerListener(name, listener);
    }

    interface IListener {

        void onReceive(Bundle bundle);

    }

    interface IOneTimeListener extends IListener {
    }

}
