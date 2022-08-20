package com.bgsoftware.ssbproxybridge.bukkit.database;

import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.factory.DatabaseBridgeFactory;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.handlers.StackedBlocksManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import javax.annotation.Nullable;

public class ProxyDatabaseBridgeFactory implements DatabaseBridgeFactory {

    private static final ProxyDatabaseBridgeFactory INSTANCE = new ProxyDatabaseBridgeFactory();

    public static ProxyDatabaseBridgeFactory getInstance() {
        return INSTANCE;
    }

    private boolean createActivatedBridge = true;

    private ProxyDatabaseBridgeFactory() {

    }

    public void setCreateActivatedBridge(boolean createActivatedBridge) {
        this.createActivatedBridge = createActivatedBridge;
    }

    @Override
    public DatabaseBridge createIslandsDatabaseBridge(@Nullable Island island, DatabaseBridge databaseBridge) {
        return new ProxyDatabaseBridge(databaseBridge, createActivatedBridge);
    }

    @Override
    public DatabaseBridge createPlayersDatabaseBridge(@Nullable SuperiorPlayer superiorPlayer, DatabaseBridge databaseBridge) {
        return new ProxyDatabaseBridge(databaseBridge, createActivatedBridge);
    }

    @Override
    public DatabaseBridge createGridDatabaseBridge(@Nullable GridManager gridManager, DatabaseBridge databaseBridge) {
        return new ProxyDatabaseBridge(databaseBridge, createActivatedBridge);
    }

    @Override
    public DatabaseBridge createStackedBlocksDatabaseBridge(@Nullable StackedBlocksManager stackedBlocksManager, DatabaseBridge databaseBridge) {
        return new ProxyDatabaseBridge(databaseBridge, createActivatedBridge);
    }

}
