package com.bgsoftware.ssbproxybridge.core.messaging;

public interface IConnector {

    void connect() throws ConnectionFailureException;

    void shutdown();

    void registerListener(String channel, IListener listener);

    void sendData(String channel, String data);

    interface IListener {

        void onReceive(String data);

    }

}
