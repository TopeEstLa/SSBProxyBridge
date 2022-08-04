package com.bgsoftware.ssbproxybridge.bukkit.island;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandCalculationAlgorithm;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class RemoteIslandCalculationAlgorithm implements IslandCalculationAlgorithm {

    private static final RemoteIslandCalculationAlgorithm INSTANCE = new RemoteIslandCalculationAlgorithm();

    private static final IslandCalculationResult EMPTY_RESULT = Collections::emptyMap;

    public static RemoteIslandCalculationAlgorithm getInstance() {
        return INSTANCE;
    }

    private RemoteIslandCalculationAlgorithm() {

    }

    @Override
    public CompletableFuture<IslandCalculationResult> calculateIsland(Island island) {
        return CompletableFuture.completedFuture(EMPTY_RESULT);
    }

}
