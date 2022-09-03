package com.bgsoftware.ssbproxybridge.manager;

import com.bgsoftware.ssbproxybridge.manager.config.APIConfiguration;
import com.bgsoftware.ssbproxybridge.manager.tracker.ServersTracker;

import java.io.IOException;

public class ManagerServer {

    private static final ManagerServer INSTANCE = new ManagerServer();

    private final ServersTracker serversTracker = new ServersTracker(this);
    private APIConfiguration configuration;

    private ManagerServer() {
    }

    public void initialize() throws IOException {
        if (this.configuration != null)
            throw new IllegalStateException("The manager was already been initialized.");

        this.configuration = new APIConfiguration();
    }

    public ServersTracker getServersTracker() {
        return serversTracker;
    }

    public APIConfiguration getConfig() {
        return configuration;
    }

    public static ManagerServer getInstance() {
        return INSTANCE;
    }

}
