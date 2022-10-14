package com.bgsoftware.ssbproxybridge.bukkit.action;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.island.algorithm.RemoteIslandCreationAlgorithm;
import com.bgsoftware.ssbproxybridge.bukkit.player.RemoteSuperiorPlayer;
import com.bgsoftware.ssbproxybridge.bukkit.teleport.ProxyPlayerTeleportAlgorithm;
import com.bgsoftware.ssbproxybridge.bukkit.teleport.ProxyPlayersFactory;
import com.bgsoftware.ssbproxybridge.bukkit.utils.BukkitExecutor;
import com.bgsoftware.ssbproxybridge.bukkit.utils.MessagesSender;
import com.bgsoftware.ssbproxybridge.bukkit.utils.PlayerLocales;
import com.bgsoftware.ssbproxybridge.bukkit.utils.Serializers;
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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public enum ActionType {

    TELEPORT(dataObject -> requirePlayer(dataObject, player -> {
        SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

        SuperiorPlayer superiorPlayer = module.getPlugin().getPlayers().getSuperiorPlayer(player);

        if (dataObject.has("island")) {
            UUID islandUUID = UUID.fromString(dataObject.get("island").getAsString());
            Island targetIsland = islandUUID.equals(new UUID(0, 0)) ?
                    module.getPlugin().getGrid().getSpawnIsland() :
                    module.getPlugin().getGrid().getIslandByUUID(islandUUID);

            if (targetIsland == null) {
                throw new RequestHandlerException("Couldn't teleport player to invalid island \"" + islandUUID + "\"");
            }

            superiorPlayer.teleport(targetIsland);
        } else if (dataObject.has("location")) {
            Location location = Serializers.deserializeLocation(dataObject.get("location").getAsString());

            if (location == null || location.getWorld() == null)
                throw new RequestHandlerException("Couldn't teleport player to invalid location \"" + location + "\"");

            superiorPlayer.teleport(location);
        }

    }, true)),

    CREATE_ISLAND(dataObject -> {
        SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

        UUID islandUUID = UUID.fromString(dataObject.get("uuid").getAsString());
        SuperiorPlayer islandLeader = SuperiorSkyblockAPI.getPlayer(UUID.fromString(dataObject.get("leader").getAsString()));

        JsonObject position = dataObject.get("position").getAsJsonObject();

        BlockPosition blockPosition = module.getPlugin().getFactory().createBlockPosition(position.get("world").getAsString(),
                position.get("x").getAsInt(), position.get("y").getAsInt(), position.get("z").getAsInt());

        String name = dataObject.get("name").getAsString();
        String schematic = dataObject.get("schematic").getAsString();
        BigDecimal worthBonus = dataObject.get("worth_bonus").getAsBigDecimal();
        BigDecimal levelBonus = dataObject.get("level_bonus").getAsBigDecimal();

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

        module.getPlugin().getGrid().createIsland(islandLeader, schematic, worthBonus, levelBonus, biome, name, offset);
    }),

    SEND_MESSAGE(dataObject -> {
        CommandSender target;

        if (dataObject.has("player")) {
            UUID playerUUID = UUID.fromString(dataObject.get("player").getAsString());
            target = Bukkit.getPlayer(playerUUID);
        } else if (dataObject.has("console")) {
            target = Bukkit.getConsoleSender();
        } else {
            target = null;
        }

        if (target == null)
            return;

        RegisteredServiceProvider<MessagesService> registeredServiceProvider = Bukkit.getServicesManager().getRegistration(MessagesService.class);

        if (registeredServiceProvider == null)
            return;

        SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

        MessagesService messagesService = registeredServiceProvider.getProvider();

        Locale targetLocale;

        if (target instanceof Player) {
            SuperiorPlayer superiorPlayer = module.getPlugin().getPlayers().getSuperiorPlayer((Player) target);
            targetLocale = superiorPlayer.getUserLocale();
        } else {
            String defaultLocale = module.getPlugin().getSettings().getDefaultLanguage();
            targetLocale = PlayerLocales.getLocale(defaultLocale);
        }

        IMessageComponent component = messagesService.getComponent(dataObject.get("type").getAsString(), targetLocale);

        JsonArray jsonArgs = dataObject.get("args").getAsJsonArray();

        List<Object> arguments = new ArrayList<>();

        for (JsonElement elementArgument : jsonArgs) {
            Object value = elementArgument.isJsonPrimitive() ?
                    JsonUtil.getValueFromPrimitive(elementArgument.getAsJsonPrimitive()) : null;
            if (value != null)
                arguments.add(value);
        }

        if (component != null) {
            MessagesSender.sendMessageSilenty(component, target, arguments.toArray(new Object[0]));
        } else {
            // We send the message raw to the player.
            String message = arguments.isEmpty() || arguments.get(0) == null ? null : arguments.get(0).toString();
            boolean translateColors = arguments.size() >= 2 && arguments.get(1) instanceof Boolean && (boolean) arguments.get(1);

            MessagesService.Builder messageBuilder = messagesService.newBuilder();
            messageBuilder.addRawMessage(translateColors ? Text.colorize(message) : message);

            MessagesSender.sendMessageSilenty(messageBuilder.build(), target);
        }
    }),

    WARP_PLAYER(dataObject -> requirePlayer(dataObject, player -> {
        SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

        SuperiorPlayer superiorPlayer = module.getPlugin().getPlayers().getSuperiorPlayer(player);

        UUID islandUUID = UUID.fromString(dataObject.get("island").getAsString());

        Island targetIsland = islandUUID.equals(new UUID(0, 0)) ?
                module.getPlugin().getGrid().getSpawnIsland() :
                module.getPlugin().getGrid().getIslandByUUID(islandUUID);

        if (targetIsland == null)
            throw new RequestHandlerException("Couldn't teleport player to invalid island \"" + islandUUID + "\"");

        // We want to ignore warmup.
        RemoteSuperiorPlayer remoteSuperiorPlayer = new RemoteSuperiorPlayer(superiorPlayer);
        try {
            remoteSuperiorPlayer.setFakeBypassMode(true);
            targetIsland.warpPlayer(remoteSuperiorPlayer, dataObject.get("warp_name").getAsString());
        } finally {
            remoteSuperiorPlayer.setFakeBypassMode(false);
        }

    }, true)),

    CALCULATE_ISLAND(dataObject -> {
        SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

        UUID islandUUID = UUID.fromString(dataObject.get("island").getAsString());
        Island island = module.getPlugin().getGrid().getIslandByUUID(islandUUID);

        if (island == null)
            throw new RequestHandlerException("Couldn't teleport player to invalid island \"" + islandUUID + "\"");

        int responseId = dataObject.get("response-id").getAsInt();

        island.getCalculationAlgorithm().calculateIsland(island).whenComplete((result, error) -> {
            JsonObject response = new JsonObject();
            response.addProperty("id", responseId);

            if (error != null) {
                response.addProperty("error", error.getMessage());
            } else {
                JsonArray blockCounts = new JsonArray();
                response.add("block_counts", blockCounts);
                result.getBlockCounts().forEach((block, count) -> {
                    JsonObject blockCount = new JsonObject();
                    blockCount.addProperty("block", block.toString());
                    blockCount.addProperty("count", count);
                    blockCounts.add(blockCount);
                });
            }

            ServerActions.sendCalculationResult(response);
        });
    });

    private final IRequestHandler requestHandler;

    ActionType(IRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    public IRequestHandler getHandler() {
        return requestHandler;
    }

    private static void requirePlayer(JsonObject data, RequestHandlerConsumer<Player> consumer, boolean addAsPendingTeleport) throws RequestHandlerException {
        UUID playerUUID = UUID.fromString(data.get("player").getAsString());
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            consumer.accept(player);
        } else {
            if (addAsPendingTeleport) {
                ProxyPlayerTeleportAlgorithm playerTeleportAlgorithm = ProxyPlayersFactory.getInstance()
                        .getPlayerTeleportAlgorithm(playerUUID);

                if (playerTeleportAlgorithm != null) {
                    playerTeleportAlgorithm.setHasPendingTeleportTask(true);
                    ActionsQueue.getPlayersQueue().addAction(data, playerUUID, playerAction -> {
                        consumer.accept(playerAction);
                        BukkitExecutor.runTaskLater(() -> playerTeleportAlgorithm.setHasPendingTeleportTask(false), 1L);
                    });

                    return;
                }
            }

            ActionsQueue.getPlayersQueue().addAction(data, playerUUID, consumer);
        }
    }

}
