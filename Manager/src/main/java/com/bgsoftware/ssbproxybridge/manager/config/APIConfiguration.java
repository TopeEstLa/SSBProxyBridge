package com.bgsoftware.ssbproxybridge.manager.config;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class APIConfiguration {

    public final List<String> excludedServers;
    public final long keepAlive;

    public APIConfiguration() {
        this.excludedServers = Arrays.asList("spawn");
        this.keepAlive = TimeUnit.MINUTES.toMillis(1);
    }

}
