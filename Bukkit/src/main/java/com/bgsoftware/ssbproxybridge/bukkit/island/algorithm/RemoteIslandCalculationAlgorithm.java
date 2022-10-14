package com.bgsoftware.ssbproxybridge.bukkit.island.algorithm;

import com.bgsoftware.ssbproxybridge.bukkit.action.ServerActions;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandCalculationAlgorithm;

import java.util.concurrent.CompletableFuture;

public class RemoteIslandCalculationAlgorithm implements IslandCalculationAlgorithm {

    private String targetServer;

    public RemoteIslandCalculationAlgorithm() {
    }

    public void setTargetServer(String targetServer) {
        this.targetServer = targetServer;
    }

    @Override
    public CompletableFuture<IslandCalculationResult> calculateIsland(Island island) {
        return ServerActions.calculateIsland(this.targetServer, island.getUniqueId());
    }

}
