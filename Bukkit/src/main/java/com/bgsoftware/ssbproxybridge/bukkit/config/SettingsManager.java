package com.bgsoftware.ssbproxybridge.bukkit.config;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;

import java.io.File;
import java.io.IOException;

public class SettingsManager {

    public final String serverName;
    public final String spawnServerName;

    public final String messagingServiceType;
    public final String messagingServiceHost;
    public final int messagingServicePort;
    public final String messagingServicePassword;

    public SettingsManager(SSBProxyBridgeModule module) {
        File configFile = new File(module.getModuleFolder(), "config.yml");

        if (!configFile.exists())
            module.saveResource("config.yml");

        CommentedConfiguration config = CommentedConfiguration.loadConfiguration(configFile);

        try {
            config.syncWithConfig(configFile, module.getResource("config.yml"));
        } catch (IOException error) {
            error.printStackTrace();
        }

        this.serverName = config.getString("server", "");
        this.spawnServerName = config.getString("spawn-server-name", "");

        this.messagingServiceType = config.getString("messaging-service.type", "redis");
        this.messagingServiceHost = config.getString("messaging-service.host", "localhost");
        this.messagingServicePort = config.getInt("messaging-service.port", 6379);
        this.messagingServicePassword = config.getString("messaging-service.password", "");
    }

}
