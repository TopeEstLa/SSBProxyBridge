package com.bgsoftware.ssbproxybridge.bukkit.action;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.utils.DatabaseBridgeAccessor;
import com.bgsoftware.ssbproxybridge.bukkit.utils.Serializers;
import com.bgsoftware.ssbproxybridge.core.JsonUtil;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.world.algorithm.IslandCreationAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
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
    private static final Gson gson = new Gson();
    private static final Random random = new Random();

    private static final Queue<RequestData> pendingRequests = new LinkedList<>();

    private ServerActions() {

    }

    public static void teleportToIsland(Player player, String targetServer, UUID islandUUID) {
        JsonObject data = new JsonObject();
        data.addProperty("action", ActionType.TELEPORT.name());
        data.addProperty("island", islandUUID.toString());
        data.addProperty("player", player.getUniqueId().toString());
        sendData(data, targetServer, error -> {
            logger.warning("Cannot send teleport-island command due to an unexpected error:");
            error.printStackTrace();

            // TODO: SEND ACTUAL MESSAGE
            if (player.isOnline()) {
                player.sendMessage("Cannot teleport you now, try again later.");
            }
        });
    }

    public static void teleportToLocation(Player player, String targetServer, Location location) {
        JsonObject data = new JsonObject();
        data.addProperty("action", ActionType.TELEPORT.name());
        data.addProperty("location", Serializers.serializeLocation(location));
        data.addProperty("player", player.getUniqueId().toString());
        sendData(data, targetServer, error -> {
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

        JsonObject position = new JsonObject();
        position.addProperty("world", blockPosition.getWorldName());
        position.addProperty("x", blockPosition.getX());
        position.addProperty("y", blockPosition.getY());
        position.addProperty("z", blockPosition.getZ());

        JsonObject data = new JsonObject();
        data.addProperty("action", ActionType.CREATE_ISLAND.name());
        data.addProperty("uuid", islandUUID.toString());
        data.addProperty("leader", islandLeader.getUniqueId().toString());
        data.add("position", position);
        data.addProperty("name", name);
        data.addProperty("schematic", schematic);
        data.addProperty("worth_bonus", worthBonus);
        data.addProperty("level_bonus", levelBonus);

        int responseId = new Random().nextInt();
        data.addProperty("response-id", responseId);

        sendData(data, targetServer, response -> {
            if (response.has("error")) {
                result.completeExceptionally(new RuntimeException("Failed to create island: " + response.get("error").getAsString()));
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

    public static void sendCreationResult(JsonObject result) {
        sendResponse(result, error -> {
            logger.warning("Cannot send creation island result due to an unexpected error:");
            error.printStackTrace();
            // We add the request to the pending requests queue, so players will be notified later.
            pendingRequests.add(new RequestData(result, null));
        });
    }

    public static CompletableFuture<IslandCalculationAlgorithm.IslandCalculationResult> calculateIsland(String targetServer,
                                                                                                        UUID islandUUID) {
        CompletableFuture<IslandCalculationAlgorithm.IslandCalculationResult> result = new CompletableFuture<>();

        JsonObject request = new JsonObject();
        request.addProperty("action", ActionType.CALCULATE_ISLAND.name());
        request.addProperty("island", islandUUID.toString());

        sendData(request, targetServer, response -> {
            if (response.has("error")) {
                result.completeExceptionally(new RuntimeException("Failed to calculate island: " + response.get("error").getAsString()));
                return;
            }

            KeyMap<BigInteger> blockCounts = KeyMap.createKeyMap();
            JsonArray blockCountsArray = response.getAsJsonArray("block_counts");
            blockCountsArray.forEach(blockCountElement -> {
                JsonObject blockCount = blockCountElement.getAsJsonObject();
                Key block = Key.of(blockCount.get("block").getAsString());
                BigInteger count = blockCount.get("count").getAsBigInteger();
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

    public static void sendCalculationResult(JsonObject result) {
        sendResponse(result, error -> {
            logger.warning("Cannot send calculation island result due to an unexpected error:");
            error.printStackTrace();
        });
    }

    public static void sendMessage(@Nullable UUID playerUUID, String messageType, Object[] args) {
        JsonObject data = new JsonObject();
        data.addProperty("action", ActionType.SEND_MESSAGE.name());

        if (playerUUID != null) {
            data.addProperty("player", playerUUID.toString());
        } else {
            data.addProperty("console", "");
        }

        data.addProperty("type", messageType);

        JsonArray jsonArgs = new JsonArray();
        for (Object argument : args) {
            JsonElement jsonArgument = JsonUtil.getJsonFromObject(argument);
            jsonArgs.add(jsonArgument == null ? new JsonPrimitive(argument.toString()) : jsonArgument);
        }
        data.add("args", jsonArgs);

        sendData(data, null, error -> {
            logger.warning("Cannot send message command due to an unexpected error:");
            error.printStackTrace();
        });
    }

    public static void warpPlayer(Player player, String targetServer, UUID islandUUID, String warpName) {
        JsonObject data = new JsonObject();
        data.addProperty("action", ActionType.WARP_PLAYER.name());
        data.addProperty("island", islandUUID.toString());
        data.addProperty("warp_name", warpName);
        data.addProperty("player", player.getUniqueId().toString());
        sendData(data, targetServer, error -> {
            logger.warning("Cannot send warp command due to an unexpected error:");
            error.printStackTrace();

            // TODO: SEND ACTUAL MESSAGE
            if (player.isOnline()) {
                player.sendMessage("Cannot warp you now, try again later.");
            }
        });
    }

    public static void setIslandBiome(String targetServer, UUID islandUUID, Biome biome, boolean updateBlocks) {
        JsonObject data = new JsonObject();
        data.addProperty("action", ActionType.SET_BIOME.name());
        data.addProperty("island", islandUUID.toString());
        data.addProperty("biome", biome.name());
        data.addProperty("update_blocks", updateBlocks);
        sendData(data, targetServer, error -> {
            logger.warning("Cannot send biome command due to an unexpected error:");
            error.printStackTrace();
        });
    }

    public static Queue<RequestData> getPendingRequests() {
        return pendingRequests;
    }

    private static void sendResponse(JsonObject data, Consumer<Throwable> errorCallback) {
        finishData(data, null);
        module.getMessaging().sendData(module.getSettings().messagingServiceActionsChannelName + "_response", gson.toJson(data), errorCallback);
    }

    private static void sendData(JsonObject data, @Nullable String recipient, Consumer<Throwable> errorCallback) {
        finishData(data, recipient);
        module.getMessaging().sendData(module.getSettings().messagingServiceActionsChannelName, gson.toJson(data), errorCallback);
    }

    private static void sendData(JsonObject data, @Nullable String recipient,
                                 Consumer<JsonObject> responseCallback, Consumer<Throwable> errorCallback) {
        int responseId = random.nextInt();
        data.addProperty("response-id", responseId);

        sendData(data, recipient, errorCallback);

        module.getMessaging().listenOnce(module.getSettings().messagingServiceActionsChannelName + "_response", responseBody -> {
            JsonObject response = gson.fromJson(responseBody, JsonObject.class);
            if (response.get("id").getAsInt() == responseId)
                responseCallback.accept(response);
        });
    }

    private static void finishData(JsonObject data, @Nullable String recipient) {
        data.addProperty("sender", module.getSettings().serverName);
        data.addProperty("channel", module.getSettings().messagingServiceActionsChannelName);

        if (recipient != null) {
            JsonArray recipients = new JsonArray();
            recipients.add(new JsonPrimitive(recipient));
            data.add("recipients", recipients);
        }
    }

    private static class RequestData {

        private final JsonObject data;
        @Nullable
        private final String recipient;

        RequestData(JsonObject data, @Nullable String recipient) {
            this.data = data;
            this.recipient = recipient;
        }

    }

}
