package com.bgsoftware.ssbproxybridge.bukkit.action;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.island.algorithm.RemoteIslandCreationAlgorithm;
import com.bgsoftware.ssbproxybridge.bukkit.player.RemoteSuperiorPlayer;
import com.bgsoftware.ssbproxybridge.bukkit.teleport.ProxyPlayerTeleportAlgorithm;
import com.bgsoftware.ssbproxybridge.bukkit.teleport.ProxyPlayersFactory;
import com.bgsoftware.ssbproxybridge.bukkit.utils.BukkitExecutor;
import com.bgsoftware.ssbproxybridge.bukkit.utils.Consts;
import com.bgsoftware.ssbproxybridge.bukkit.utils.MessagesSender;
import com.bgsoftware.ssbproxybridge.bukkit.utils.PlayerLocales;
import com.bgsoftware.ssbproxybridge.bukkit.utils.Serializers;
import com.bgsoftware.ssbproxybridge.bukkit.utils.Text;
import com.bgsoftware.ssbproxybridge.core.bundle.Bundle;
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

    TELEPORT() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle, (unused, player) -> {
                SuperiorPlayer superiorPlayer = module.getPlugin().getPlayers().getSuperiorPlayer(player);

                if (bundle.contains(Consts.Action.Teleport.ISLAND)) {
                    UUID islandUUID = bundle.getUUID(Consts.Action.Teleport.ISLAND);
                    Island targetIsland = islandUUID.equals(SPAWN_UUID) ?
                            module.getPlugin().getGrid().getSpawnIsland() :
                            module.getPlugin().getGrid().getIslandByUUID(islandUUID);

                    if (targetIsland == null) {
                        throw new RequestHandlerException("Couldn't teleport player to invalid island \"" + islandUUID + "\"");
                    }

                    superiorPlayer.teleport(targetIsland);
                } else if (bundle.contains(Consts.Action.Teleport.LOCATION)) {
                    Location location = Serializers.deserializeLocation(bundle.getExtra(Consts.Action.Teleport.LOCATION));

                    if (location.getWorld() == null)
                        throw new RequestHandlerException("Couldn't teleport player to invalid location \"" + location + "\"");

                    superiorPlayer.teleport(location);
                }

            });
        }
    },

    CREATE_ISLAND() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            UUID islandUUID = bundle.getUUID(Consts.Action.CreateIsland.UUID);
            SuperiorPlayer islandLeader = SuperiorSkyblockAPI.getPlayer(bundle.getUUID(Consts.Action.CreateIsland.LEADER));

            Bundle position = bundle.getExtra(Consts.Action.CreateIsland.POSITION);

            BlockPosition blockPosition = module.getPlugin().getFactory().createBlockPosition(
                    position.getString("world"),
                    position.getInt("x"),
                    position.getInt("y"),
                    position.getInt("z")
            );

            String name = bundle.getString(Consts.Action.CreateIsland.NAME);
            String schematic = bundle.getString(Consts.Action.CreateIsland.SCHEMATIC);
            BigDecimal worthBonus = bundle.getBigDecimal(Consts.Action.CreateIsland.WORTH_BONUS);
            BigDecimal levelBonus = bundle.getBigDecimal(Consts.Action.CreateIsland.LEVELS_BONUS);

            int responseId = bundle.getInt(Consts.Action.RESPONSE_ID);

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
        }
    },

    SEND_MESSAGE() {
        @Override
        public void onReceive(Bundle bundle) {
            CommandSender target;

            if (bundle.contains(Consts.Action.SendMessage.PLAYER)) {
                UUID playerUUID = bundle.getUUID(Consts.Action.SendMessage.PLAYER);
                target = Bukkit.getPlayer(playerUUID);
            } else if (bundle.contains(Consts.Action.SendMessage.CONSOLE)) {
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

            IMessageComponent component = messagesService.getComponent(
                    bundle.getString(Consts.Action.SendMessage.TYPE), targetLocale);

            List<Object> args = bundle.getList(Consts.Action.SendMessage.ARGS);

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
        }
    },

    WARP_PLAYER() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle, (unused, player) -> {
                SuperiorPlayer superiorPlayer = module.getPlugin().getPlayers().getSuperiorPlayer(player);

                UUID islandUUID = bundle.getUUID(Consts.Action.WarpPlayer.ISLAND);

                Island targetIsland = islandUUID.equals(SPAWN_UUID) ?
                        module.getPlugin().getGrid().getSpawnIsland() :
                        module.getPlugin().getGrid().getIslandByUUID(islandUUID);

                if (targetIsland == null)
                    throw new RequestHandlerException("Couldn't teleport player to invalid island \"" + islandUUID + "\"");

                // We want to ignore warmup.
                RemoteSuperiorPlayer remoteSuperiorPlayer = new RemoteSuperiorPlayer(superiorPlayer);
                try {
                    remoteSuperiorPlayer.setFakeBypassMode(true);
                    targetIsland.warpPlayer(remoteSuperiorPlayer, bundle.getString(Consts.Action.WarpPlayer.WARP_NAME));
                } finally {
                    remoteSuperiorPlayer.setFakeBypassMode(false);
                }

            });
        }
    },

    CALCULATE_ISLAND() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            UUID islandUUID = bundle.getUUID(Consts.Action.CalculateIsland.ISLAND);
            Island island = module.getPlugin().getGrid().getIslandByUUID(islandUUID);

            if (island == null)
                throw new RequestHandlerException("Couldn't teleport player to invalid island \"" + islandUUID + "\"");

            int responseId = bundle.getInt(Consts.Action.RESPONSE_ID);

            island.getCalculationAlgorithm().calculateIsland(island).whenComplete((result, error) -> {
                Bundle response = new Bundle();
                response.setInt(Consts.Action.RESPONSE_ID, responseId);

                if (error != null) {
                    response.setString(Consts.Action.CalculateIsland.Response.ERROR, error.getMessage());
                } else {
                    List<Bundle> blockCounts = new LinkedList<>();
                    result.getBlockCounts().forEach((block, count) -> {
                        Bundle blockCount = new Bundle();
                        blockCount.setString(Consts.Action.CalculateIsland.BlockCount.BLOCK, block.toString());
                        blockCount.setBigInteger(Consts.Action.CalculateIsland.BlockCount.COUNT, count);
                        blockCounts.add(blockCount);
                    });
                    response.setList(Consts.Action.CalculateIsland.Response.RESULT, blockCounts);
                }


                ServerActions.sendCalculationResult(response);
            });
        }
    },

    SET_BIOME() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            UUID islandUUID = bundle.getUUID(Consts.Action.SetBiome.ISLAND);
            Island island = module.getPlugin().getGrid().getIslandByUUID(islandUUID);

            if (island == null)
                throw new RequestHandlerException("Couldn't teleport player to invalid island \"" + islandUUID + "\"");

            Biome biome = bundle.getEnum(Consts.Action.SetBiome.BIOME, Biome.class);
            boolean updateBlocks = bundle.getBoolean(Consts.Action.SetBiome.UPDATE_BLOCKS);

            island.setBiome(biome, updateBlocks);
        }
    };

    private static final SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();
    private static final UUID SPAWN_UUID = new UUID(0, 0);

    ActionType() {
    }

    public void onReceive(Bundle bundle) throws RequestHandlerException {
        throw new UnsupportedOperationException("Must be overridden.");
    }

    private static void requirePlayer(Bundle bundle, RequestHandlerConsumer<Player> consumer) throws RequestHandlerException {
        UUID playerUUID = bundle.getUUID(Consts.Action.PLAYER);
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            consumer.accept(bundle, player);
        } else {
            ProxyPlayerTeleportAlgorithm playerTeleportAlgorithm = ProxyPlayersFactory.getInstance()
                    .getPlayerTeleportAlgorithm(playerUUID);

            if (playerTeleportAlgorithm == null) {
                ActionsQueue.getPlayersQueue().addAction(bundle, playerUUID, consumer);
                return;
            }

            playerTeleportAlgorithm.setHasPendingTeleportTask(true);
            ActionsQueue.getPlayersQueue().addAction(bundle, playerUUID, (actionBundle, playerAction) -> {
                consumer.accept(actionBundle, playerAction);
                BukkitExecutor.runTaskLater(() -> playerTeleportAlgorithm.setHasPendingTeleportTask(false), 1L);
            });
        }
    }

}
