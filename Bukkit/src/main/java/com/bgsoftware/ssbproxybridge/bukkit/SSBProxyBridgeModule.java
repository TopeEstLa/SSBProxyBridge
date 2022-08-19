package com.bgsoftware.ssbproxybridge.bukkit;

import com.bgsoftware.ssbproxybridge.bukkit.action.ActionsListener;
import com.bgsoftware.ssbproxybridge.bukkit.config.SettingsManager;
import com.bgsoftware.ssbproxybridge.bukkit.database.DatabaseBridgeListener;
import com.bgsoftware.ssbproxybridge.bukkit.database.ProxyDatabaseBridgeFactory;
import com.bgsoftware.ssbproxybridge.bukkit.listener.PlayersListener;
import com.bgsoftware.ssbproxybridge.bukkit.proxy.ProxyPlayerBridge;
import com.bgsoftware.ssbproxybridge.bukkit.teleport.ProxyPlayersFactory;
import com.bgsoftware.ssbproxybridge.core.connector.ConnectionFailureException;
import com.bgsoftware.ssbproxybridge.core.connector.EmptyConnector;
import com.bgsoftware.ssbproxybridge.core.connector.IConnectionArguments;
import com.bgsoftware.ssbproxybridge.core.connector.IConnector;
import com.bgsoftware.ssbproxybridge.core.rabbitmq.RabbitMQConnectionArguments;
import com.bgsoftware.ssbproxybridge.core.rabbitmq.RabbitMQConnector;
import com.bgsoftware.ssbproxybridge.core.redis.RedisConnectionArguments;
import com.bgsoftware.ssbproxybridge.core.redis.RedisConnector;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.modules.ModuleLoadTime;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import org.bukkit.event.Listener;

import javax.annotation.Nullable;
import java.util.Locale;

public class SSBProxyBridgeModule extends PluginModule {

    private static final int API_VERSION = 3;

    private static SSBProxyBridgeModule INSTANCE;

    private SuperiorSkyblock plugin;

    private SettingsManager settingsManager;

    @SuppressWarnings("rawtypes")
    private IConnector messagingConnector = EmptyConnector.getInstance();

    public SSBProxyBridgeModule() {
        super("SSBProxyBridge", "Ome_R");
        INSTANCE = this;
    }

    @Override
    public void onEnable(SuperiorSkyblock plugin) {
        this.plugin = plugin;

        if (SuperiorSkyblockAPI.getAPIVersion() < API_VERSION)
            throw new RuntimeException("SuperiorSkyblock2 API version is not supported: " + SuperiorSkyblockAPI.getAPIVersion() + " < " + API_VERSION);

        this.settingsManager = new SettingsManager(this);

        // Setup messaging connector so the modules can talk with each other.
        setupMessagingConnector();

        // Setup the custom factories for SuperiorSkyblock2
        setupDatabaseBridgeFactory();
        setupPlayersFactory();

        // Setup outgoing plugin channel for BungeeCord
        // Used to teleport the player, send messages, etc.
        ProxyPlayerBridge.register(plugin);
    }

    @Override
    public void onReload(SuperiorSkyblock plugin) {

    }

    @Override
    public void onDisable(SuperiorSkyblock plugin) {
        this.messagingConnector.unregisterListeners(settingsManager.messagingServiceDataChannelName);
        this.messagingConnector.unregisterListeners(settingsManager.messagingServiceActionsChannelName);
        this.messagingConnector.shutdown();
    }

    @Nullable
    @Override
    public Listener[] getModuleListeners(SuperiorSkyblock plugin) {
        return new Listener[]{new PlayersListener()};
    }

    @Nullable
    @Override
    public SuperiorCommand[] getSuperiorCommands(SuperiorSkyblock plugin) {
        return new SuperiorCommand[0];
    }

    @Nullable
    @Override
    public SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblock plugin) {
        return new SuperiorCommand[0];
    }

    @Override
    public ModuleLoadTime getLoadTime() {
        return ModuleLoadTime.NORMAL;
    }

    public SettingsManager getSettings() {
        return settingsManager;
    }

    public IConnector<?> getMessaging() {
        return this.messagingConnector;
    }

    private void setupMessagingConnector() {
        IConnectionArguments connectionArguments;

        switch (settingsManager.messagingServiceType.toUpperCase(Locale.ENGLISH)) {
            case "REDIS":
                this.messagingConnector = RedisConnector.getConnector();
                connectionArguments = new RedisConnectionArguments(settingsManager.messagingServiceRedisHost,
                        settingsManager.messagingServiceRedisPort, settingsManager.messagingServiceRedisUsername,
                        settingsManager.messagingServiceRedisPassword);
                break;
            case "RABBITMQ":
                this.messagingConnector = RabbitMQConnector.getConnector();
                connectionArguments = new RabbitMQConnectionArguments(settingsManager.messagingServiceRabbitMQHost,
                        settingsManager.messagingServiceRabbitMQPort, settingsManager.messagingServiceRabbitMQVirtualHost,
                        settingsManager.messagingServiceRabbitMQUsername, settingsManager.messagingServiceRabbitMQPassword);
                break;
            default:
                throw new RuntimeException("Invalid connector: " + settingsManager.messagingServiceType);
        }

        try {
            // noinspection unchecked
            this.messagingConnector.connect(connectionArguments);
            this.messagingConnector.registerListener(settingsManager.messagingServiceDataChannelName, new DatabaseBridgeListener(this));
            this.messagingConnector.registerListener(settingsManager.messagingServiceActionsChannelName, new ActionsListener(this));
        } catch (ConnectionFailureException error) {
            getLogger().info("Failed to connect to messaging connector:");
            error.printStackTrace();
        }
    }

    private void setupDatabaseBridgeFactory() {
        plugin.getFactory().registerDatabaseBridgeFactory(ProxyDatabaseBridgeFactory.getInstance());
    }

    private void setupPlayersFactory() {
        plugin.getFactory().registerPlayersFactory(ProxyPlayersFactory.getInstance());
    }

    public static SSBProxyBridgeModule getModule() {
        return INSTANCE;
    }

    public SuperiorSkyblock getPlugin() {
        return plugin;
    }

}
