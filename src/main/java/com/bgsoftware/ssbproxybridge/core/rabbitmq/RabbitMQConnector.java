package com.bgsoftware.ssbproxybridge.core.rabbitmq;

import com.bgsoftware.ssbproxybridge.core.Singleton;
import com.bgsoftware.ssbproxybridge.core.connector.ConnectionFailureException;
import com.bgsoftware.ssbproxybridge.core.connector.IConnector;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class RabbitMQConnector implements IConnector<RabbitMQConnectionArguments> {

    private static final Singleton<RabbitMQConnector> SINGLETON = new Singleton<RabbitMQConnector>() {
        @Override
        protected RabbitMQConnector create() {
            return new RabbitMQConnector();
        }
    };

    private static final Logger logger = Logger.getLogger("SSBProxyBridge");

    private final Map<String, List<String>> QUEUES = new HashMap<>();

    private Connection connection;
    private Channel channel;

    public static RabbitMQConnector getConnector() {
        return SINGLETON.get();
    }

    private RabbitMQConnector() {

    }

    @Override
    public void connect(RabbitMQConnectionArguments args) throws ConnectionFailureException {
        logger.info("Connecting to RabbitMQ (" + args.getHost() + ":" + args.getPort() + ")");

        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost(args.getHost());
        factory.setPort(args.getPort());
        factory.setVirtualHost(args.getVirtualHost());

        if (!args.getUsername().isEmpty()) {
            factory.setUsername(args.getUsername());
            factory.setPassword(args.getPassword());
        }

        try {
            this.connection = factory.newConnection();
            this.channel = this.connection.createChannel();
        } catch (Throwable error) {
            throw new ConnectionFailureException(error);
        }
    }

    @Override
    public void shutdown() {
        try {
            this.channel.close();
            this.connection.close();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    @Override
    public boolean registerListener(String channel, IListener listener) {
        try {
            this.channel.exchangeDeclareNoWait(channel, "fanout", false, false, false, null);
            String queueName = this.channel.queueDeclare().getQueue();

            QUEUES.computeIfAbsent(channel, s -> new LinkedList<>()).add(queueName);

            this.channel.queueBindNoWait(queueName, channel, "", null);
            this.channel.basicConsume(queueName, true, (consumerTag, delivery) -> {
                String data = new String(delivery.getBody(), StandardCharsets.UTF_8);
                listener.onReceive(data);
            }, cancelCallback -> {
            });
        } catch (IOException error) {
            error.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean unregisterListener(String channel, IListener listener) {
        return this.unregisterListeners(channel);
    }

    @Override
    public boolean unregisterListeners(String channel) {
        try {
            List<String> queues = QUEUES.remove(channel);
            if (queues != null) {
                for (String queueName : queues)
                    this.channel.queueUnbind(queueName, channel, "");
            }

            this.channel.exchangeDeleteNoWait(channel, true);
        } catch (IOException error) {
            error.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public void sendData(String channel, String data, Consumer<Throwable> errorCallback) {
        try {
            this.channel.basicPublish(channel, "", null, data.getBytes(StandardCharsets.UTF_8));
        } catch (IOException error) {
            errorCallback.accept(error);
        }
    }

}
