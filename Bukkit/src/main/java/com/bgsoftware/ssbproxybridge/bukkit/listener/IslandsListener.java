package com.bgsoftware.ssbproxybridge.bukkit.listener;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.bridge.ProxyDatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseFilter;
import com.bgsoftware.superiorskyblock.api.events.IslandInviteEvent;
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
        DatabaseBridge databaseBridge = event.getIsland().getDatabaseBridge();

        if (!(databaseBridge instanceof ProxyDatabaseBridge))
            return;

        ProxyDatabaseBridge proxyDatabaseBridge = (ProxyDatabaseBridge) databaseBridge;
        proxyDatabaseBridge.customOperation("islands",
                DatabaseFilter.fromFilter("uuid", event.getIsland().getUniqueId().toString()),
                new Pair[]{new Pair<>("invite_player", event.getTarget().getUniqueId().toString())});
    }

}
