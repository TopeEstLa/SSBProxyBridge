package com.bgsoftware.ssbproxybridge.core.messaging.redis;

import com.bgsoftware.ssbproxybridge.core.Singleton;
import com.bgsoftware.ssbproxybridge.core.messaging.ConnectionFailureException;
import com.bgsoftware.ssbproxybridge.core.messaging.ConnectorAbstract;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

import java.util.logging.Logger;

public class RedisConnector extends ConnectorAbstract {

    private static final Singleton<RedisConnector> SINGLETON = new Singleton<RedisConnector>() {
        @Override
        protected RedisConnector create() {
            return new RedisConnector();
        }
    };

    private static final Logger logger = Logger.getLogger("SSBProxyBridge");

    private RedisClient redisClient;
    private RedisPubSubCommands<String, String> subCommands;
    private RedisPubSubCommands<String, String> pubCommands;
    private StatefulRedisPubSubConnection<String, String> subConnection;
    private StatefulRedisPubSubConnection<String, String> pubConnection;

    public static RedisConnector getConnector() {
        return SINGLETON.get();
    }

    private RedisConnector() {

    }

    @Override
    public void connect(String host, int port, String password) throws ConnectionFailureException {
        logger.info("Connecting to Redis (" + host + ":" + port + ")");

        RedisURI.Builder builder = RedisURI.Builder.redis(host, port);
        if (!password.isEmpty())
            builder.withPassword(password.toCharArray());

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
    public boolean registerListener(String channel, IListener listener) {
        boolean res = super.registerListener(channel, listener);
        if (res) {
            subCommands.subscribe(channel);
        }
        return res;
    }

    @Override
    public boolean unregisterListener(String channel, IListener listener) {
        boolean res = super.unregisterListener(channel, listener);
        if (res) {
            subCommands.unsubscribe(channel);
        }
        return res;
    }

    @Override
    public void sendData(String channel, String data) {
        pubCommands.publish(channel, data);
    }

}
