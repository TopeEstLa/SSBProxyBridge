package com.bgsoftware.ssbproxybridge.bukkit.action;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.island.algorithm.RemoteIslandCreationAlgorithm;
import com.bgsoftware.ssbproxybridge.bukkit.player.RemoteSuperiorPlayer;
import com.bgsoftware.ssbproxybridge.bukkit.teleport.ProxyPlayerTeleportAlgorithm;
import com.bgsoftware.ssbproxybridge.bukkit.teleport.ProxyPlayersFactory;
import com.bgsoftware.ssbproxybridge.bukkit.utils.BukkitExecutor;
import com.bgsoftware.ssbproxybridge.bukkit.utils.LazyWorldLocation;
import com.bgsoftware.ssbproxybridge.bukkit.utils.MessagesSender;
import com.bgsoftware.ssbproxybridge.bukkit.utils.PlayerLocales;
import com.bgsoftware.ssbproxybridge.bukkit.utils.Text;
import com.bgsoftware.ssbproxybridge.core.bundle.Bundle;
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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public enum ActionType {

    TELEPORT(bundle -> requirePlayer(bundle, (unused, player) -> {
        SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

        SuperiorPlayer superiorPlayer = module.getPlugin().getPlayers().getSuperiorPlayer(player);

        if (bundle.contains("island")) {
            UUID islandUUID = bundle.getUUID("island");
            Island targetIsland = islandUUID.equals(new UUID(0, 0)) ?
                    module.getPlugin().getGrid().getSpawnIsland() :
                    module.getPlugin().getGrid().getIslandByUUID(islandUUID);

            if (targetIsland == null) {
                throw new RequestHandlerException("Couldn't teleport player to invalid island \"" + islandUUID + "\"");
            }

            superiorPlayer.teleport(targetIsland);
        } else if (bundle.contains("location")) {
            Bundle position = bundle.getExtra("location");
            Location location = new LazyWorldLocation(
                    position.getString("world"),
                    position.getDouble("x"),
                    position.getDouble("y"),
                    position.getDouble("z"),
                    position.getFloat("yaw"),
                    position.getFloat("pitch")
            );

            if (location.getWorld() == null)
                throw new RequestHandlerException("Couldn't teleport player to invalid location \"" + location + "\"");

            superiorPlayer.teleport(location);
        }

    }, true)),

    CREATE_ISLAND(bundle -> {
        SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

        UUID islandUUID = bundle.getUUID("uuid");
        SuperiorPlayer islandLeader = SuperiorSkyblockAPI.getPlayer(bundle.getUUID("leader"));

        Bundle position = bundle.getExtra("position");

        BlockPosition blockPosition = module.getPlugin().getFactory().createBlockPosition(
                position.getString("world"),
                position.getInt("x"),
                position.getInt("y"),
                position.getInt("z")
        );

        String name = bundle.getString("name");
        String schematic = bundle.getString("schematic");
        BigDecimal worthBonus = bundle.getBigDecimal("worth_bonus");
        BigDecimal levelBonus = bundle.getBigDecimal("level_bonus");

        int responseId = bundle.getInt("response-id");

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

    SEND_MESSAGE(bundle -> {
        CommandSender target;

        if (bundle.contains("player")) {
            UUID playerUUID = bundle.getUUID("player");
            target = Bukkit.getPlayer(playerUUID);
        } else if (bundle.contains("console")) {
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

        IMessageComponent component = messagesService.getComponent(bundle.getString("type"), targetLocale);

        List<Object> args = bundle.getList("args");

        if (component != null) {
            MessagesSender.sendMessageSilenty(component, target, args.toArray(new Object[0]));
        } else {
            // We send the message raw to the player.
            String message = args.isEmpty() || args.get(0) == null ? null : args.get(0).toString();
            boolean translateColors = args.size() >= 2 && args.get(1) instanceof Boolean && (boolean) args.get(1);

            MessagesService.Builder messageBuilder = messagesService.newBuilder();
            messageBuilder.addRawMessage(translateColors ? Text.colorize(message) : message);

            MessagesSender.sendMessageSilenty(messageBuilder.build(), target);
        }
    }),

    WARP_PLAYER(bundle -> requirePlayer(bundle, (unused, player) -> {
        SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

        SuperiorPlayer superiorPlayer = module.getPlugin().getPlayers().getSuperiorPlayer(player);

        UUID islandUUID = bundle.getUUID("island");

        Island targetIsland = islandUUID.equals(new UUID(0, 0)) ?
                module.getPlugin().getGrid().getSpawnIsland() :
                module.getPlugin().getGrid().getIslandByUUID(islandUUID);

        if (targetIsland == null)
            throw new RequestHandlerException("Couldn't teleport player to invalid island \"" + islandUUID + "\"");

        // We want to ignore warmup.
        RemoteSuperiorPlayer remoteSuperiorPlayer = new RemoteSuperiorPlayer(superiorPlayer);
        try {
            remoteSuperiorPlayer.setFakeBypassMode(true);
            targetIsland.warpPlayer(remoteSuperiorPlayer, bundle.getString("warp_name"));
        } finally {
            remoteSuperiorPlayer.setFakeBypassMode(false);
        }

    }, true)),

    CALCULATE_ISLAND(bundle -> {
        SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

        UUID islandUUID = bundle.getUUID("island");
        Island island = module.getPlugin().getGrid().getIslandByUUID(islandUUID);

        if (island == null)
            throw new RequestHandlerException("Couldn't teleport player to invalid island \"" + islandUUID + "\"");

        int responseId = bundle.getInt("response-id");

        island.getCalculationAlgorithm().calculateIsland(island).whenComplete((result, error) -> {
            Bundle response = new Bundle();
            response.setInt("id", responseId);

            if (error != null) {
                response.setString("error", error.getMessage());
            } else {
                List<Bundle> blockCounts = new LinkedList<>();
                result.getBlockCounts().forEach((block, count) -> {
                    Bundle blockCount = new Bundle();
                    blockCount.setString("block", block.toString());
                    blockCount.setBigInteger("count", count);
                    blockCounts.add(blockCount);
                });
                response.setList("block_counts", blockCounts);
            }


            ServerActions.sendCalculationResult(response);
        });
    }),

    SET_BIOME(bundle -> {
        SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

        UUID islandUUID = bundle.getUUID("island");
        Island island = module.getPlugin().getGrid().getIslandByUUID(islandUUID);

        if (island == null)
            throw new RequestHandlerException("Couldn't teleport player to invalid island \"" + islandUUID + "\"");

        Biome biome = bundle.getEnum("biome", Biome.class);
        boolean updateBlocks = bundle.getBoolean("update_blocks");

        island.setBiome(biome, updateBlocks);
    });

    private final IRequestHandler requestHandler;

    ActionType(IRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    public IRequestHandler getHandler() {
        return requestHandler;
    }

    private static void requirePlayer(Bundle bundle, RequestHandlerConsumer<Player> consumer, boolean addAsPendingTeleport) throws RequestHandlerException {
        UUID playerUUID = bundle.getUUID("player");
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            consumer.accept(bundle, player);
        } else {
            if (addAsPendingTeleport) {
                ProxyPlayerTeleportAlgorithm playerTeleportAlgorithm = ProxyPlayersFactory.getInstance()
                        .getPlayerTeleportAlgorithm(playerUUID);

                if (playerTeleportAlgorithm != null) {
                    playerTeleportAlgorithm.setHasPendingTeleportTask(true);
                    ActionsQueue.getPlayersQueue().addAction(bundle, playerUUID, (actionBundle, playerAction) -> {
                        consumer.accept(actionBundle, playerAction);
                        BukkitExecutor.runTaskLater(() -> playerTeleportAlgorithm.setHasPendingTeleportTask(false), 1L);
                    });

                    return;
                }
            }

            ActionsQueue.getPlayersQueue().addAction(bundle, playerUUID, consumer);
        }
    }

}
