package com.bgsoftware.ssbproxybridge.bukkit.config;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;

import java.io.File;
import java.io.IOException;

public class SettingsManager {

    public final String serverName;
    public final String spawnServerName;

    public final String messagingServiceType;
    public final String messagingServiceDataChannelName;
    public final String messagingServiceActionsChannelName;

    public final String messagingServiceRedisHost;
    public final int messagingServiceRedisPort;
    public final String messagingServiceRedisUsername;
    public final String messagingServiceRedisPassword;

    public final String messagingServiceRabbitMQHost;
    public final int messagingServiceRabbitMQPort;
    public final String messagingServiceRabbitMQVirtualHost;
    public final String messagingServiceRabbitMQUsername;
    public final String messagingServiceRabbitMQPassword;

    public final String managerType;
    public final String managerFallbackServer;
    public final String managerRestUrl;
    public final String managerRestSecret;

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
        this.messagingServiceDataChannelName = config.getString("messaging-service.channels.data", "ssbproxybridge-data");
        this.messagingServiceActionsChannelName = config.getString("messaging-service.channels.actions", "ssbproxybridge-actions");

        this.messagingServiceRedisHost = config.getString("messaging-service.redis.host", "localhost");
        this.messagingServiceRedisPort = config.getInt("messaging-service.redis.port", 6379);
        this.messagingServiceRedisUsername = config.getString("messaging-service.redis.username", "");
        this.messagingServiceRedisPassword = config.getString("messaging-service.redis.password", "");

        this.messagingServiceRabbitMQHost = config.getString("messaging-service.rabbitmq.host", "localhost");
        this.messagingServiceRabbitMQPort = config.getInt("messaging-service.rabbitmq.port", 5672);
        this.messagingServiceRabbitMQVirtualHost = config.getString("messaging-service.rabbitmq.virtual-host", "/");
        this.messagingServiceRabbitMQUsername = config.getString("messaging-service.rabbitmq.username", "guest");
        this.messagingServiceRabbitMQPassword = config.getString("messaging-service.rabbitmq.password", "guest");

        this.managerType = config.getString("manager.type", "rest");
        this.managerFallbackServer = config.getString("manager.fallback-server", "");
        this.managerRestUrl = config.getString("manager.rest.url", "http://localhost:8080/");
        this.managerRestSecret = config.getString("manager.rest.secret", "");
    }

}
