package com.bgsoftware.ssbproxybridge.bukkit.teleport;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.action.ServerActions;
import com.bgsoftware.ssbproxybridge.bukkit.island.RemoteIsland;
import com.bgsoftware.ssbproxybridge.bukkit.proxy.ProxyPlayerBridge;
import com.bgsoftware.ssbproxybridge.bukkit.utils.BukkitExecutor;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class ProxyPlayerTeleportAlgorithm implements PlayerTeleportAlgorithm {

    private static final SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

    private final PlayerTeleportAlgorithm original;
    private boolean hasPendingTeleportTask = false;
    private boolean teleportedToServer = false;

    public ProxyPlayerTeleportAlgorithm(PlayerTeleportAlgorithm original) {
        this.original = original;
    }

    public void setHasPendingTeleportTask(boolean hasPendingTeleportTask) {
        this.hasPendingTeleportTask = hasPendingTeleportTask;
    }

    public boolean hasPendingTeleportTask() {
        return teleportedToServer || hasPendingTeleportTask;
    }

    private void setTeleportedToServer(boolean teleportedToServer) {
        this.teleportedToServer = teleportedToServer;
    }

    private boolean teleportToIsland(Player player, String islandServer, Island island) {
        if (module.getSettings().serverName.equals(islandServer))
            // Teleport regularly to the island.
            return false;

        /* We set the teleportedToServer flag set to true.
        This will prevent messages of joining/leaving the server. */
        setTeleportedToServer(true);
        BukkitExecutor.runTaskLater(() -> setTeleportedToServer(false), 5L);

        ServerActions.teleportToIsland(player, islandServer, island.getUniqueId());
        ProxyPlayerBridge.teleportPlayer(player, islandServer);

        return true;
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Location location) {
        return original.teleport(player, location);
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Island island) {
        String targetServer = island.isSpawn() ? module.getSettings().spawnServerName :
                island instanceof RemoteIsland ? ((RemoteIsland) island).getOriginalServer() : null;

        if (targetServer == null)
            return original.teleport(player, island);

        CompletableFuture<Boolean> result = new CompletableFuture<>();

        if (hasPendingTeleportTask || teleportToIsland(player, targetServer, island)) {
            result.complete(true);
        } else {
            original.teleport(player, island).whenComplete((originalTeleport, originalError) -> {
                if (originalError != null) {
                    result.completeExceptionally(originalError);
                } else {
                    result.complete(originalTeleport);
                }
            });
        }

        return result;
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Island island, World.Environment environment) {
        String targetServer = island.isSpawn() ? module.getSettings().spawnServerName :
                island instanceof RemoteIsland ? ((RemoteIsland) island).getOriginalServer() : null;

        if (targetServer == null)
            return original.teleport(player, island);

        CompletableFuture<Boolean> result = new CompletableFuture<>();

        if (hasPendingTeleportTask || teleportToIsland(player, targetServer, island)) {
            result.complete(true);
        } else {
            original.teleport(player, island).whenComplete((originalTeleport, originalError) -> {
                if (originalError != null) {
                    result.completeExceptionally(originalError);
                } else {
                    result.complete(originalTeleport);
                }
            });
        }

        return result;
    }

}
