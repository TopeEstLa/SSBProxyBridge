package com.bgsoftware.ssbproxybridge.manager;

import com.bgsoftware.ssbproxybridge.manager.config.APIConfiguration;
import com.bgsoftware.ssbproxybridge.manager.tracker.ServersTracker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    private static Main INSTANCE;

    private final ServersTracker serversTracker = new ServersTracker();

    private APIConfiguration configuration;

    public Main() {
        INSTANCE = this;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        INSTANCE.configuration = new APIConfiguration();
    }

    public ServersTracker getServersTracker() {
        return serversTracker;
    }

    public APIConfiguration getConfig() {
        return configuration;
    }

    public static Main getInstance() {
        return INSTANCE;
    }

}
