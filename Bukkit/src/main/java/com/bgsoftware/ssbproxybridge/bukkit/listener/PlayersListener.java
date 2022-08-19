package com.bgsoftware.ssbproxybridge.bukkit.listener;

import com.bgsoftware.ssbproxybridge.bukkit.action.ActionsQueue;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayersListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ActionsQueue.getPlayersQueue().poll(player.getUniqueId(), player);
    }

}
