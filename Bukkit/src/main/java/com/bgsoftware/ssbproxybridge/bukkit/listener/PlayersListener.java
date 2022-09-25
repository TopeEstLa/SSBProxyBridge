package com.bgsoftware.ssbproxybridge.bukkit.listener;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.action.ActionsQueue;
import com.bgsoftware.ssbproxybridge.bukkit.action.ServerActions;
import com.bgsoftware.ssbproxybridge.bukkit.bridge.ProxyDatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.data.DatabaseFilter;
import com.bgsoftware.superiorskyblock.api.events.AttemptPlayerSendMessageEvent;
import com.bgsoftware.superiorskyblock.api.events.PlayerToggleBlocksStackerEvent;
import com.bgsoftware.superiorskyblock.api.events.PlayerToggleBypassEvent;
import com.bgsoftware.superiorskyblock.api.events.PlayerToggleSpyEvent;
import com.bgsoftware.superiorskyblock.api.events.PlayerToggleTeamChatEvent;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayersListener implements Listener {

    private final SSBProxyBridgeModule module;

    public PlayersListener(SSBProxyBridgeModule module) {
        this.module = module;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ActionsQueue.getPlayersQueue().poll(player.getUniqueId(), player, (error, request) -> {
            this.module.getLogger().warning("Received an unexpected error while handling request:");
            this.module.getLogger().warning(request + "");
            error.printStackTrace();
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttemptPlayerSendMessage(AttemptPlayerSendMessageEvent event) {
        Player player = event.getReceiver().asPlayer();

        if (player != null)
            return;

        event.setCancelled(true);

        Object[] args = new Object[event.getArgumentsLength()];
        for (int i = 0; i < args.length; ++i)
            args[i] = event.getArgument(i);

        ServerActions.sendMessage(event.getReceiver().getUniqueId(), event.getMessageType(), args);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onToggleBlocksStacker(PlayerToggleBlocksStackerEvent event) {
        trySyncServers(event.getPlayer(), new Pair<>("blocks_stacker", !event.getPlayer().hasBlocksStackerEnabled()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onToggleTeamChat(PlayerToggleTeamChatEvent event) {
        trySyncServers(event.getPlayer(), new Pair<>("team_chat", !event.getPlayer().hasTeamChatEnabled()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onToggleBypass(PlayerToggleBypassEvent event) {
        trySyncServers(event.getPlayer(), new Pair<>("admin_bypass", !event.getPlayer().hasBypassModeEnabled()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onToggleSpy(PlayerToggleSpyEvent event) {
        trySyncServers(event.getPlayer(), new Pair<>("admin_spy", !event.getPlayer().hasAdminSpyEnabled()));
    }

    private void trySyncServers(SuperiorPlayer superiorPlayer, Pair<String, Object> data) {
        DatabaseBridge databaseBridge = superiorPlayer.getDatabaseBridge();

        if (!(databaseBridge instanceof ProxyDatabaseBridge) || databaseBridge.getDatabaseBridgeMode() != DatabaseBridgeMode.SAVE_DATA)
            return;

        ProxyDatabaseBridge proxyDatabaseBridge = (ProxyDatabaseBridge) databaseBridge;
        proxyDatabaseBridge.customOperation("players",
                DatabaseFilter.fromFilter("uuid", superiorPlayer.getUniqueId().toString()),
                new Pair[]{data});
    }

}
