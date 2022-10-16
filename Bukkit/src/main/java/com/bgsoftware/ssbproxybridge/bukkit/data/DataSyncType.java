package com.bgsoftware.ssbproxybridge.bukkit.data;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.bridge.ProxyDatabaseBridge;
import com.bgsoftware.ssbproxybridge.bukkit.bridge.ProxyDatabaseBridgeFactory;
import com.bgsoftware.ssbproxybridge.bukkit.island.FakeSchematic;
import com.bgsoftware.ssbproxybridge.bukkit.island.Islands;
import com.bgsoftware.ssbproxybridge.bukkit.island.RemoteIsland;
import com.bgsoftware.ssbproxybridge.bukkit.island.RemoteIslandsFactory;
import com.bgsoftware.ssbproxybridge.bukkit.island.algorithm.RemoteIslandCreationAlgorithm;
import com.bgsoftware.ssbproxybridge.bukkit.player.RemoteSuperiorPlayer;
import com.bgsoftware.ssbproxybridge.bukkit.utils.BukkitExecutor;
import com.bgsoftware.ssbproxybridge.bukkit.utils.Consts;
import com.bgsoftware.ssbproxybridge.bukkit.utils.DatabaseBridgeAccessor;
import com.bgsoftware.ssbproxybridge.bukkit.utils.Serializers;
import com.bgsoftware.ssbproxybridge.core.bundle.Bundle;
import com.bgsoftware.ssbproxybridge.core.requests.RequestHandlerAction;
import com.bgsoftware.ssbproxybridge.core.requests.RequestHandlerConsumer;
import com.bgsoftware.ssbproxybridge.core.requests.RequestHandlerException;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.enums.BankAction;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.factory.IslandsFactory;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.world.algorithm.IslandCreationAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Locale;
import java.util.UUID;

public enum DataSyncType {

    /* Delete Operations */

    DELETE_GRID(false),

    DELETE_ISLANDS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            optionalIsland(bundle.getExtra("filters"), (filters, island) -> {
                if (island instanceof RemoteIsland) {
                    ((RemoteIsland) island).removeIsland();
                } else {
                    BukkitExecutor.runTask(() -> DatabaseBridgeAccessor.runWithoutDataSave(island, (Runnable) island::disbandIsland));
                }
            });
        }
    },

    DELETE_ISLANDS_BANKS(false),

    DELETE_ISLANDS_BANS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            optionalIsland(bundle.getExtra("filters"), (filters, island) ->
                    island.unbanMember(SuperiorSkyblockAPI.getPlayer(filters.getUUID(Consts.Island.Banned.PLAYER))));
        }
    },

    DELETE_ISLANDS_BLOCK_LIMITS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            optionalIsland(bundle.getExtra("filters"), (filters, island) -> {
                if (filters.contains(Consts.Island.BlockLimit.BLOCK)) {
                    island.removeBlockLimit(Key.of(filters.getString(Consts.Island.BlockLimit.BLOCK)));
                } else {
                    island.clearBlockLimits();
                }
            });
        }
    },

    DELETE_ISLANDS_CHESTS(false),

    DELETE_ISLANDS_CUSTOM_DATA(false),

    DELETE_ISLANDS_EFFECTS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            optionalIsland(bundle.getExtra("filters"), (filters, island) -> {
                if (filters.contains(Consts.Island.IslandEffect.NAME)) {
                    island.removePotionEffect(PotionEffectType.getByName(filters.getString(Consts.Island.IslandEffect.NAME)));
                } else {
                    island.clearEffects();
                }
            });
        }
    },

    DELETE_ISLANDS_ENTITY_LIMITS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            optionalIsland(bundle.getExtra("filters"), (filters, island) -> island.clearEntitiesLimits());
        }
    },

    DELETE_ISLANDS_FLAGS(false),

    DELETE_ISLANDS_GENERATORS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            optionalIsland(bundle.getExtra("filters"), (filters, island) -> {
                if (filters.contains(Consts.Island.GeneratorRate.ENVIRONMENT)) {
                    World.Environment environment = filters.getEnum(Consts.Island.GeneratorRate.ENVIRONMENT, World.Environment.class);
                    if (filters.contains(Consts.Island.GeneratorRate.BlockRate.BLOCK)) {
                        island.removeGeneratorAmount(Key.of(filters.getString(Consts.Island.GeneratorRate.BlockRate.BLOCK)), environment);
                    } else {
                        island.clearGeneratorAmounts(environment);
                    }
                }
            });
        }
    },

    DELETE_ISLANDS_HOMES() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            optionalIsland(bundle.getExtra("filters"), (filters, island) ->
                    island.setIslandHome(filters.getEnum(Consts.Island.IslandHome.ENVIRONMENT, World.Environment.class), null));
        }
    },

    DELETE_ISLANDS_ISLAND_EFFECTS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            optionalIsland(bundle.getExtra("filters"), (filters, island) ->
                    island.removePotionEffect(PotionEffectType.getByName(filters.getString(Consts.Island.IslandEffect.NAME))));
        }
    },

    DELETE_ISLANDS_MEMBERS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            optionalIsland(bundle.getExtra("filters"), (filters, island) -> {
                SuperiorPlayer islandMember = SuperiorSkyblockAPI.getPlayer(filters.getUUID(Consts.Island.Member.PLAYER));
                DatabaseBridgeAccessor.runWithoutDataSave(islandMember, (Runnable) () -> island.kickMember(islandMember));
            });
        }
    },

    DELETE_ISLANDS_MISSIONS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            optionalIsland(bundle.getExtra("filters"), (filters, island) -> {
                String missionName = filters.getString(Consts.Mission.NAME);
                Mission<?> mission = SuperiorSkyblockAPI.getMissions().getMission(missionName);

                if (mission == null)
                    throw new RequestHandlerException("Cannot find a valid mission \"" + missionName + "\"");

                island.setAmountMissionCompleted(mission, 0);
            });
        }
    },

    DELETE_ISLANDS_PLAYER_PERMISSIONS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            optionalIsland(bundle.getExtra("filters"), (filters, island) ->
                    island.resetPermissions(SuperiorSkyblockAPI.getPlayer(filters.getUUID(Consts.Island.PlayerPermission.PLAYER))));
        }
    },

    DELETE_ISLANDS_RATINGS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            optionalIsland(bundle.getExtra("filters"), (filters, island) -> {
                if (filters.contains(Consts.Island.Rating.PLAYER)) {
                    island.removeRating(SuperiorSkyblockAPI.getPlayer(filters.getUUID(Consts.Island.Rating.PLAYER)));
                } else {
                    island.removeRatings();
                }
            });
        }
    },

    DELETE_ISLANDS_ROLE_LIMITS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            optionalIsland(bundle.getExtra("filters"), (filters, island) ->
                    island.removeRoleLimit(SuperiorSkyblockAPI.getRoles().getPlayerRole(bundle.getInt(Consts.Island.RoleLimit.ROLE))));
        }
    },

    DELETE_ISLANDS_ROLE_PERMISSIONS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            optionalIsland(bundle.getExtra("filters"), (filters, island) -> island.resetPermissions());
        }
    },

    DELETE_ISLANDS_SETTINGS(false),

    DELETE_ISLANDS_UPGRADES(false),

    DELETE_ISLANDS_VISITORS(false),

    DELETE_ISLANDS_VISITOR_HOMES() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            optionalIsland(bundle.getExtra("filters"), (filters, island) -> island.setVisitorsLocation(null));
        }
    },

    DELETE_ISLANDS_WARPS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            optionalIsland(bundle.getExtra("filters"), (filters, island) ->
                    island.deleteWarp(filters.getString(Consts.Island.Warp.NAME)));
        }
    },

    DELETE_ISLANDS_WARP_CATEGORIES() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            optionalIsland(bundle.getExtra("filters"), (filters, island) ->
                    island.deleteCategory(island.getWarpCategory(filters.getString(Consts.Island.WarpCategory.NAME))));
        }
    },

    DELETE_PLAYERS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle.getExtra("filters"), (filters, superiorPlayer) ->
                    SuperiorSkyblockAPI.getPlayers().getPlayersContainer().removePlayer(superiorPlayer));
        }
    },

    DELETE_PLAYERS_CUSTOM_DATA(false),

    DELETE_PLAYERS_MISSIONS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle.getExtra("filters"), (filters, superiorPlayer) -> {
                String missionName = bundle.getString(Consts.Mission.NAME);
                Mission<?> mission = SuperiorSkyblockAPI.getMissions().getMission(missionName);

                if (mission == null)
                    throw new RequestHandlerException("Cannot find a valid mission \"" + missionName + "\"");

                superiorPlayer.setAmountMissionCompleted(mission, 0);
            });
        }
    },

    DELETE_PLAYERS_SETTINGS(false),

    DELETE_STACKED_BLOCKS(false),

    /* Insert Operations */

    INSERT_BANK_TRANSACTIONS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> {
                island.getIslandBank().loadTransaction(module.getPlugin().getFactory().createTransaction(
                        columns.getUUID(Consts.Island.BankTransaction.PLAYER),
                        columns.getEnum(Consts.Island.BankTransaction.BANK_ACTION, BankAction.class),
                        columns.getInt(Consts.Island.BankTransaction.POSITION),
                        columns.getLong(Consts.Island.BankTransaction.TIME),
                        columns.getString(Consts.Island.BankTransaction.FAILURE_REASON),
                        columns.getBigDecimal(Consts.Island.BankTransaction.AMOUNT)
                ));
            });
        }
    },

    INSERT_GRID(false),

    INSERT_ISLANDS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            Bundle columns = bundle.getExtra("columns");
            UUID islandUUID = columns.getUUID(Consts.Island.UUID);

            Island island = SuperiorSkyblockAPI.getIslandByUUID(islandUUID);

            if (island != null) // Only if the island doesn't exist already we try to insert it again.
                return;

            String center = columns.getString(Consts.Island.CENTER);
            SuperiorPlayer islandLeader = SuperiorSkyblockAPI.getPlayer(columns.getUUID(Consts.Island.OWNER));

            islandLeader.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.IDLE);

            // We create our RemoteIsland
            IslandCreationAlgorithm islandCreationAlgorithm = SuperiorSkyblockAPI.getGrid().getIslandCreationAlgorithm();
            if (islandCreationAlgorithm instanceof RemoteIslandCreationAlgorithm)
                islandCreationAlgorithm = ((RemoteIslandCreationAlgorithm) islandCreationAlgorithm).getOriginal();

            IslandsFactory originalIslandsFactory = SuperiorSkyblockAPI.getFactory().getIslandsFactory();
            SuperiorSkyblockAPI.getFactory().registerIslandsFactory(new RemoteIslandsFactory(originalIslandsFactory, islandUUID));

            islandCreationAlgorithm.createIsland(
                    islandUUID,
                    islandLeader,
                    SuperiorSkyblockAPI.getFactory().createBlockPosition(SuperiorSkyblockAPI.getGrid().getLastIslandLocation()),
                    columns.getString(Consts.Island.NAME),
                    new FakeSchematic(columns.getString(Consts.Island.ISLAND_TYPE))
            ).whenComplete((result, error) -> {
                SuperiorSkyblockAPI.getFactory().registerIslandsFactory(originalIslandsFactory);

                boolean createdSuccessfully = false;

                try {
                    if (error != null) {
                        error.printStackTrace();
                    } else {
                        createdSuccessfully = islandCreationCallback(result, bundle.getSender(), center);
                    }
                } catch (Throwable callbackError) {
                    callbackError.printStackTrace();
                } finally {
                    // We need to make sure the leader isn't part of the island anymore
                    if (!createdSuccessfully) {
                        islandLeader.setIsland(null);
                    }

                    islandLeader.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.SAVE_DATA);
                }
            });
        }
    },

    INSERT_ISLANDS_BANKS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> {
                island.getIslandBank().setBalance(columns.getBigDecimal(Consts.Island.BALANCE));
                island.setLastInterestTime(columns.getLong(Consts.Island.LAST_INTEREST_TIME));
            });
        }
    },

    INSERT_ISLANDS_BANS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> island.banMember(
                    SuperiorSkyblockAPI.getPlayer(columns.getUUID(Consts.Island.Banned.PLAYER)),
                    SuperiorSkyblockAPI.getPlayer(columns.getUUID(Consts.Island.Banned.BANNED_BY))
            ));
        }
    },

    INSERT_ISLANDS_BLOCK_LIMITS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setBlockLimit(
                    Key.of(columns.getString(Consts.Island.BlockLimit.BLOCK)),
                    columns.getInt(Consts.Island.BlockLimit.LIMIT)
            ));
        }
    },

    /* We do not update chests */
    INSERT_ISLANDS_CHESTS(false),

    INSERT_ISLANDS_CUSTOM_DATA() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> {
                byte[] data = columns.getString(Consts.Island.PERSISTENT_DATA).getBytes(StandardCharsets.UTF_8);
                island.getPersistentDataContainer().load(data);
            });
        }
    },

    INSERT_ISLANDS_EFFECTS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setPotionEffect(
                    PotionEffectType.getByName(columns.getString(Consts.Island.IslandEffect.NAME)),
                    columns.getInt(Consts.Island.IslandEffect.LEVEL)
            ));
        }
    },

    INSERT_ISLANDS_ENTITY_LIMITS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setEntityLimit(
                    Key.of(columns.getString(Consts.Island.EntityLimit.ENTITY)),
                    columns.getInt(Consts.Island.EntityLimit.LIMIT)
            ));
        }
    },

    INSERT_ISLANDS_FLAGS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> {
                IslandFlag islandFlag = IslandFlag.getByName(columns.getString(Consts.Island.IslandFlag.NAME));
                if (columns.getInt(Consts.Island.IslandFlag.STATUS) == 1) {
                    island.enableSettings(islandFlag);
                } else {
                    island.disableSettings(islandFlag);
                }
            });
        }
    },

    INSERT_ISLANDS_GENERATORS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setGeneratorAmount(
                    Key.of(columns.getString(Consts.Island.GeneratorRate.BlockRate.BLOCK)),
                    columns.getInt(Consts.Island.GeneratorRate.BlockRate.RATE),
                    columns.getEnum(Consts.Island.GeneratorRate.ENVIRONMENT, World.Environment.class)
            ));
        }
    },

    INSERT_ISLANDS_HOMES() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setIslandHome(
                    columns.getEnum(Consts.Island.IslandHome.ENVIRONMENT, World.Environment.class),
                    Serializers.deserializeLocation(columns.getString(Consts.Island.IslandHome.LOCATION))
            ));
        }
    },

    INSERT_ISLANDS_MEMBERS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> island.addMember(
                    SuperiorSkyblockAPI.getPlayer(columns.getUUID(Consts.Island.Member.PLAYER)),
                    SuperiorSkyblockAPI.getRoles().getPlayerRole(columns.getInt(Consts.Island.Member.ROLE))
            ));
        }
    },

    INSERT_ISLANDS_MISSIONS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> {
                String missionName = columns.getString(Consts.Mission.NAME);
                Mission<?> mission = SuperiorSkyblockAPI.getMissions().getMission(missionName);

                if (mission == null)
                    throw new RequestHandlerException("Cannot find a valid mission \"" + missionName + "\"");

                island.setAmountMissionCompleted(mission, columns.getInt(Consts.Mission.FINISH_COUNT));
            });
        }
    },

    INSERT_ISLANDS_PLAYER_PERMISSIONS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setPermission(
                    SuperiorSkyblockAPI.getPlayer(columns.getUUID(Consts.Island.PlayerPermission.PLAYER)),
                    IslandPrivilege.getByName(columns.getString(Consts.Island.PlayerPermission.Privilege.NAME)),
                    columns.getBoolean(Consts.Island.PlayerPermission.Privilege.STATUS)
            ));
        }
    },

    INSERT_ISLANDS_RATINGS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setRating(
                    SuperiorSkyblockAPI.getPlayer(columns.getUUID(Consts.Island.Rating.PLAYER)),
                    Rating.valueOf(columns.getInt(Consts.Island.Rating.RATING))
            ));
        }
    },

    INSERT_ISLANDS_ROLE_LIMITS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setRoleLimit(
                    SuperiorSkyblockAPI.getRoles().getPlayerRole(columns.getInt(Consts.Island.RoleLimit.ROLE)),
                    columns.getInt(Consts.Island.RoleLimit.LIMIT)
            ));
        }
    },

    INSERT_ISLANDS_ROLE_PERMISSIONS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setPermission(
                    SuperiorSkyblockAPI.getRoles().getPlayerRole(columns.getInt(Consts.Island.RolePermission.ROLE)),
                    IslandPrivilege.getByName(columns.getString(Consts.Island.RolePermission.PRIVILEGE))
            ));
        }
    },

    /* Do nothing, as upgrades will not be synced otherwise */
    INSERT_ISLANDS_SETTINGS(false),

    INSERT_ISLANDS_UPGRADES() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setUpgradeLevel(
                    SuperiorSkyblockAPI.getUpgrades().getUpgrade(columns.getString(Consts.Island.Upgrade.NAME)),
                    columns.getInt(Consts.Island.Upgrade.LEVEL)
            ));
        }
    },

    INSERT_ISLANDS_VISITOR_HOMES() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setVisitorsLocation(
                    Serializers.deserializeLocation(columns.getString(Consts.Island.VisitorHome.LOCATION))
            ));
        }
    },

    INSERT_ISLANDS_VISITORS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> {
                SuperiorPlayer islandVisitor = SuperiorSkyblockAPI.getPlayer(columns.getUUID(Consts.Island.Visitors.PLAYER));
                // We use a fake player so we can fake his online status
                RemoteSuperiorPlayer remoteSuperiorPlayer = new RemoteSuperiorPlayer(islandVisitor);
                remoteSuperiorPlayer.setOnlineStatus(true);
                island.setPlayerInside(remoteSuperiorPlayer, true);
                island.setPlayerInside(remoteSuperiorPlayer, false);
                remoteSuperiorPlayer.setOnlineStatus(false);
            });
        }
    },

    INSERT_ISLANDS_WARPS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> {
                IslandWarp islandWarp = island.createWarp(columns.getString(Consts.Island.Warp.NAME),
                        Serializers.deserializeLocation(columns.getString(Consts.Island.Warp.LOCATION)),
                        island.getWarpCategory(columns.getString(Consts.Island.Warp.CATEGORY))
                );
                islandWarp.setPrivateFlag(columns.getBoolean(Consts.Island.Warp.PRIVATE));
//            // TODO
//            islandWarp.setIcon(columns.get("private").getAsBoolean());
            });
        }
    },

    INSERT_ISLANDS_WARP_CATEGORIES() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("columns"), (columns, island) -> {
                WarpCategory warpCategory = island.createWarpCategory(columns.getString(Consts.Island.WarpCategory.NAME));
                warpCategory.setSlot(columns.getInt(Consts.Island.WarpCategory.SLOT));
//            // TODO
//            warpCategory.setIcon(columns.get("icon").getAsString());
            });
        }
    },

    INSERT_PLAYERS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            Bundle columns = bundle.getExtra("columns");
            UUID playerUUID = columns.getUUID(Consts.Player.UUID);

            // We want to create a new player, which is done by calling the getPlayer method.
            try {
                ProxyDatabaseBridgeFactory.getInstance().setCreateActivatedBridge(false);
                SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(playerUUID);

                RemoteSuperiorPlayer remoteSuperiorPlayer = new RemoteSuperiorPlayer(superiorPlayer);
                remoteSuperiorPlayer.setName(columns.getString(Consts.Player.LAST_USED_NAME));
                remoteSuperiorPlayer.setTextureValue(columns.getString(Consts.Player.LAST_USED_SKIN));
                remoteSuperiorPlayer.setDisbands(columns.getInt(Consts.Player.DISBANDS));

                ((ProxyDatabaseBridge) superiorPlayer.getDatabaseBridge()).activate();
            } finally {
                ProxyDatabaseBridgeFactory.getInstance().setCreateActivatedBridge(true);
            }
        }
    },

    INSERT_PLAYERS_CUSTOM_DATA() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle.getExtra("columns"), (columns, superiorPlayer) -> {
                byte[] data = columns.getString(Consts.Player.PERSISTENT_DATA).getBytes(StandardCharsets.UTF_8);
                superiorPlayer.getPersistentDataContainer().load(data);
            });
        }
    },

    INSERT_PLAYERS_MISSIONS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle.getExtra("columns"), (columns, superiorPlayer) -> {
                String missionName = columns.getString(Consts.Mission.NAME);
                Mission<?> mission = SuperiorSkyblockAPI.getMissions().getMission(missionName);

                if (mission == null)
                    throw new RequestHandlerException("Cannot find a valid mission \"" + missionName + "\"");

                superiorPlayer.setAmountMissionCompleted(mission, columns.getInt(Consts.Mission.FINISH_COUNT));
            });
        }
    },

    INSERT_PLAYERS_SETTINGS(false),

    INSERT_STACKED_BLOCKS(false),

    /* Updates Operations */

    /* Last islands are updated when new islands are created */
    UPDATE_GRID_LAST_ISLAND(false),

    UPDATE_ISLANDS_BANKS_BALANCE() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.getIslandBank().setBalance(columns.getBigDecimal(Consts.Island.BALANCE))
            );
        }
    },

    UPDATE_ISLANDS_BANKS_LAST_INTEREST_TIME() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.setLastInterestTime(columns.getLong(Consts.Island.LAST_INTEREST_TIME))
            );
        }
    },

    UPDATE_ISLANDS_BLOCK_COUNTS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) -> {
                island.clearBlockCounts();
                island.handleBlocksPlace(JsonMethods.parseBlockCounts(columns.getString(Consts.Island.BLOCK_COUNTS)));
            });
        }
    },

    UPDATE_ISLANDS_COOP_PLAYER() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.addCoop(SuperiorSkyblockAPI.getPlayer(columns.getUUID(Consts.Island.Coop.UUID)))
            );
        }
    },

    UPDATE_ISLANDS_DESCRIPTION() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.setDescription(columns.getString(Consts.Island.DESCRIPTION))
            );
        }
    },

    /* We do not care about dirty chunks */
    UPDATE_ISLANDS_DIRTY_CHUNKS(false),

    UPDATE_ISLANDS_DISCORD() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.setDiscord(columns.getString(Consts.Island.DISCORD))
            );
        }
    },

    UPDATE_ISLANDS_GENERATED_SCHEMATICS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    Islands.setGeneratedSchematics(island, columns.getInt(Consts.Island.GENERATED_SCHEMATICS))
            );
        }
    },

    UPDATE_ISLANDS_IGNORED() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.setIgnored(columns.getBoolean(Consts.Island.IGNORED))
            );
        }
    },

    UPDATE_ISLANDS_INVITE_PLAYER() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.inviteMember(SuperiorSkyblockAPI.getPlayer(columns.getUUID(Consts.Island.Invited.UUID)))
            );
        }
    },

    UPDATE_ISLANDS_LAST_TIME_UPDATED() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) -> {
                island.setLastTimeUpdate(columns.getLong(Consts.Island.LAST_TIME_UPDATED));
                module.getManager().updateIsland(island.getUniqueId());
            });
        }
    },

    UPDATE_ISLANDS_LEVELS_BONUS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.setBonusLevel(columns.getBigDecimal(Consts.Island.LEVELS_BONUS))
            );
        }
    },

    UPDATE_ISLANDS_LOCKED() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.setLocked(bundle.getBoolean(Consts.Island.LOCKED))
            );
        }
    },

    UPDATE_ISLANDS_MEMBERS_ROLE() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                    superiorPlayer.setPlayerRole(SuperiorSkyblockAPI.getRoles().getPlayerRole(columns.getInt(Consts.Island.Member.ROLE)))
            );
        }
    },

    UPDATE_ISLANDS_NAME() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.setName(columns.getString(Consts.Island.NAME))
            );
        }
    },

    UPDATE_ISLANDS_OWNER() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.transferIsland(SuperiorSkyblockAPI.getPlayer(columns.getUUID(Consts.Island.OWNER)))
            );
        }
    },

    UPDATE_ISLANDS_PAYPAL() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.setPaypal(bundle.getString(Consts.Island.PAYPAL))
            );
        }
    },


    UPDATE_ISLANDS_SETTINGS_BANK_LIMIT() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.setBankLimit(bundle.getBigDecimal(Consts.Island.BANK_LIMIT))
            );
        }
    },

    UPDATE_ISLANDS_SETTINGS_COOPS_LIMIT() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.setCoopLimit(columns.getInt(Consts.Island.COOPS_LIMIT))
            );
        }
    },

    UPDATE_ISLANDS_SETTINGS_CROP_GROWTH_MULTIPLIER() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.setCropGrowthMultiplier(columns.getDouble(Consts.Island.CROP_GROWTH))
            );
        }
    },

    UPDATE_ISLANDS_SETTINGS_MEMBERS_LIMIT() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.setTeamLimit(columns.getInt(Consts.Island.MEMBERS_LIMIT))
            );
        }
    },

    UPDATE_ISLANDS_SETTINGS_MOB_DROPS_MULTIPLIER() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.setMobDropsMultiplier(columns.getDouble(Consts.Island.MOB_DROPS))
            );
        }
    },

    UPDATE_ISLANDS_SETTINGS_SIZE() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.setIslandSize(bundle.getInt(Consts.Island.SIZE))
            );
        }
    },

    UPDATE_ISLANDS_SETTINGS_SPAWNER_RATES_MULTIPLIER() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.setSpawnerRatesMultiplier(columns.getDouble(Consts.Island.SPAWNER_RATES))
            );
        }
    },

    UPDATE_ISLANDS_SETTINGS_WARPS_LIMIT() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.setWarpsLimit(columns.getInt(Consts.Island.WARPS_LIMIT))
            );
        }
    },

    UPDATE_ISLANDS_UNCOOP_PLAYER() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.removeCoop(SuperiorSkyblockAPI.getPlayer(columns.getUUID(Consts.Island.Coop.UUID)))
            );
        }
    },

    UPDATE_ISLANDS_UNLOCKED_WORLDS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    Islands.setUnlockedWorlds(island, columns.getInt(Consts.Island.UNLOCKED_WORLDS))
            );
        }
    },

    UPDATE_ISLANDS_WARPS_ICON() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            Bundle filters = bundle.getExtra("filters");
            requireIsland(filters, bundle.getExtra("columns"), (columns, island) -> {
                String islandWarpName = filters.getString(Consts.Island.Warp.NAME);
                IslandWarp islandWarp = island.getWarp(islandWarpName);

                if (islandWarp == null)
                    throw new RequestHandlerException("Cannot find a valid warp \"" + islandWarpName + "\"");

                // TODO
            });
        }
    },

    UPDATE_ISLANDS_WARPS_LOCATION() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            Bundle filters = bundle.getExtra("filters");
            requireIsland(filters, bundle.getExtra("columns"), (columns, island) -> {
                String islandWarpName = filters.getString(Consts.Island.Warp.NAME);
                IslandWarp islandWarp = island.getWarp(islandWarpName);

                if (islandWarp == null)
                    throw new RequestHandlerException("Cannot find a valid warp \"" + islandWarpName + "\"");

                islandWarp.setLocation(Serializers.deserializeLocation(columns.getString(Consts.Island.Warp.LOCATION)));
            });
        }
    },

    UPDATE_ISLANDS_WARPS_NAME() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            Bundle filters = bundle.getExtra("filters");
            requireIsland(filters, bundle.getExtra("columns"), (columns, island) -> {
                String islandWarpName = filters.getString(Consts.Island.Warp.NAME);
                IslandWarp islandWarp = island.getWarp(islandWarpName);

                if (islandWarp == null)
                    throw new RequestHandlerException("Cannot find a valid warp \"" + islandWarpName + "\"");

                islandWarp.setName(columns.getString(Consts.Island.Warp.NAME));
            });
        }
    },

    UPDATE_ISLANDS_WARPS_PRIVATE() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            Bundle filters = bundle.getExtra("filters");
            requireIsland(filters, bundle.getExtra("columns"), (columns, island) -> {
                String islandWarpName = filters.getString(Consts.Island.Warp.NAME);
                IslandWarp islandWarp = island.getWarp(islandWarpName);

                if (islandWarp == null)
                    throw new RequestHandlerException("Cannot find a valid warp \"" + islandWarpName + "\"");

                islandWarp.setPrivateFlag(columns.getBoolean(Consts.Island.Warp.PRIVATE));
            });
        }
    },

    UPDATE_ISLANDS_WARP_CATEGORIES_ICON() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            Bundle filters = bundle.getExtra("filters");
            requireIsland(filters, bundle.getExtra("columns"), (columns, island) -> {
                String warpCategoryName = filters.getString(Consts.Island.WarpCategory.NAME);
                WarpCategory warpCategory = island.getWarpCategory(warpCategoryName);

                if (warpCategory == null)
                    throw new RequestHandlerException("Invalid warp category update with name \"" + warpCategoryName + "\"");

                // TODO
            });
        }
    },

    UPDATE_ISLANDS_WARP_CATEGORIES_NAME() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            Bundle filters = bundle.getExtra("filters");
            requireIsland(filters, bundle.getExtra("columns"), (columns, island) -> {
                String warpCategoryName = filters.getString(Consts.Island.WarpCategory.NAME);
                WarpCategory warpCategory = island.getWarpCategory(warpCategoryName);

                if (warpCategory == null)
                    throw new RequestHandlerException("Invalid warp category update with name \"" + warpCategoryName + "\"");

                warpCategory.getIsland().renameCategory(warpCategory, columns.getString(Consts.Island.WarpCategory.NAME));
            });
        }
    },

    UPDATE_ISLANDS_WARP_CATEGORIES_SLOT() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            Bundle filters = bundle.getExtra("filters");
            requireIsland(filters, bundle.getExtra("columns"), (columns, island) -> {
                String warpCategoryName = filters.getString(Consts.Island.WarpCategory.NAME);
                WarpCategory warpCategory = island.getWarpCategory(warpCategoryName);

                if (warpCategory == null)
                    throw new RequestHandlerException("Invalid warp category update with name \"" + warpCategoryName + "\"");

                warpCategory.setSlot(columns.getInt(Consts.Island.WarpCategory.SLOT));
            });
        }
    },

    UPDATE_ISLANDS_WORTH_BONUS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                    island.setBonusWorth(columns.getBigDecimal(Consts.Island.WORTH_BONUS))
            );
        }
    },

    UPDATE_PLAYERS_ADMIN_BYPASS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                    superiorPlayer.setBypassMode(columns.getBoolean(Consts.Player.ADMIN_BYPASS))
            );
        }
    },

    UPDATE_PLAYERS_ADMIN_SPY() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                    superiorPlayer.setAdminSpy(columns.getBoolean(Consts.Player.ADMIN_SPY))

            );
        }
    },

    UPDATE_PLAYERS_BLOCKS_STACKER() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                    superiorPlayer.setBlocksStacker(columns.getBoolean(Consts.Player.BLOCKS_STACKER))
            );
        }
    },

    UPDATE_PLAYERS_CUSTOM_DATA_DATA() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) -> {
                byte[] data = columns.getString(Consts.Player.PERSISTENT_DATA).getBytes(StandardCharsets.UTF_8);
                superiorPlayer.getPersistentDataContainer().load(data);
            });
        }
    },

    UPDATE_PLAYERS_DISBANDS() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                    superiorPlayer.setDisbands(columns.getInt(Consts.Player.DISBANDS))
            );
        }
    },

    UPDATE_PLAYERS_LAST_TIME_UPDATED() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                    superiorPlayer.setLastTimeStatus(columns.getLong(Consts.Player.LAST_TIME_UPDATED))
            );
        }
    },

    UPDATE_PLAYERS_LAST_USED_NAME() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                    superiorPlayer.setName(columns.getString(Consts.Player.LAST_USED_NAME))
            );
        }
    },

    UPDATE_PLAYERS_LAST_USED_SKIN() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                    superiorPlayer.setTextureValue(columns.getString(Consts.Player.LAST_USED_SKIN))
            );
        }
    },

    UPDATE_PLAYERS_SETTINGS_BORDER_COLOR() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                    superiorPlayer.setBorderColor(columns.getEnum(Consts.Player.BORDER_COLOR, BorderColor.class))
            );
        }
    },

    UPDATE_PLAYERS_SETTINGS_ISLAND_FLY() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                    superiorPlayer.setIslandFly(columns.getBoolean(Consts.Player.ISLAND_FLY))
            );
        }
    },

    UPDATE_PLAYERS_SETTINGS_LANGUAGE() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) -> {
                String[] language = columns.getString(Consts.Player.LANGUAGE).split("-");
                superiorPlayer.setUserLocale(new Locale(language[0], language[1]));
            });
        }
    },

    UPDATE_PLAYERS_SETTINGS_TOGGLED_BORDER() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                    superiorPlayer.setWorldBorderEnabled(columns.getBoolean(Consts.Player.TOGGLED_BORDER))
            );
        }
    },

    UPDATE_PLAYERS_SETTINGS_TOGGLED_PANEL() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                    superiorPlayer.setToggledPanel(columns.getBoolean(Consts.Player.TOGGLED_PANEL))
            );
        }
    },

    UPDATE_PLAYERS_TEAM_CHAT() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                    superiorPlayer.setTeamChat(columns.getBoolean(Consts.Player.TEAM_CHAT))
            );
        }
    },

    REQUEST_DATA_SYNC() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            module.sendDataSync(bundle.getSender(), bundle.contains(Consts.DataSyncRequest.INCLUDE_PLAYERS));
        }

        @Override
        public boolean onSend(Bundle bundle) {
            this.populateBundle(bundle);
            return true;
        }

        @Override
        public boolean canReceive(Bundle bundle) {
            return true;
        }
    },

    FORCE_DATA_SYNC() {
        @Override
        public void onReceive(Bundle bundle) throws RequestHandlerException {
            bundle.getList(Consts.ForceDataSync.ISLANDS).forEach(islandData -> {
                Island island = Serializers.deserializeIsland((Bundle) islandData);
                Island oldIsland = module.getPlugin().getGrid().getIslandByUUID(island.getUniqueId());
                if (oldIsland != null) {
                    World.Environment defaultWorld = module.getPlugin().getSettings().getWorlds().getDefaultWorld();
                    Location oldIslandCenter = oldIsland.getCenter(defaultWorld);
                    Location islandCenter = island.getCenter(defaultWorld);
                    if (!islandCenter.equals(oldIslandCenter))
                        throw new IllegalStateException("Old island and new islands are not in the same place. " +
                                "Old: " + oldIslandCenter + ", Current: " + islandCenter);

                    module.getPlugin().getGrid().getIslandsContainer().removeIsland(oldIsland);
                    oldIsland.getIslandMembers(true).forEach(islandMember -> {
                        if (islandMember.getIsland() == oldIsland)
                            islandMember.setIsland(island);
                    });
                    oldIsland.getInvitedPlayers().forEach(invitedPlayer -> invitedPlayer.removeInvite(oldIsland));
                }
                module.getPlugin().getGrid().getIslandsContainer().addIsland(island);
            });

            bundle.getList(Consts.ForceDataSync.PLAYERS).forEach(playerData -> {
                Serializers.deserializePlayer((Bundle) playerData);
            });
        }

        @Override
        public boolean onSend(Bundle bundle) {
            this.populateBundle(bundle);
            return true;
        }

        @Override
        public boolean canReceive(Bundle bundle) {
            return true;
        }
    };

    private static final SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();
    private static final EnumMap<DataSyncType, Long> SEQUENCE_NUMBERS = new EnumMap<>(DataSyncType.class);

    private final boolean hasReceiveLogic;

    DataSyncType() {
        this(true);
    }

    DataSyncType(boolean hasReceiveLogic) {
        this.hasReceiveLogic = hasReceiveLogic;
    }

    public boolean canSend(Bundle bundle) {
        return this.hasReceiveLogic;
    }

    public boolean onSend(Bundle bundle) {
        this.populateBundle(bundle);
        long sequenceNumber = System.currentTimeMillis();
        bundle.setLong("sequenceNumber", sequenceNumber);
        Long oldSequenceNumber = SEQUENCE_NUMBERS.put(this, sequenceNumber);
        // We make sure the old sequence number is lower than the current one
        // Because we use times for sequence number this situation should never occur, however better being safe.
        return oldSequenceNumber == null || oldSequenceNumber < sequenceNumber;
    }

    protected void populateBundle(Bundle bundle) {
        bundle.setSender(module.getSettings().serverName);
        bundle.setChannelName(module.getSettings().messagingServiceDataChannelName);
        bundle.setString("type", name());
    }

    public boolean canReceive(Bundle bundle) {
        long sequenceNumber = bundle.getLong("sequenceNumber");

        if (sequenceNumber <= SEQUENCE_NUMBERS.getOrDefault(this, 0L)) {
            System.out.println("Seq Number of packet: " + sequenceNumber);
            System.out.println("Cached Seq Number: " + SEQUENCE_NUMBERS.getOrDefault(this, 0L));
            return false;
        }

        return true;
    }

    public void onReceive(Bundle bundle) throws RequestHandlerException {
    }

    private static void requireIsland(Bundle filters, Bundle columns, RequestHandlerConsumer<Island> consumer) throws RequestHandlerException {
        Island island = getIslandFromFilters(filters);

        if (island == null)
            throw new RequestHandlerException("Cannot find a valid uuid of an island.");

        DatabaseBridgeAccessor.runWithoutDataSave(island, (RequestHandlerAction) () -> consumer.accept(columns, island));
    }

    private static void requireIsland(Bundle filters, RequestHandlerConsumer<Island> consumer) throws RequestHandlerException {
        requireIsland(filters, filters, consumer);
    }

    private static void optionalIsland(Bundle filters, RequestHandlerConsumer<Island> consumer) throws RequestHandlerException {
        Island island = getIslandFromFilters(filters);
        if (island != null) {
            DatabaseBridgeAccessor.runWithoutDataSave(island, (RequestHandlerAction) () -> consumer.accept(filters, island));
        }
    }

    @Nullable
    private static Island getIslandFromFilters(Bundle filters) {
        if (filters.contains("uuid")) {
            return SuperiorSkyblockAPI.getIslandByUUID(filters.getUUID("uuid"));
        } else if (filters.contains("island")) {
            return SuperiorSkyblockAPI.getIslandByUUID(filters.getUUID("island"));
        }

        return null;
    }

    private static void requirePlayer(Bundle filters, Bundle columns, RequestHandlerConsumer<SuperiorPlayer> consumer) throws RequestHandlerException {
        SuperiorPlayer superiorPlayer = getPlayerFromFilters(filters);

        if (superiorPlayer == null)
            throw new RequestHandlerException("Cannot find a valid uuid of a player.");

        DatabaseBridgeAccessor.runWithoutDataSave(superiorPlayer, (RequestHandlerAction) () -> consumer.accept(columns, superiorPlayer));
    }

    private static void requirePlayer(Bundle filters, RequestHandlerConsumer<SuperiorPlayer> consumer) throws RequestHandlerException {
        requirePlayer(filters, filters, consumer);
    }

    @Nullable
    private static SuperiorPlayer getPlayerFromFilters(Bundle filters) {
        UUID playerUUID;

        if (filters.contains("uuid")) {
            playerUUID = filters.getUUID("uuid");
        } else if (filters.contains("player")) {
            playerUUID = filters.getUUID("player");
        } else {
            return null;
        }

        return SuperiorSkyblockAPI.getPlayers().getPlayersContainer().getSuperiorPlayer(playerUUID);
    }

    private static boolean islandCreationCallback(IslandCreationAlgorithm.IslandCreationResult result,
                                                  String sender, String center) {
        // We make sure the location the island was generated is the same as the center from the request.

        String newIslandLocation = result.getIslandLocation().getWorld().getName() + "," +
                result.getIslandLocation().getX() + "," +
                result.getIslandLocation().getY() + "," +
                result.getIslandLocation().getZ() + "," +
                result.getIslandLocation().getYaw() + "," +
                result.getIslandLocation().getPitch();

        if (!newIslandLocation.equals(center)) {
            new RequestHandlerException("Cannot created at the desired location. Expected \"" +
                    center + "\", actual: \"" + newIslandLocation + "\"").printStackTrace();
            return false;
        }

        RemoteIsland remoteIsland = new RemoteIsland(sender, result.getIsland());

        SuperiorSkyblockAPI.getGrid().getIslandsContainer().addIsland(remoteIsland);

        SuperiorSkyblockAPI.getGrid().setLastIslandLocation(result.getIslandLocation());

        // We want to update the leader of the island with the new RemoteIsland
        DatabaseBridgeAccessor.runWithoutDataSave(remoteIsland.getOwner(),
                islandLeader -> islandLeader.setIsland(remoteIsland));

        return true;
    }

}
