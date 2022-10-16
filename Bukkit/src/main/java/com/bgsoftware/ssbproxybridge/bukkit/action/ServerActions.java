package com.bgsoftware.ssbproxybridge.bukkit.action;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.utils.Consts;
import com.bgsoftware.ssbproxybridge.bukkit.utils.DatabaseBridgeAccessor;
import com.bgsoftware.ssbproxybridge.bukkit.utils.Serializers;
import com.bgsoftware.ssbproxybridge.core.bundle.Bundle;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.world.algorithm.IslandCreationAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ServerActions {

    private static final SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();
    private static final Logger logger = Logger.getLogger("SSBProxyBridge");
    private static final Random random = new Random();

    private static final Queue<Bundle> pendingRequests = new LinkedList<>();

    private ServerActions() {

    }

    public static void teleportToIsland(Player player, String targetServer, UUID islandUUID) {
        Bundle bundle = new Bundle();
        bundle.setEnum(Consts.Action.ACTION, ActionType.TELEPORT);
        bundle.setUUID(Consts.Action.Teleport.ISLAND, islandUUID);
        bundle.setUUID(Consts.Action.PLAYER, player.getUniqueId());
        sendData(bundle, targetServer, error -> {
            logger.warning("Cannot send teleport-island command due to an unexpected error:");
            error.printStackTrace();

            // TODO: SEND ACTUAL MESSAGE
            if (player.isOnline()) {
                player.sendMessage("Cannot teleport you now, try again later.");
            }
        });
    }

    public static void teleportToLocation(Player player, String targetServer, Location location) {
        Bundle bundle = new Bundle();
        bundle.setEnum(Consts.Action.ACTION, ActionType.TELEPORT);
        bundle.setExtra(Consts.Action.Teleport.LOCATION, Serializers.serializeLocation(location));
        bundle.setUUID(Consts.Action.PLAYER, player.getUniqueId());
        sendData(bundle, targetServer, error -> {
            logger.warning("Cannot send teleport command due to an unexpected error:");
            error.printStackTrace();

            // TODO: SEND ACTUAL MESSAGE
            if (player.isOnline()) {
                player.sendMessage("Cannot teleport you now, try again later.");
            }
        });
    }

    public static CompletableFuture<IslandCreationAlgorithm.IslandCreationResult> createIsland(String targetServer,
                                                                                               UUID islandUUID,
                                                                                               SuperiorPlayer islandLeader,
                                                                                               BlockPosition blockPosition,
                                                                                               String name,
                                                                                               String schematic,
                                                                                               BigDecimal worthBonus,
                                                                                               BigDecimal levelBonus) {
        CompletableFuture<IslandCreationAlgorithm.IslandCreationResult> result = new CompletableFuture<>();

        Bundle position = new Bundle();
        position.setString("world", blockPosition.getWorldName());
        position.setInt("x", blockPosition.getX());
        position.setInt("y", blockPosition.getY());
        position.setInt("z", blockPosition.getZ());

        Bundle bundle = new Bundle();
        bundle.setEnum(Consts.Action.ACTION, ActionType.CREATE_ISLAND);
        bundle.setUUID(Consts.Action.CreateIsland.UUID, islandUUID);
        bundle.setUUID(Consts.Action.CreateIsland.LEADER, islandLeader.getUniqueId());
        bundle.setExtra(Consts.Action.CreateIsland.POSITION, position);
        bundle.setString(Consts.Action.CreateIsland.NAME, name);
        bundle.setString(Consts.Action.CreateIsland.SCHEMATIC, schematic);
        bundle.setBigDecimal(Consts.Action.CreateIsland.WORTH_BONUS, worthBonus);
        bundle.setBigDecimal(Consts.Action.CreateIsland.LEVELS_BONUS, levelBonus);

        sendData(bundle, targetServer, response -> {
            if (response.contains(Consts.Action.CreateIsland.Response.ERROR)) {
                result.completeExceptionally(new RuntimeException("Failed to create island: " + response.getString("error")));
                return;
            }

            ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                    .setNameFormat("SSBProxyBridge Island Creation Waiting").build());

            executorService.execute(() -> {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ignored) {
                        // Do nothing.
                    }

                    Island island = SuperiorSkyblockAPI.getIslandByUUID(islandUUID);

                    if (island == null)
                        continue;

                    DatabaseBridgeAccessor.runWithoutDataSave(island, (Runnable) () -> {
                        result.complete(new IslandCreationAlgorithm.IslandCreationResult(island,
                                island.getCenter(module.getPlugin().getSettings().getWorlds().getDefaultWorld()),
                                true));
                    });

                    return;
                }
            });
        }, result::completeExceptionally);

        return result;
    }

    public static void sendCreationResult(Bundle bundle) {
        sendResponse(bundle, error -> {
            logger.warning("Cannot send creation island result due to an unexpected error:");
            error.printStackTrace();
            // We add the request to the pending requests queue, so players will be notified later.
            pendingRequests.add(bundle);
        });
    }

    public static CompletableFuture<IslandCalculationAlgorithm.IslandCalculationResult> calculateIsland(String targetServer,
                                                                                                        UUID islandUUID) {
        CompletableFuture<IslandCalculationAlgorithm.IslandCalculationResult> result = new CompletableFuture<>();

        Bundle bundle = new Bundle();
        bundle.setEnum(Consts.Action.ACTION, ActionType.CALCULATE_ISLAND);
        bundle.setUUID(Consts.Action.CalculateIsland.ISLAND, islandUUID);

        sendData(bundle, targetServer, response -> {
            if (response.contains(Consts.Action.CalculateIsland.Response.ERROR)) {
                result.completeExceptionally(new RuntimeException("Failed to calculate island: " + response.getString("error")));
                return;
            }

            KeyMap<BigInteger> blockCounts = KeyMap.createKeyMap();
            List<Object> blockCountsArray = response.getList(Consts.Action.CalculateIsland.Response.RESULT);
            blockCountsArray.forEach(blockCountElement -> {
                Bundle blockCount = (Bundle) blockCountElement;
                Key block = Key.of(blockCount.getString(Consts.Action.CalculateIsland.BlockCount.BLOCK));
                BigInteger count = blockCount.getBigInteger(Consts.Action.CalculateIsland.BlockCount.COUNT);
                blockCounts.put(block, count);
            });

            result.complete(() -> blockCounts);
        }, error -> {
            logger.warning("Cannot send calculate command due to an unexpected error:");
            error.printStackTrace();
            result.completeExceptionally(error);
        });

        return result;
    }

    public static void sendCalculationResult(Bundle bundle) {
        sendResponse(bundle, error -> {
            logger.warning("Cannot send calculation island result due to an unexpected error:");
            error.printStackTrace();
        });
    }

    public static void sendMessage(@Nullable UUID playerUUID, String messageType, Object[] args) {
        Bundle bundle = new Bundle();
        bundle.setEnum(Consts.Action.ACTION, ActionType.SEND_MESSAGE);

        if (playerUUID != null) {
            bundle.setUUID(Consts.Action.SendMessage.PLAYER, playerUUID);
        } else {
            bundle.setString(Consts.Action.SendMessage.CONSOLE, "");
        }

        bundle.setString(Consts.Action.SendMessage.TYPE, messageType);
        bundle.setList(Consts.Action.SendMessage.ARGS, Arrays.asList(args));

        sendData(bundle, null, error -> {
            logger.warning("Cannot send message command due to an unexpected error:");
            error.printStackTrace();
        });
    }

    public static void warpPlayer(Player player, String targetServer, UUID islandUUID, String warpName) {
        Bundle bundle = new Bundle();
        bundle.setEnum(Consts.Action.ACTION, ActionType.WARP_PLAYER);
        bundle.setUUID(Consts.Action.WarpPlayer.ISLAND, islandUUID);
        bundle.setString(Consts.Action.WarpPlayer.WARP_NAME, warpName);
        bundle.setUUID(Consts.Action.PLAYER, player.getUniqueId());
        sendData(bundle, targetServer, error -> {
            logger.warning("Cannot send warp command due to an unexpected error:");
            error.printStackTrace();

            // TODO: SEND ACTUAL MESSAGE
            if (player.isOnline()) {
                player.sendMessage("Cannot warp you now, try again later.");
            }
        });
    }

    public static void setIslandBiome(String targetServer, UUID islandUUID, Biome biome, boolean updateBlocks) {
        Bundle bundle = new Bundle();
        bundle.setEnum(Consts.Action.ACTION, ActionType.SET_BIOME);
        bundle.setUUID(Consts.Action.SetBiome.ISLAND, islandUUID);
        bundle.setEnum(Consts.Action.SetBiome.BIOME, biome);
        bundle.setBoolean(Consts.Action.SetBiome.UPDATE_BLOCKS, updateBlocks);
        sendData(bundle, targetServer, error -> {
            logger.warning("Cannot send biome command due to an unexpected error:");
            error.printStackTrace();
        });
    }

    public static Queue<Bundle> getPendingRequests() {
        return pendingRequests;
    }

    private static void sendResponse(Bundle bundle, Consumer<Throwable> errorCallback) {
        finishData(module.getSettings().messagingServiceActionsChannelName + "_response", bundle, null);
        module.getMessaging().sendBundle(bundle, errorCallback);
    }

    private static void sendData(Bundle bundle, @Nullable String recipient, Consumer<Throwable> errorCallback) {
        finishData(module.getSettings().messagingServiceActionsChannelName, bundle, recipient);
        module.getMessaging().sendBundle(bundle, errorCallback);
    }

    private static void sendData(Bundle bundle, @Nullable String recipient, Consumer<Bundle> responseCallback,
                                 Consumer<Throwable> errorCallback) {
        int responseId = random.nextInt();
        bundle.setInt(Consts.Action.RESPONSE_ID, responseId);

        sendData(bundle, recipient, errorCallback);

        module.getMessaging().listenOnce(module.getSettings().messagingServiceActionsChannelName + "_response", response -> {
            if (response.getInt(Consts.Action.RESPONSE_ID) == responseId)
                responseCallback.accept(response);
        });
    }

    private static void finishData(String channelName, Bundle bundle, @Nullable String recipient) {
        bundle.setSender(module.getSettings().serverName);
        bundle.setChannelName(channelName);

        if (recipient != null)
            bundle.setRecipient(recipient);
    }

}
