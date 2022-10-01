package com.bgsoftware.ssbproxybridge.bukkit.island;

import com.bgsoftware.ssbproxybridge.bukkit.utils.DatabaseBridgeAccessor;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.DelegateIsland;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class RemoteIsland extends DelegateIsland {

    private static final SuperiorSkyblock plugin = SuperiorSkyblockAPI.getSuperiorSkyblock();

    private final String originalServer;

    public RemoteIsland(String originalServer, Island handle) {
        super(handle);
        this.originalServer = originalServer;
    }

    public String getOriginalServer() {
        return originalServer;
    }

    @Override
    public void addMember(SuperiorPlayer superiorPlayer, PlayerRole playerRole) {
        try {
            super.addMember(superiorPlayer, playerRole);
        } finally {
            // We want the RemoteIsland instance to be saved as the island of the player, and not the actual handle.
            superiorPlayer.setIsland(this);
        }
    }

    @Override
    public List<Chunk> getAllChunks() {
        // On another server, therefore no chunks.
        return Collections.emptyList();
    }

    @Override
    public List<Chunk> getAllChunks(boolean onlyProtected) {
        // On another server, therefore no chunks.
        return Collections.emptyList();
    }

    @Override
    public List<Chunk> getAllChunks(World.Environment environment) {
        // On another server, therefore no chunks.
        return Collections.emptyList();
    }

    @Override
    public List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected) {
        // On another server, therefore no chunks.
        return Collections.emptyList();
    }

    @Override
    public List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks) {
        // On another server, therefore no chunks.
        return Collections.emptyList();
    }

    @Override
    public List<Chunk> getLoadedChunks(boolean onlyProtected, boolean noEmptyChunks) {
        // On another server, therefore no chunks.
        return Collections.emptyList();
    }

    @Override
    public List<Chunk> getLoadedChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks) {
        // On another server, therefore no chunks.
        return Collections.emptyList();
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected,
                                                            @Nullable Consumer<Chunk> onChunkLoad) {
        // On another server, therefore no chunks.
        return Collections.emptyList();
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected,
                                                            boolean noEmptyChunks, @Nullable Consumer<Chunk> onChunkLoad) {
        // On another server, therefore no chunks.
        return Collections.emptyList();
    }

    @Override
    public void resetChunks(World.Environment environment, boolean onlyProtected) {
        // On another server, therefore no chunks.
    }

    @Override
    public void resetChunks(World.Environment environment, boolean onlyProtected, @Nullable Runnable onFinish) {
        // On another server, therefore no chunks.
    }

    @Override
    public void resetChunks(boolean onlyProtected) {
        // On another server, therefore no chunks.
    }

    @Override
    public void resetChunks(boolean onlyProtected, @Nullable Runnable onFinish) {
        // On another server, therefore no chunks.
    }

    private static boolean ignoreRemoteStatus = false;

    public static void setIgnoreRemoteStatus(boolean ignoreRemoteStatus) {
        RemoteIsland.ignoreRemoteStatus = ignoreRemoteStatus;
    }

    @Override
    public boolean isInside(Location location) {
        // On another server, therefore not inside (unless ignoreRemoteStatus=true)
        return ignoreRemoteStatus && super.isInside(location);
    }

    @Override
    public boolean isInsideRange(Location location) {
        // On another server, therefore not inside (unless ignoreRemoteStatus=true)
        return ignoreRemoteStatus && super.isInsideRange(location);
    }

    @Override
    public boolean isInsideRange(Location location, int extra) {
        // On another server, therefore not inside (unless ignoreRemoteStatus=true)
        return ignoreRemoteStatus && super.isInsideRange(location, extra);
    }

    @Override
    public boolean isInsideRange(Chunk chunk) {
        // On another server, therefore not inside (unless ignoreRemoteStatus=true)
        return ignoreRemoteStatus && super.isInsideRange(chunk);
    }

    public void removeIsland() {
        // Remove roles and island status from leader and members
        getIslandMembers(true).forEach(islandMember -> {
            DatabaseBridgeAccessor.runWithoutDataSave(islandMember, (Runnable) () -> {
                islandMember.setIsland(null);
                islandMember.setPlayerRole(plugin.getRoles().getGuestRole());
            });
        });

        plugin.getGrid().getIslandsContainer().removeIsland(this);
    }

    @Override
    public void calcIslandWorth(@Nullable SuperiorPlayer superiorPlayer, @Nullable Runnable runnable) {
        // TODO
    }

    @Override
    public IslandCalculationAlgorithm getCalculationAlgorithm() {
        return RemoteIslandCalculationAlgorithm.getInstance();
    }

    @Override
    public void setBiome(Biome biome, boolean updateBlocks) {
        // TODO
    }

    @Override
    public void sendTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int duration,
                          int fadeOut, UUID... ignoredMembers) {
        // TODO
    }

    @Override
    public void executeCommand(String command, boolean onlyOnlineMembers, UUID... ignoredMembers) {
        // TODO: other server command execution
    }

    @Override
    public boolean isBeingRecalculated() {
        return false;
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType) {
        // Island is on another server, no entities check.
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(Key key) {
        // Island is on another server, no entities check.
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType, int amount) {
        // Island is on another server, no entities check.
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(Key key, int amount) {
        // Island is on another server, no entities check.
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public void warpPlayer(SuperiorPlayer superiorPlayer, String name) {
        // TODO
    }

    @Nullable
    @Override
    public Key generateBlock(Location location, boolean optimizeCobblestone) {
        // Island is on another server, do nothing.
        return Key.of("AIR");
    }

    @Override
    public Key generateBlock(Location location, World.Environment environment, boolean optimizeCobblestone) {
        // Island is on another server, do nothing.
        return Key.of("AIR");
    }

    @Override
    public IslandChest[] getChest() {
        // Island chests are not synchronized, therefore no chests available.
        return new IslandChest[0];
    }

    @Override
    public int getChestSize() {
        // Island chests are not synchronized, therefore no chests available.
        return 0;
    }

    @Override
    public void setChestRows(int index, int rows) {
        // Do nothing.
    }

}
