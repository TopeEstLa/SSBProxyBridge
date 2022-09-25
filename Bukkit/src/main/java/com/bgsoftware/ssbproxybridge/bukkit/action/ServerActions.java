package com.bgsoftware.ssbproxybridge.bukkit.action;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.utils.DatabaseBridgeAccessor;
import com.bgsoftware.ssbproxybridge.core.JsonUtil;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.world.algorithm.IslandCreationAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ServerActions {

    private static final SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();
    private static final Gson gson = new Gson();

    private static final Random random = new Random();

    private ServerActions() {

    }

    public static void teleportToIsland(Player player, String targetServer, UUID islandUUID) {
        JsonObject data = new JsonObject();
        data.addProperty("action", ActionType.TELEPORT.name());
        data.addProperty("island", islandUUID.toString());
        data.addProperty("player", player.getUniqueId().toString());
        sendData(data, targetServer);
    }

    public static CompletableFuture<IslandCreationAlgorithm.IslandCreationResult> createIsland(String targetServer, UUID islandUUID, SuperiorPlayer islandLeader,
                                                                                               BlockPosition blockPosition, String name, Schematic schematic) {
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
        data.addProperty("schematic", schematic.getName());

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
        });

        return result;
    }

    public static void sendMessage(UUID playerUUID, String messageType, Object[] args) {
        JsonObject data = new JsonObject();
        data.addProperty("action", ActionType.SEND_MESSAGE.name());
        data.addProperty("player", playerUUID.toString());
        data.addProperty("type", messageType);

        JsonArray jsonArgs = new JsonArray();
        for (Object argument : args) {
            JsonElement jsonArgument = JsonUtil.getJsonFromObject(argument);
            jsonArgs.add(jsonArgument == null ? new JsonPrimitive(argument.toString()) : jsonArgument);
        }
        data.add("args", jsonArgs);

        sendData(data, null);
    }

    private static void sendData(JsonObject data, @Nullable String recipient) {
        data.addProperty("sender", module.getSettings().serverName);
        data.addProperty("channel", module.getSettings().messagingServiceActionsChannelName);

        if (recipient != null) {
            JsonArray recipients = new JsonArray();
            recipients.add(new JsonPrimitive(recipient));
            data.add("recipients", recipients);
        }

        module.getMessaging().sendData(module.getSettings().messagingServiceActionsChannelName, gson.toJson(data));
    }

    private static void sendData(JsonObject data, @Nullable String recipient, Consumer<JsonObject> responseCallback) {
        int responseId = random.nextInt();
        data.addProperty("response-id", responseId);

        sendData(data, recipient);

        module.getMessaging().listenOnce(module.getSettings().messagingServiceActionsChannelName + "_response", responseBody -> {
            JsonObject response = gson.fromJson(responseBody, JsonObject.class);
            if (response.get("id").getAsInt() == responseId)
                responseCallback.accept(response);
        });
    }

}
