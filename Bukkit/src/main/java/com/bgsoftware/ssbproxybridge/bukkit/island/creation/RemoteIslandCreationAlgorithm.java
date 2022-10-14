package com.bgsoftware.ssbproxybridge.bukkit.island.creation;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.action.ServerActions;
import com.bgsoftware.ssbproxybridge.bukkit.utils.Text;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.world.algorithm.DelegateIslandCreationAlgorithm;
import com.bgsoftware.superiorskyblock.api.world.algorithm.IslandCreationAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class RemoteIslandCreationAlgorithm extends DelegateIslandCreationAlgorithm {

    private static final SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();
    private static final Logger logger = Logger.getLogger("SSBProxyManager");

    private final IslandCreationAlgorithm original;

    private IslandCreationArguments overriddenArguments;

    public RemoteIslandCreationAlgorithm(IslandCreationAlgorithm original) {
        super(original);
        this.original = original;
    }

    public IslandCreationAlgorithm getOriginal() {
        return original;
    }

    public void createWithArguments(IslandCreationArguments overriddenArguments) {
        this.overriddenArguments = overriddenArguments;
    }

    @Override
    public CompletableFuture<IslandCreationResult> createIsland(Island.Builder builder, BlockPosition lastIsland) {
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

                ServerActions.sendCreationResult(response);
            }));
        } else {
            module.getManager().getServerForNextIsland(builder.getUniqueId()).whenComplete((response, error) -> {
                String targetServer;

                if (error != null) {
                    logger.warning("Cannot send create-island command due to an unexpected error:");
                    error.printStackTrace();

                    if (Text.isBlank(module.getSettings().managerFallbackServer)) {
                        Player player = builder.getOwner().asPlayer();

                        // TODO: SEND ACTUAL MESSAGE
                        if (player != null) {
                            player.sendMessage("Cannot create an island for you now, try again later.");
                        }

                        result.completeExceptionally(error);

                        return;
                    } else {
                        targetServer = module.getSettings().managerFallbackServer;
                    }
                } else if (response.has("error")) {
                    result.completeExceptionally(new RuntimeException("Received error from manager: " + response.get("error").getAsString()));
                    return;
                } else {
                    targetServer = response.get("result").getAsString();
                }

                // Create the island in targetServer.

                if (targetServer.equals(module.getSettings().serverName)) {
                    original.createIsland(builder, lastIsland).whenComplete((originalResult, originalError) -> {
                        if (originalError != null)
                            result.completeExceptionally(originalError);
                        else
                            result.complete(originalResult);
                    });
                } else {
                    // Create island on another server.
                    ServerActions.createIsland(targetServer, builder.getUniqueId(),
                            builder.getOwner(), lastIsland, builder.getName(), builder.getScehmaticName()).whenComplete((creationResult, creationError) -> {
                        if (creationError != null)
                            result.completeExceptionally(creationError);
                        else
                            result.complete(creationResult);
                    });
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
