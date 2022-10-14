package com.bgsoftware.ssbproxybridge.bukkit.island;

import com.bgsoftware.ssbproxybridge.bukkit.island.algorithm.RemoteIslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.api.factory.DelegateIslandsFactory;
import com.bgsoftware.superiorskyblock.api.factory.IslandsFactory;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandCalculationAlgorithm;

import javax.annotation.Nullable;
import java.util.UUID;

public class RemoteIslandsFactory extends DelegateIslandsFactory {

    private final UUID filteredIslandUUID;

    public RemoteIslandsFactory(@Nullable IslandsFactory handle, UUID filteredIslandUUID) {
        super(handle);
        this.filteredIslandUUID = filteredIslandUUID;
    }

    @Override
    public IslandCalculationAlgorithm createIslandCalculationAlgorithm(Island island, IslandCalculationAlgorithm original) {
        return filteredIslandUUID.equals(island.getUniqueId()) ? new RemoteIslandCalculationAlgorithm() : original;
    }

}
