package com.bgsoftware.ssbproxybridge.core.redis;

import com.bgsoftware.ssbproxybridge.core.connector.IConnectionArguments;

public class RedisConnectionArguments implements IConnectionArguments {

    private final String host;
    private final int port;
    private final String password;

    public RedisConnectionArguments(String host, int port, String password) {
        this.host = host;
        this.port = port;
        this.password = password;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getPassword() {
        return this.password;
    }

}
