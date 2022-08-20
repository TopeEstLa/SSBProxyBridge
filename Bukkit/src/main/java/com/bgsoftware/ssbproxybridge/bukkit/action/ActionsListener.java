package com.bgsoftware.ssbproxybridge.bukkit.action;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.connector.JsonConnectorListener;
import com.bgsoftware.ssbproxybridge.bukkit.island.creation.RemoteIslandCreationAlgorithm;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.algorithm.IslandCreationAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class ActionsListener extends JsonConnectorListener {

    private final Map<String, IAction<Player>> PLAYER_ACTIONS = new ImmutableMap.Builder<String, IAction<Player>>()
            .put("teleport", this::handlePlayerTeleport)
            .build();
    private final Map<String, IAction<Void>> GENERAL_ACTIONS = new ImmutableMap.Builder<String, IAction<Void>>()
            .put("create_island", this::handleIslandCreation)
            .build();

    public ActionsListener(SSBProxyBridgeModule module) {
        super(module, module.getSettings().messagingServiceActionsChannelName);
    }

    @Override
    protected void processRequest(JsonObject dataObject) {
        String action = dataObject.get("action").getAsString();

        IAction<Player> playerAction = PLAYER_ACTIONS.get(action);

        if (playerAction != null) {
            UUID playerUUID = UUID.fromString(dataObject.get("player").getAsString());
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                if (playerAction.run(dataObject, player))
                    return;
            } else {
                ActionsQueue.getPlayersQueue().addAction(dataObject, playerUUID, playerAction);
                return;
            }

        }

        IAction<Void> generalAction = GENERAL_ACTIONS.get(action);
        if (generalAction != null) {
            generalAction.run(dataObject, null);
            return;
        }

        this.module.getLogger().warning("Received an unknown action to handle: \"" + action + "\":");
        this.module.getLogger().warning(dataObject + "");
    }

    private boolean handlePlayerTeleport(JsonObject dataObject, Player player) {
        UUID islandUUID = UUID.fromString(dataObject.get("island").getAsString());
        Island targetIsland;

        if (islandUUID.getLeastSignificantBits() == 0 && islandUUID.getMostSignificantBits() == 0) {
            targetIsland = module.getPlugin().getGrid().getSpawnIsland();
        } else {
            targetIsland = module.getPlugin().getGrid().getIslandByUUID(islandUUID);
        }

        if (targetIsland == null) {
            return false;
        }

        SuperiorPlayer superiorPlayer = module.getPlugin().getPlayers().getSuperiorPlayer(player);
        superiorPlayer.teleport(targetIsland);

        return true;
    }

    private boolean handleIslandCreation(JsonObject dataObject, Void unused) {
        UUID islandUUID = UUID.fromString(dataObject.get("uuid").getAsString());
        SuperiorPlayer islandLeader = SuperiorSkyblockAPI.getPlayer(UUID.fromString(dataObject.get("leader").getAsString()));

        JsonObject position = dataObject.get("position").getAsJsonObject();

        BlockPosition blockPosition = module.getPlugin().getFactory().createBlockPosition(position.get("world").getAsString(),
                position.get("x").getAsInt(), position.get("y").getAsInt(), position.get("z").getAsInt());

        String name = dataObject.get("name").getAsString();
        String schematic = dataObject.get("schematic").getAsString();
        int responseId = dataObject.get("response-id").getAsInt();

        IslandCreationAlgorithm islandCreationAlgorithm = module.getPlugin().getGrid().getIslandCreationAlgorithm();
        if (islandCreationAlgorithm instanceof RemoteIslandCreationAlgorithm) {
            RemoteIslandCreationAlgorithm.IslandCreationArguments arguments = new RemoteIslandCreationAlgorithm
                    .IslandCreationArguments(islandUUID, islandLeader, blockPosition, name,
                    module.getPlugin().getSchematics().getSchematic(schematic), responseId);
            ((RemoteIslandCreationAlgorithm) islandCreationAlgorithm).createWithArguments(arguments);
        }

        boolean offset;
        Biome biome;

        World.Environment environment = module.getPlugin().getSettings().getWorlds().getDefaultWorld();
        switch (environment) {
            case NORMAL:
                offset = module.getPlugin().getSettings().getWorlds().getNormal().isSchematicOffset();
                biome = Biome.valueOf(module.getPlugin().getSettings().getWorlds().getNormal().getBiome());
                break;
            case NETHER:
                offset = module.getPlugin().getSettings().getWorlds().getNether().isSchematicOffset();
                biome = Biome.valueOf(module.getPlugin().getSettings().getWorlds().getNether().getBiome());
                break;
            case THE_END:
                offset = module.getPlugin().getSettings().getWorlds().getEnd().isSchematicOffset();
                biome = Biome.valueOf(module.getPlugin().getSettings().getWorlds().getEnd().getBiome());
                break;
            default:
                return false;
        }

        module.getPlugin().getGrid().createIsland(islandLeader, schematic, BigDecimal.ZERO, BigDecimal.ZERO, biome, name, offset);

        return true;
    }

}
