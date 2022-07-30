package com.bgsoftware.ssbproxybridge.bukkit.teleport;

import com.bgsoftware.ssbproxybridge.bukkit.proxy.ProxyPlayerBridge;
import com.bgsoftware.ssbproxybridge.core.Singleton;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class ProxyPlayerTeleportAlgorithm implements PlayerTeleportAlgorithm {

    private static PlayerTeleportAlgorithm original;

    private static final Singleton<ProxyPlayerTeleportAlgorithm> SINGLETON = new Singleton<ProxyPlayerTeleportAlgorithm>() {
        @Override
        protected ProxyPlayerTeleportAlgorithm create() {
            return new ProxyPlayerTeleportAlgorithm();
        }
    };

    public static ProxyPlayerTeleportAlgorithm get(PlayerTeleportAlgorithm original) {
        if (ProxyPlayerTeleportAlgorithm.original == null)
            ProxyPlayerTeleportAlgorithm.original = original;
        return SINGLETON.get();
    }

    private ProxyPlayerTeleportAlgorithm() {
    }

    private CompletableFuture<Boolean> teleportToSpawn(Player player) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        ProxyPlayerBridge.fetchServerName(player).whenComplete((serverName, error) -> {
            if (error != null) {
                result.completeExceptionally(error);
            } else {
                // TODO: Better check for spawn server
                if (serverName.equals("spawn")) {
                    // Teleport regularly to the spawn
                    result.complete(false);
                } else {
                    ProxyPlayerBridge.teleportPlayer(player, "spawn");
                    result.complete(true);
                }
            }
        });
        return result;
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Location location) {
        return original.teleport(player, location);
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Island island) {
        if (island.isSpawn()) {
            CompletableFuture<Boolean> result = new CompletableFuture<>();
            teleportToSpawn(player).whenComplete((teleported, error) -> {
                if (error != null) {
                    result.completeExceptionally(error);
                } else if (teleported) {
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
            });
            return result;
        }

        return original.teleport(player, island);
    }

    @Override
    public CompletableFuture<Boolean> teleport(Player player, Island island, World.Environment environment) {
        if (island.isSpawn()) {
            CompletableFuture<Boolean> result = new CompletableFuture<>();
            teleportToSpawn(player).whenComplete((teleported, error) -> {
                if (error != null) {
                    result.completeExceptionally(error);
                } else if (teleported) {
                    result.complete(true);
                } else {
                    original.teleport(player, island, environment).whenComplete((originalTeleport, originalError) -> {
                        if (originalError != null) {
                            result.completeExceptionally(originalError);
                        } else {
                            result.complete(originalTeleport);
                        }
                    });
                }
            });
            return result;
        }

        return original.teleport(player, island, environment);
    }

}
