package com.bgsoftware.ssbproxybridge.bukkit.listener;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.bridge.ProxyDatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.data.DatabaseFilter;
import com.bgsoftware.superiorskyblock.api.events.IslandCoopPlayerEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandInviteEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandUncoopPlayerEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class IslandsListener implements Listener {

    private final SSBProxyBridgeModule module;

    public IslandsListener(SSBProxyBridgeModule module) {
        this.module = module;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandInvite(IslandInviteEvent event) {
        syncCustomData(event.getIsland(), new Pair<>("invite_player", event.getTarget().getUniqueId().toString()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandCoopPlayer(IslandCoopPlayerEvent event) {
        syncCustomData(event.getIsland(), new Pair<>("coop_player", event.getTarget().getUniqueId().toString()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandUncoopPlayer(IslandUncoopPlayerEvent event) {
        syncCustomData(event.getIsland(), new Pair<>("uncoop_player", event.getTarget().getUniqueId().toString()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandDisband(IslandDisbandEvent event) {
        module.getManager().deleteIsland(event.getIsland().getUniqueId());
    }

    private static void syncCustomData(Island island, Pair<String, Object> data) {
        DatabaseBridge databaseBridge = island.getDatabaseBridge();

        if (!(databaseBridge instanceof ProxyDatabaseBridge) || databaseBridge.getDatabaseBridgeMode() != DatabaseBridgeMode.SAVE_DATA)
            return;

        ProxyDatabaseBridge proxyDatabaseBridge = (ProxyDatabaseBridge) databaseBridge;
        proxyDatabaseBridge.customOperation("islands",
                DatabaseFilter.fromFilter("uuid", island.getUniqueId().toString()),
                new Pair[]{data});
    }

}
