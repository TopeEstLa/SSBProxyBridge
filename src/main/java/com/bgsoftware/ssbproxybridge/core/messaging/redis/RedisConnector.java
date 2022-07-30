package com.bgsoftware.ssbproxybridge.core.messaging.redis;

import com.bgsoftware.ssbproxybridge.core.messaging.ConnectionFailureException;
import com.bgsoftware.ssbproxybridge.core.messaging.IConnector;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RedisConnector implements IConnector {

    private static final Logger logger = Logger.getLogger("SSBProxyBridge");

    private final Map<String, List<IListener>> listeners = new HashMap<>();

    private final String host;
    private final int port;
    private final String password;

    private RedisClient redisClient;
    private RedisPubSubCommands<String, String> subCommands;
    private RedisPubSubCommands<String, String> pubCommands;
    private StatefulRedisPubSubConnection<String, String> subConnection;
    private StatefulRedisPubSubConnection<String, String> pubConnection;

    public RedisConnector(String host, int port, String password) {
        this.host = host;
        this.port = port;
        this.password = password;
    }

    @Override
    public void connect() throws ConnectionFailureException {
        logger.info("Connecting to Redis (" + this.host + ":" + this.port + ")");

        RedisURI.Builder builder = RedisURI.Builder.redis(this.host, this.port);
        if (!this.password.isEmpty())
            builder.withPassword(this.password.toCharArray());

        try {
            this.redisClient = RedisClient.create(builder.build());
        } catch (Throwable error) {
            throw new ConnectionFailureException(error);
        }

        this.subConnection = this.redisClient.connectPubSub();
        this.pubConnection = this.redisClient.connectPubSub();

        this.subConnection.addListener(new RedisPubSubAdapter<String, String>() {
            @Override
            public void message(String channel, String data) {
                notifyListeners(channel, data);
            }
        });

        this.subCommands = this.subConnection.sync();
        this.pubCommands = this.pubConnection.sync();
    }

    @Override
    public void shutdown() {
        this.pubConnection.close();
        this.subConnection.close();
        this.redisClient.shutdown();
    }

    @Override
    public void registerListener(String channel, IListener listener) {
        listeners.computeIfAbsent(channel, ch -> new LinkedList<>()).add(listener);
        subCommands.subscribe(channel);
    }

    @Override
    public void sendData(String channel, String data) {
        pubCommands.publish(channel, data);
    }

    private void notifyListeners(String channel, String data) {
        List<IListener> listeners = this.listeners.get(channel);
        if (listeners != null)
            listeners.forEach(listener -> listener.onReceive(data));
    }

}
