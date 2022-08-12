package com.bgsoftware.ssbproxybridge.core.rabbitmq;

import com.bgsoftware.ssbproxybridge.core.connector.IConnectionArguments;

public class RabbitMQConnectionArguments implements IConnectionArguments {

    private final String host;
    private final int port;
    private final String virtualHost;
    private final String username;
    private final String password;

    public RabbitMQConnectionArguments(String host, int port, String virtualHost, String username, String password) {
        this.host = host;
        this.port = port;
        this.virtualHost = virtualHost;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getVirtualHost() {
        return this.virtualHost;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

}
