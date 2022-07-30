package com.bgsoftware.ssbproxybridge.core.messaging;

public interface IConnector {

    void connect() throws ConnectionFailureException;

    void shutdown();

    void registerListener(String channel, IListener listener);

    void unregisterListener(String channel, IListener listener);

    void sendData(String channel, String data);

    default void listenOnce(String channel, IListener listener) {
        registerListener(channel, new IListener() {
            @Override
            public void onReceive(String data) {
                try {
                    listener.onReceive(data);
                } finally {
                    unregisterListener(channel, this);
                }
            }
        });
    }

    interface IListener {

        void onReceive(String data);

    }

}
