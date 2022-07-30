package com.bgsoftware.ssbproxybridge.bukkit;

import com.bgsoftware.ssbproxybridge.bukkit.database.DatabaseBridgeListener;
import com.bgsoftware.ssbproxybridge.bukkit.database.ProxyDatabaseBridge;
import com.bgsoftware.ssbproxybridge.bukkit.database.ProxyDatabaseBridgeFactory;
import com.bgsoftware.ssbproxybridge.core.connector.ConnectionFailureException;
import com.bgsoftware.ssbproxybridge.core.connector.EmptyConnector;
import com.bgsoftware.ssbproxybridge.core.connector.IConnector;
import com.bgsoftware.ssbproxybridge.core.redis.RedisConnector;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.modules.ModuleLoadTime;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import org.bukkit.event.Listener;

import javax.annotation.Nullable;

public class SSBProxyBridgeModule extends PluginModule {

    private static SSBProxyBridgeModule INSTANCE;

    private SuperiorSkyblock plugin;

    private IConnector messagingConnector = EmptyConnector.getInstance();

    public SSBProxyBridgeModule() {
        super("SSBProxyBridge", "Ome_R");
        INSTANCE = this;
    }

    @Override
    public void onEnable(SuperiorSkyblock plugin) {
        this.plugin = plugin;
        setupMessagingConnector();
        setupDatabaseBridgeFactory();
    }

    @Override
    public void onReload(SuperiorSkyblock plugin) {

    }

    @Override
    public void onDisable(SuperiorSkyblock plugin) {
        this.messagingConnector.shutdown();
    }

    @Nullable
    @Override
    public Listener[] getModuleListeners(SuperiorSkyblock plugin) {
        return new Listener[0];
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

    public IConnector getMessaging() {
        return this.messagingConnector;
    }

    private void setupMessagingConnector() {
        this.messagingConnector = RedisConnector.getConnector();
        try {
            this.messagingConnector.connect("127.0.0.1", 6379, "");
            this.messagingConnector.registerListener(ProxyDatabaseBridge.CHANNEL_NAME, new DatabaseBridgeListener());
        } catch (ConnectionFailureException error) {
            getLogger().info("Failed to connect to messaging connector:");
            error.printStackTrace();
        }
    }

    private void setupDatabaseBridgeFactory() {
        plugin.getFactory().registerDatabaseBridgeFactory(ProxyDatabaseBridgeFactory.getInstance());
    }

    public static SSBProxyBridgeModule getModule() {
        return INSTANCE;
    }

    public SuperiorSkyblock getPlugin() {
        return plugin;
    }

}
