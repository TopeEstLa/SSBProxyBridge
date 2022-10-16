package com.bgsoftware.ssbproxybridge.bukkit.island.algorithm;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.action.ServerActions;
import com.bgsoftware.ssbproxybridge.bukkit.utils.Consts;
import com.bgsoftware.ssbproxybridge.bukkit.utils.Text;
import com.bgsoftware.ssbproxybridge.core.bundle.Bundle;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.world.algorithm.DelegateIslandCreationAlgorithm;
import com.bgsoftware.superiorskyblock.api.world.algorithm.IslandCreationAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
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
                Bundle response = new Bundle();
                response.setInt(Consts.Action.RESPONSE_ID, arguments.responseId);

                if (error != null) {
                    response.setString(Consts.Action.CreateIsland.Response.ERROR, error.getMessage());
                    result.completeExceptionally(error);
                } else {
                    response.setBoolean(Consts.Action.CreateIsland.Response.RESULT, true);
                    result.complete(islandCreationResult);
                }

                ServerActions.sendCreationResult(response);
            }));
        } else {
            module.getManager().getServerForNextIsland(builder.getUniqueId()).whenComplete((response, error) -> {
                String targetServer;

                if (error == null && response.contains("error"))
                    error = new RuntimeException("Received error from manager: " + response.getString("error"));

                if (error != null) {
                    logger.warning("Cannot send create-island command due to an unexpected error:");
                    error.printStackTrace();

                    if (Text.isBlank(module.getSettings().managerFallbackServer)) {
                        Player player = builder.getOwner().asPlayer();

                        // TODO: SEND ACTUAL MESSAGE
                        if (player != null) {
                            player.sendMessage("Cannot create an island for you now, try again later.");
                        }

                        // We want to delete the island from the manager.
                        module.getManager().deleteIsland(builder.getUniqueId());

                        result.completeExceptionally(error);

                        return;
                    } else {
                        targetServer = module.getSettings().managerFallbackServer;
                    }
                } else {
                    targetServer = response.getString("result");
                }

                // Create the island in targetServer.

                CompletableFuture<IslandCreationResult> newIslandResult;

                if (targetServer.equals(module.getSettings().serverName)) {
                    newIslandResult = original.createIsland(builder, lastIsland);
                } else {
                    // Create island on another server.
                    newIslandResult = ServerActions.createIsland(targetServer, builder.getUniqueId(),
                            builder.getOwner(), lastIsland, builder.getName(), builder.getScehmaticName(),
                            builder.getBonusWorth(), builder.getBonusLevel());
                }

                newIslandResult.whenComplete((originalResult, originalError) -> {
                    if (originalError != null) {
                        // We want to delete the island from the manager.
                        module.getManager().deleteIsland(builder.getUniqueId());

                        result.completeExceptionally(originalError);
                    } else {
                        result.complete(originalResult);
                    }
                });

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
