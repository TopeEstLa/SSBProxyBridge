package com.bgsoftware.ssbproxybridge.bukkit.island.creation;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.action.ServerActions;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.world.algorithm.IslandCreationAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RemoteIslandCreationAlgorithm implements IslandCreationAlgorithm {

    private static final Gson gson = new Gson();

    private static final SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

    private final IslandCreationAlgorithm original;

    private IslandCreationArguments overriddenArguments;

    public RemoteIslandCreationAlgorithm(IslandCreationAlgorithm original) {
        this.original = original;
    }

    public IslandCreationAlgorithm getOriginal() {
        return original;
    }

    public void createWithArguments(IslandCreationArguments overriddenArguments) {
        this.overriddenArguments = overriddenArguments;
    }

    @Override
    public CompletableFuture<IslandCreationResult> createIsland(UUID islandUUID,
                                                                SuperiorPlayer islandLeader,
                                                                BlockPosition blockPosition,
                                                                String name,
                                                                Schematic schematic) {
        CompletableFuture<IslandCreationResult> result = new CompletableFuture<>();

        if (this.overriddenArguments != null) {
            IslandCreationArguments arguments = this.overriddenArguments;
            this.overriddenArguments = null;
            original.createIsland(arguments.islandUUID, arguments.islandLeader,
                    arguments.blockPosition, arguments.name, arguments.schematic).whenComplete(((islandCreationResult, error) -> {
                JsonObject response = new JsonObject();
                response.addProperty("id", arguments.responseId);

                if (error != null) {
                    response.addProperty("error", error.getMessage());
                    result.completeExceptionally(error);
                } else {
                    response.addProperty("result", true);
                    result.complete(islandCreationResult);
                }

                module.getMessaging().sendData(module.getSettings().messagingServiceActionsChannelName + "_response", gson.toJson(response));
            }));
        } else {
            module.getManager().getServerForNextIsland(islandUUID).whenComplete((response, error) -> {
                if (error != null) {
                    result.completeExceptionally(error);
                } else if (response.has("error")) {
                    result.completeExceptionally(new RuntimeException("Received error from manager: " + response.get("error").getAsString()));
                } else {
                    String targetServer = response.get("result").getAsString();

                    if (targetServer.equals(module.getSettings().serverName)) {
                        original.createIsland(islandUUID, islandLeader, blockPosition, name, schematic).whenComplete((originalResult, originalError) -> {
                            if (originalError != null)
                                result.completeExceptionally(originalError);
                            else
                                result.complete(originalResult);
                        });
                    } else {
                        // Create island on another server.
                        ServerActions.createIsland(targetServer, islandUUID, islandLeader, blockPosition, name, schematic).whenComplete((creationResult, creationError) -> {
                            if (creationError != null)
                                result.completeExceptionally(creationError);
                            else
                                result.complete(creationResult);
                        });
                    }

                }
            });
        }

        return result;
    }

    public static class IslandCreationArguments {

        private final UUID islandUUID;
        private final SuperiorPlayer islandLeader;
        private final BlockPosition blockPosition;
        private final String name;
        private final Schematic schematic;
        private final int responseId;

        public IslandCreationArguments(UUID islandUUID,
                                       SuperiorPlayer islandLeader,
                                       BlockPosition blockPosition,
                                       String name,
                                       Schematic schematic,
                                       int responseId) {
            this.islandUUID = islandUUID;
            this.islandLeader = islandLeader;
            this.blockPosition = blockPosition;
            this.name = name;
            this.schematic = schematic;
            this.responseId = responseId;
        }

    }

}
