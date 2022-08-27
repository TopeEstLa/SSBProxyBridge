package com.bgsoftware.ssbproxybridge.manager.config;

import java.util.concurrent.TimeUnit;

public class APIConfiguration {

    public final String spawnServerName;
    public final long keepAlive;

    public APIConfiguration() {
        this.spawnServerName = "spawn";
        this.keepAlive = TimeUnit.MINUTES.toMillis(1);
    }

}
