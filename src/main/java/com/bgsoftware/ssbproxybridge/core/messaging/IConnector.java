package com.bgsoftware.ssbproxybridge.core.messaging;

public interface IConnector {

    void connect(String host, int port, String password) throws ConnectionFailureException;

    void shutdown();

    boolean registerListener(String channel, IListener listener);

    boolean unregisterListener(String channel, IListener listener);

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
