package com.bgsoftware.ssbproxybridge.core.connector;

public interface IConnector<Args extends IConnectionArguments> {

    void connect(Args args) throws ConnectionFailureException;

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
