package com.bgsoftware.ssbproxybridge.bukkit.action;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.island.creation.RemoteIslandCreationAlgorithm;
import com.bgsoftware.ssbproxybridge.bukkit.utils.Text;
import com.bgsoftware.ssbproxybridge.core.JsonUtil;
import com.bgsoftware.ssbproxybridge.core.requests.IRequestHandler;
import com.bgsoftware.ssbproxybridge.core.requests.RequestHandlerConsumer;
import com.bgsoftware.ssbproxybridge.core.requests.RequestHandlerException;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.service.message.MessagesService;
import com.bgsoftware.superiorskyblock.api.world.algorithm.IslandCreationAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public enum ActionType {

    TELEPORT(dataObject -> requirePlayer(dataObject, player -> {
        SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

        UUID islandUUID = UUID.fromString(dataObject.get("island").getAsString());
        Island targetIsland;

        if (islandUUID.getLeastSignificantBits() == 0 && islandUUID.getMostSignificantBits() == 0) {
            targetIsland = module.getPlugin().getGrid().getSpawnIsland();
        } else {
            targetIsland = module.getPlugin().getGrid().getIslandByUUID(islandUUID);
        }

        if (targetIsland == null) {
            throw new RequestHandlerException("Couldn't teleport player to invalid island \"" + islandUUID + "\"");
        }

        SuperiorPlayer superiorPlayer = module.getPlugin().getPlayers().getSuperiorPlayer(player);
        superiorPlayer.teleport(targetIsland);
    })),

    CREATE_ISLAND(dataObject -> {
        SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

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
                throw new RequestHandlerException("Invalid environment to create island in \"" + environment.name() + "\"");
        }

        module.getPlugin().getGrid().createIsland(islandLeader, schematic, BigDecimal.ZERO, BigDecimal.ZERO, biome, name, offset);
    }),

    SEND_MESSAGE(dataObject -> {
        UUID playerUUID = UUID.fromString(dataObject.get("player").getAsString());
        Player player = Bukkit.getPlayer(playerUUID);

        if (player == null)
            return;

        RegisteredServiceProvider<MessagesService> registeredServiceProvider = Bukkit.getServicesManager().getRegistration(MessagesService.class);

        if (registeredServiceProvider == null)
            return;

        SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

        MessagesService messagesService = registeredServiceProvider.getProvider();
        SuperiorPlayer superiorPlayer = module.getPlugin().getPlayers().getSuperiorPlayer(player);

        IMessageComponent component = messagesService.getComponent(dataObject.get("type").getAsString(), superiorPlayer.getUserLocale());

        JsonArray jsonArgs = dataObject.get("args").getAsJsonArray();

        List<Object> arguments = new ArrayList<>();

        for (JsonElement elementArgument : jsonArgs) {
            Object value = elementArgument.isJsonPrimitive() ?
                    JsonUtil.getValueFromPrimitive(elementArgument.getAsJsonPrimitive()) : null;
            if (value != null)
                arguments.add(value);
        }

        if (component != null) {
            component.sendMessage(player, arguments.toArray(new Object[0]));
        } else {
            // We send the message raw to the player.
            String message = arguments.isEmpty() || arguments.get(0) == null ? null : arguments.get(0).toString();
            boolean translateColors = arguments.size() >= 2 && arguments.get(1) instanceof Boolean && (boolean) arguments.get(1);

            MessagesService.Builder messageBuilder = messagesService.newBuilder();
            messageBuilder.addRawMessage(translateColors ? Text.colorize(message) : message);

            messageBuilder.build().sendMessage(player);
        }
    });

    private final IRequestHandler requestHandler;

    ActionType(IRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    public IRequestHandler getHandler() {
        return requestHandler;
    }

    private static void requirePlayer(JsonObject data, RequestHandlerConsumer<Player> consumer) throws RequestHandlerException {
        UUID playerUUID = UUID.fromString(data.get("player").getAsString());
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            consumer.accept(player);
        } else {
            ActionsQueue.getPlayersQueue().addAction(data, playerUUID, consumer);
        }
    }

}
