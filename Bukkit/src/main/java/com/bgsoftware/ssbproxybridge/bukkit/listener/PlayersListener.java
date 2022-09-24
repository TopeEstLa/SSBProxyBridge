package com.bgsoftware.ssbproxybridge.bukkit.listener;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.action.ActionsQueue;
import com.bgsoftware.ssbproxybridge.bukkit.action.ServerActions;
import com.bgsoftware.superiorskyblock.api.events.AttemptPlayerSendMessageEvent;
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

}
