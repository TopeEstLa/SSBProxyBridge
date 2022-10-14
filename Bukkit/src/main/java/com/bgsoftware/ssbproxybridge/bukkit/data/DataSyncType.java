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
import com.bgsoftware.ssbproxybridge.bukkit.utils.DatabaseBridgeAccessor;
import com.bgsoftware.ssbproxybridge.bukkit.utils.Serializers;
import com.bgsoftware.ssbproxybridge.core.bundle.Bundle;
import com.bgsoftware.ssbproxybridge.core.requests.IRequestHandler;
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
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Locale;
import java.util.UUID;

public enum DataSyncType {

    /* Delete Operations */

    DELETE_GRID(),

    DELETE_ISLANDS(bundle -> {
        optionalIsland(bundle.getExtra("filters"), (filters, island) -> {
            if (island instanceof RemoteIsland) {
                ((RemoteIsland) island).removeIsland();
            } else {
                BukkitExecutor.runTask(() -> DatabaseBridgeAccessor.runWithoutDataSave(island, (Runnable) island::disbandIsland));
            }
        });
    }),

    DELETE_ISLANDS_BANKS(),

    DELETE_ISLANDS_BANS(bundle -> {
        optionalIsland(bundle.getExtra("filters"), (filters, island) ->
                island.unbanMember(SuperiorSkyblockAPI.getPlayer(filters.getUUID("player"))));
    }),

    DELETE_ISLANDS_BLOCK_LIMITS(bundle -> {
        optionalIsland(bundle.getExtra("filters"), (filters, island) -> {
            if (filters.contains("block")) {
                island.removeBlockLimit(Key.of(filters.getString("block")));
            } else {
                island.clearBlockLimits();
            }
        });
    }),

    DELETE_ISLANDS_CHESTS(),

    DELETE_ISLANDS_CUSTOM_DATA(),

    DELETE_ISLANDS_EFFECTS(bundle -> {
        optionalIsland(bundle.getExtra("filters"), (filters, island) -> {
            if (filters.contains("effect_type")) {
                island.removePotionEffect(PotionEffectType.getByName(filters.getString("effect_type")));
            } else {
                island.clearEffects();
            }
        });
    }),

    DELETE_ISLANDS_ENTITY_LIMITS(bundle -> {
        optionalIsland(bundle.getExtra("filters"), (filters, island) -> island.clearEntitiesLimits());
    }),

    DELETE_ISLANDS_FLAGS(),

    DELETE_ISLANDS_GENERATORS(bundle -> {
        optionalIsland(bundle.getExtra("filters"), (filters, island) -> {
            if (filters.contains("environment")) {
                World.Environment environment = filters.getEnum("environment", World.Environment.class);
                if (filters.contains("block")) {
                    island.removeGeneratorAmount(Key.of(filters.getString("block")), environment);
                } else {
                    island.clearGeneratorAmounts(environment);
                }
            }
        });
    }),

    DELETE_ISLANDS_HOMES(bundle -> {
        optionalIsland(bundle.getExtra("filters"), (filters, island) ->
                island.setIslandHome(filters.getEnum("environment", World.Environment.class), null));
    }),

    DELETE_ISLANDS_ISLAND_EFFECTS(bundle -> {
        optionalIsland(bundle.getExtra("filters"), (filters, island) ->
                island.removePotionEffect(PotionEffectType.getByName(filters.getString("effect_type"))));
    }),

    DELETE_ISLANDS_MEMBERS(bundle -> {
        optionalIsland(bundle.getExtra("filters"), (filters, island) -> {
            SuperiorPlayer islandMember = SuperiorSkyblockAPI.getPlayer(filters.getUUID("player"));
            DatabaseBridgeAccessor.runWithoutDataSave(islandMember, (Runnable) () -> island.kickMember(islandMember));
        });
    }),

    DELETE_ISLANDS_MISSIONS(bundle -> {
        optionalIsland(bundle.getExtra("filters"), (filters, island) -> {
            String missionName = filters.getString("name");
            Mission<?> mission = SuperiorSkyblockAPI.getMissions().getMission(missionName);

            if (mission == null)
                throw new RequestHandlerException("Cannot find a valid mission \"" + missionName + "\"");

            island.setAmountMissionCompleted(mission, 0);
        });
    }),

    DELETE_ISLANDS_PLAYER_PERMISSIONS(bundle -> {
        optionalIsland(bundle.getExtra("filters"), (filters, island) ->
                island.resetPermissions(SuperiorSkyblockAPI.getPlayer(filters.getUUID("player"))));
    }),

    DELETE_ISLANDS_RATINGS(bundle -> {
        optionalIsland(bundle.getExtra("filters"), (filters, island) -> {
            if (filters.contains("player")) {
                island.removeRating(SuperiorSkyblockAPI.getPlayer(filters.getUUID("player")));
            } else {
                island.removeRatings();
            }
        });
    }),

    DELETE_ISLANDS_ROLE_LIMITS(bundle -> {
        optionalIsland(bundle.getExtra("filters"), (filters, island) ->
                island.removeRoleLimit(SuperiorSkyblockAPI.getRoles().getPlayerRole(bundle.getInt("role"))));
    }),

    DELETE_ISLANDS_ROLE_PERMISSIONS(bundle -> {
        optionalIsland(bundle.getExtra("filters"), (filters, island) -> island.resetPermissions());
    }),

    DELETE_ISLANDS_SETTINGS(),

    DELETE_ISLANDS_UPGRADES(),

    DELETE_ISLANDS_VISITORS(),

    DELETE_ISLANDS_VISITOR_HOMES(bundle -> {
        optionalIsland(bundle.getExtra("filters"), (filters, island) -> island.setVisitorsLocation(null));
    }),

    DELETE_ISLANDS_WARPS(bundle -> {
        optionalIsland(bundle.getExtra("filters"), (filters, island) ->
                island.deleteWarp(filters.getString("")));
    }),

    DELETE_ISLANDS_WARP_CATEGORIES(bundle -> {
        optionalIsland(bundle.getExtra("filters"), (filters, island) ->
                island.deleteCategory(island.getWarpCategory(filters.getString("name"))));
    }),

    DELETE_PLAYERS(bundle -> {
        requirePlayer(bundle.getExtra("filters"), (filters, superiorPlayer) ->
                SuperiorSkyblockAPI.getPlayers().getPlayersContainer().removePlayer(superiorPlayer));
    }),

    DELETE_PLAYERS_CUSTOM_DATA(),

    DELETE_PLAYERS_MISSIONS(bundle -> {
        requirePlayer(bundle.getExtra("filters"), (filters, superiorPlayer) -> {
            String missionName = bundle.getString("name");
            Mission<?> mission = SuperiorSkyblockAPI.getMissions().getMission(missionName);

            if (mission == null)
                throw new RequestHandlerException("Cannot find a valid mission \"" + missionName + "\"");

            superiorPlayer.setAmountMissionCompleted(mission, 0);
        });
    }),

    DELETE_PLAYERS_SETTINGS(),

    DELETE_STACKED_BLOCKS(),

    /* Insert Operations */

    INSERT_BANK_TRANSACTIONS(bundle -> {
        SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();
        requireIsland(bundle.getExtra("columns"), (columns, island) -> {
            island.getIslandBank().loadTransaction(module.getPlugin().getFactory().createTransaction(
                    columns.getUUID("player"),
                    columns.getEnum("bank_action", BankAction.class),
                    columns.getInt("position"),
                    columns.getLong("time"),
                    columns.getString("failure_reason"),
                    columns.getBigDecimal("amount")
            ));
        });
    }),

    INSERT_GRID(),

    INSERT_ISLANDS(bundle -> {
        Bundle columns = bundle.getExtra("columns");
        UUID islandUUID = columns.getUUID("uuid");

        Island island = SuperiorSkyblockAPI.getIslandByUUID(islandUUID);

        if (island != null) // Only if the island doesn't exist already we try to insert it again.
            return;

        String center = columns.getString("center");
        SuperiorPlayer islandLeader = SuperiorSkyblockAPI.getPlayer(columns.getUUID("owner"));

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
                columns.getString("name"),
                new FakeSchematic(columns.getString("island_type"))
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
    }),

    INSERT_ISLANDS_BANKS(bundle -> {
        requireIsland(bundle.getExtra("columns"), (columns, island) -> {
            island.getIslandBank().setBalance(columns.getBigDecimal("balance"));
            island.setLastInterestTime(columns.getLong("last_interest_time"));
        });
    }),

    INSERT_ISLANDS_BANS(bundle -> {
        requireIsland(bundle.getExtra("columns"), (columns, island) -> island.banMember(
                SuperiorSkyblockAPI.getPlayer(columns.getUUID("player")),
                SuperiorSkyblockAPI.getPlayer(columns.getUUID("banned_by"))
        ));
    }),

    INSERT_ISLANDS_BLOCK_LIMITS(bundle -> {
        requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setBlockLimit(
                Key.of(columns.getString("block")),
                columns.getInt("limit")
        ));
    }),

    /* We do not update chests */
    INSERT_ISLANDS_CHESTS(),

    INSERT_ISLANDS_CUSTOM_DATA(bundle -> {
        requireIsland(bundle.getExtra("columns"), (columns, island) -> {
            byte[] data = columns.getString("data").getBytes(StandardCharsets.UTF_8);
            island.getPersistentDataContainer().load(data);
        });
    }),

    INSERT_ISLANDS_EFFECTS(bundle -> {
        requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setPotionEffect(
                PotionEffectType.getByName(columns.getString("effect_type")),
                columns.getInt("level")
        ));
    }),

    INSERT_ISLANDS_ENTITY_LIMITS(bundle -> {
        requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setEntityLimit(
                Key.of(columns.getString("entity")),
                columns.getInt("limit")
        ));
    }),

    INSERT_ISLANDS_FLAGS(bundle -> {
        requireIsland(bundle.getExtra("columns"), (columns, island) -> {
            IslandFlag islandFlag = IslandFlag.getByName(columns.getString("name"));
            if (columns.getInt("status") == 1) {
                island.enableSettings(islandFlag);
            } else {
                island.disableSettings(islandFlag);
            }
        });
    }),

    INSERT_ISLANDS_GENERATORS(bundle -> {
        requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setGeneratorAmount(
                Key.of(columns.getString("block")),
                columns.getInt("rate"),
                columns.getEnum("environment", World.Environment.class)
        ));
    }),

    INSERT_ISLANDS_HOMES(bundle -> {
        requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setIslandHome(
                columns.getEnum("environment", World.Environment.class),
                Serializers.deserializeLocation(columns.getString("location"))
        ));
    }),

    INSERT_ISLANDS_MEMBERS(bundle -> {
        requireIsland(bundle.getExtra("columns"), (columns, island) -> island.addMember(
                SuperiorSkyblockAPI.getPlayer(columns.getUUID("player")),
                SuperiorSkyblockAPI.getRoles().getPlayerRole(columns.getInt("role"))
        ));
    }),

    INSERT_ISLANDS_MISSIONS(bundle -> {
        requireIsland(bundle.getExtra("columns"), (columns, island) -> {
            String missionName = columns.getString("name");
            Mission<?> mission = SuperiorSkyblockAPI.getMissions().getMission(missionName);

            if (mission == null)
                throw new RequestHandlerException("Cannot find a valid mission \"" + missionName + "\"");

            island.setAmountMissionCompleted(mission, columns.getInt("finish_count"));
        });
    }),

    INSERT_ISLANDS_PLAYER_PERMISSIONS(bundle -> {
        requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setPermission(
                SuperiorSkyblockAPI.getPlayer(columns.getUUID("player")),
                IslandPrivilege.getByName(columns.getString("permission")),
                columns.getBoolean("status")
        ));
    }),

    INSERT_ISLANDS_RATINGS(bundle -> {
        requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setRating(
                SuperiorSkyblockAPI.getPlayer(columns.getUUID("player")),
                Rating.valueOf(columns.getInt("rating"))
        ));
    }),

    INSERT_ISLANDS_ROLE_LIMITS(bundle -> {
        requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setRoleLimit(
                SuperiorSkyblockAPI.getRoles().getPlayerRole(columns.getInt("role")),
                columns.getInt("limit")
        ));
    }),

    INSERT_ISLANDS_ROLE_PERMISSIONS(bundle -> {
        requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setPermission(
                SuperiorSkyblockAPI.getRoles().getPlayerRole(columns.getInt("role")),
                IslandPrivilege.getByName(columns.getString("permission"))
        ));
    }),

    /* Do nothing, as upgrades will not be synced otherwise */
    INSERT_ISLANDS_SETTINGS(),

    INSERT_ISLANDS_UPGRADES(bundle -> {
        requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setUpgradeLevel(
                SuperiorSkyblockAPI.getUpgrades().getUpgrade(columns.getString("upgrade")),
                columns.getInt("level")
        ));
    }),

    INSERT_ISLANDS_VISITOR_HOMES(bundle -> {
        requireIsland(bundle.getExtra("columns"), (columns, island) -> island.setVisitorsLocation(
                Serializers.deserializeLocation(columns.getString("location"))
        ));
    }),

    INSERT_ISLANDS_VISITORS(bundle -> {
        requireIsland(bundle.getExtra("columns"), (columns, island) -> {
            SuperiorPlayer islandVisitor = SuperiorSkyblockAPI.getPlayer(columns.getUUID("player"));
            // We use a fake player so we can fake his online status
            RemoteSuperiorPlayer remoteSuperiorPlayer = new RemoteSuperiorPlayer(islandVisitor);
            remoteSuperiorPlayer.setOnlineStatus(true);
            island.setPlayerInside(remoteSuperiorPlayer, true);
            island.setPlayerInside(remoteSuperiorPlayer, false);
            remoteSuperiorPlayer.setOnlineStatus(false);
        });
    }),

    INSERT_ISLANDS_WARPS(bundle -> {
        requireIsland(bundle.getExtra("columns"), (columns, island) -> {
            IslandWarp islandWarp = island.createWarp(columns.getString("name"),
                    Serializers.deserializeLocation(columns.getString("location")),
                    island.getWarpCategory(columns.getString("category"))
            );
            islandWarp.setPrivateFlag(columns.getBoolean("private"));
//            // TODO
//            islandWarp.setIcon(columns.get("private").getAsBoolean());
        });
    }),

    INSERT_ISLANDS_WARP_CATEGORIES(bundle -> {
        requireIsland(bundle.getExtra("columns"), (columns, island) -> {
            WarpCategory warpCategory = island.createWarpCategory(columns.getString("name"));
            warpCategory.setSlot(columns.getInt("slot"));
//            // TODO
//            warpCategory.setIcon(columns.get("icon").getAsString());
        });
    }),

    INSERT_PLAYERS(bundle -> {
        Bundle columns = bundle.getExtra("columns");
        UUID playerUUID = columns.getUUID("uuid");

        // We want to create a new player, which is done by calling the getPlayer method.
        try {
            ProxyDatabaseBridgeFactory.getInstance().setCreateActivatedBridge(false);
            SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(playerUUID);

            RemoteSuperiorPlayer remoteSuperiorPlayer = new RemoteSuperiorPlayer(superiorPlayer);
            remoteSuperiorPlayer.setName(columns.getString("last_used_name"));
            remoteSuperiorPlayer.setTextureValue(columns.getString("last_used_skin"));
            remoteSuperiorPlayer.setDisbands(columns.getInt("disbands"));

            ((ProxyDatabaseBridge) superiorPlayer.getDatabaseBridge()).activate();
        } finally {
            ProxyDatabaseBridgeFactory.getInstance().setCreateActivatedBridge(true);
        }
    }),

    INSERT_PLAYERS_CUSTOM_DATA(bundle -> {
        requirePlayer(bundle.getExtra("columns"), (columns, superiorPlayer) -> {
            byte[] data = columns.getString("data").getBytes(StandardCharsets.UTF_8);
            superiorPlayer.getPersistentDataContainer().load(data);
        });
    }),

    INSERT_PLAYERS_MISSIONS(bundle -> {
        requirePlayer(bundle.getExtra("columns"), (columns, superiorPlayer) -> {
            String missionName = columns.getString("name");
            Mission<?> mission = SuperiorSkyblockAPI.getMissions().getMission(missionName);

            if (mission == null)
                throw new RequestHandlerException("Cannot find a valid mission \"" + missionName + "\"");

            superiorPlayer.setAmountMissionCompleted(mission, columns.getInt("finish_count"));
        });
    }),

    INSERT_PLAYERS_SETTINGS(),

    INSERT_STACKED_BLOCKS(),

    /* Updates Operations */

    /* Last islands are updated when new islands are created */
    UPDATE_GRID_LAST_ISLAND(),

    UPDATE_ISLANDS_BANKS_BALANCE(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.getIslandBank().setBalance(columns.getBigDecimal("balance"))
        );
    }),

    UPDATE_ISLANDS_BANKS_LAST_INTEREST_TIME(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.setLastInterestTime(columns.getLong("last_interest_time"))
        );
    }),

    UPDATE_ISLANDS_BLOCK_COUNTS(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) -> {
            island.clearBlockCounts();
            island.handleBlocksPlace(JsonMethods.parseBlockCounts(columns.getString("block_counts")));
        });
    }),

    UPDATE_ISLANDS_COOP_PLAYER(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.addCoop(SuperiorSkyblockAPI.getPlayer(columns.getUUID("uuid")))
        );
    }),

    UPDATE_ISLANDS_DESCRIPTION(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.setDescription(columns.getString("description"))
        );
    }),

    /* We do not care about dirty chunks */
    UPDATE_ISLANDS_DIRTY_CHUNKS(),

    UPDATE_ISLANDS_DISCORD(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.setDiscord(columns.getString("discord"))
        );
    }),

    UPDATE_ISLANDS_GENERATED_SCHEMATICS(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                Islands.setGeneratedSchematics(island, columns.getInt("generated_schematics"))
        );
    }),

    UPDATE_ISLANDS_IGNORED(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.setIgnored(columns.getBoolean("ignored"))
        );
    }),

    UPDATE_ISLANDS_INVITE_PLAYER(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.inviteMember(SuperiorSkyblockAPI.getPlayer(columns.getUUID("uuid")))
        );
    }),

    UPDATE_ISLANDS_LAST_TIME_UPDATED(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) -> {
            island.setLastTimeUpdate(columns.getLong("last_time_updated"));
            SSBProxyBridgeModule.getModule().getManager().updateIsland(island.getUniqueId());
        });
    }),

    UPDATE_ISLANDS_LEVELS_BONUS(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.setBonusLevel(columns.getBigDecimal("levels_bonus"))
        );
    }),

    UPDATE_ISLANDS_LOCKED(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.setLocked(bundle.getBoolean("locked"))
        );
    }),

    UPDATE_ISLANDS_MEMBERS_ROLE(bundle -> {
        requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                superiorPlayer.setPlayerRole(SuperiorSkyblockAPI.getRoles().getPlayerRole(columns.getInt("role")))
        );
    }),

    UPDATE_ISLANDS_NAME(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.setName(columns.getString("name"))
        );
    }),

    UPDATE_ISLANDS_OWNER(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.transferIsland(SuperiorSkyblockAPI.getPlayer(columns.getUUID("owner")))
        );
    }),

    UPDATE_ISLANDS_PAYPAL(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.setPaypal(bundle.getString("paypal"))
        );
    }),


    UPDATE_ISLANDS_SETTINGS_BANK_LIMIT(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.setBankLimit(bundle.getBigDecimal("bank_limit"))
        );
    }),

    UPDATE_ISLANDS_SETTINGS_COOPS_LIMIT(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.setCoopLimit(columns.getInt("coops_limit"))
        );
    }),

    UPDATE_ISLANDS_SETTINGS_CROP_GROWTH_MULTIPLIER(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.setCropGrowthMultiplier(columns.getDouble("crop_growth_multiplier"))
        );
    }),

    UPDATE_ISLANDS_SETTINGS_MEMBERS_LIMIT(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.setTeamLimit(columns.getInt("members_limit"))
        );
    }),

    UPDATE_ISLANDS_SETTINGS_MOB_DROPS_MULTIPLIER(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.setMobDropsMultiplier(columns.getDouble("mob_drops_multiplier"))
        );
    }),

    UPDATE_ISLANDS_SETTINGS_SIZE(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.setIslandSize(bundle.getInt("size"))
        );
    }),

    UPDATE_ISLANDS_SETTINGS_SPAWNER_RATES_MULTIPLIER(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.setSpawnerRatesMultiplier(columns.getDouble("spawner_rates_multiplier"))
        );
    }),

    UPDATE_ISLANDS_SETTINGS_WARPS_LIMIT(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.setWarpsLimit(columns.getInt("warps_limit"))
        );
    }),

    UPDATE_ISLANDS_UNCOOP_PLAYER(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.removeCoop(SuperiorSkyblockAPI.getPlayer(columns.getUUID("uuid")))
        );
    }),

    UPDATE_ISLANDS_UNLOCKED_WORLDS(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                Islands.setUnlockedWorlds(island, columns.getInt("unlocked_worlds"))
        );
    }),

    UPDATE_ISLANDS_WARPS_ICON(bundle -> {
        Bundle filters = bundle.getExtra("filters");
        requireIsland(filters, bundle.getExtra("columns"), (columns, island) -> {
            String islandWarpName = filters.getString("name");
            IslandWarp islandWarp = island.getWarp(islandWarpName);

            if (islandWarp == null)
                throw new RequestHandlerException("Cannot find a valid warp \"" + islandWarpName + "\"");

            // TODO
        });
    }),

    UPDATE_ISLANDS_WARPS_LOCATION(bundle -> {
        Bundle filters = bundle.getExtra("filters");
        requireIsland(filters, bundle.getExtra("columns"), (columns, island) -> {
            String islandWarpName = filters.getString("name");
            IslandWarp islandWarp = island.getWarp(islandWarpName);

            if (islandWarp == null)
                throw new RequestHandlerException("Cannot find a valid warp \"" + islandWarpName + "\"");

            islandWarp.setLocation(Serializers.deserializeLocation(columns.getString("location")));
        });
    }),

    UPDATE_ISLANDS_WARPS_NAME(bundle -> {
        Bundle filters = bundle.getExtra("filters");
        requireIsland(filters, bundle.getExtra("columns"), (columns, island) -> {
            String islandWarpName = filters.getString("name");
            IslandWarp islandWarp = island.getWarp(islandWarpName);

            if (islandWarp == null)
                throw new RequestHandlerException("Cannot find a valid warp \"" + islandWarpName + "\"");

            islandWarp.setName(columns.getString("name"));
        });
    }),

    UPDATE_ISLANDS_WARPS_PRIVATE(bundle -> {
        Bundle filters = bundle.getExtra("filters");
        requireIsland(filters, bundle.getExtra("columns"), (columns, island) -> {
            String islandWarpName = filters.getString("name");
            IslandWarp islandWarp = island.getWarp(islandWarpName);

            if (islandWarp == null)
                throw new RequestHandlerException("Cannot find a valid warp \"" + islandWarpName + "\"");

            islandWarp.setPrivateFlag(columns.getBoolean("private"));
        });
    }),

    UPDATE_ISLANDS_WARP_CATEGORIES_ICON(bundle -> {
        Bundle filters = bundle.getExtra("filters");
        requireIsland(filters, bundle.getExtra("columns"), (columns, island) -> {
            String warpCategoryName = filters.getString("name");
            WarpCategory warpCategory = island.getWarpCategory(warpCategoryName);

            if (warpCategory == null)
                throw new RequestHandlerException("Invalid warp category update with name \"" + warpCategoryName + "\"");

            // TODO
        });
    }),

    UPDATE_ISLANDS_WARP_CATEGORIES_NAME(bundle -> {
        Bundle filters = bundle.getExtra("filters");
        requireIsland(filters, bundle.getExtra("columns"), (columns, island) -> {
            String warpCategoryName = filters.getString("name");
            WarpCategory warpCategory = island.getWarpCategory(warpCategoryName);

            if (warpCategory == null)
                throw new RequestHandlerException("Invalid warp category update with name \"" + warpCategoryName + "\"");

            warpCategory.getIsland().renameCategory(warpCategory, columns.getString("name"));
        });
    }),

    UPDATE_ISLANDS_WARP_CATEGORIES_SLOT(bundle -> {
        Bundle filters = bundle.getExtra("filters");
        requireIsland(filters, bundle.getExtra("columns"), (columns, island) -> {
            String warpCategoryName = filters.getString("name");
            WarpCategory warpCategory = island.getWarpCategory(warpCategoryName);

            if (warpCategory == null)
                throw new RequestHandlerException("Invalid warp category update with name \"" + warpCategoryName + "\"");

            warpCategory.setSlot(columns.getInt("slot"));
        });
    }),

    UPDATE_ISLANDS_WORTH_BONUS(bundle -> {
        requireIsland(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, island) ->
                island.setBonusWorth(columns.getBigDecimal("worth_bonus"))
        );
    }),

    UPDATE_PLAYERS_ADMIN_BYPASS(bundle -> {
        requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                superiorPlayer.setBypassMode(columns.getBoolean("admin_bypass"))
        );
    }),

    UPDATE_PLAYERS_ADMIN_SPY(bundle -> {
        requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                superiorPlayer.setAdminSpy(columns.getBoolean("admin_spy"))

        );
    }),

    UPDATE_PLAYERS_BLOCKS_STACKER(bundle -> {
        requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                superiorPlayer.setBlocksStacker(columns.getBoolean("blocks_stacker"))
        );
    }),

    UPDATE_PLAYERS_CUSTOM_DATA_DATA(bundle -> {
        requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) -> {
            byte[] data = columns.getString("data").getBytes(StandardCharsets.UTF_8);
            superiorPlayer.getPersistentDataContainer().load(data);
        });
    }),

    UPDATE_PLAYERS_DISBANDS(bundle -> {
        requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                superiorPlayer.setDisbands(columns.getInt("disbands"))
        );
    }),

    UPDATE_PLAYERS_LAST_TIME_UPDATED(bundle -> {
        requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                superiorPlayer.setLastTimeStatus(columns.getLong("last_time_updated"))
        );
    }),

    UPDATE_PLAYERS_LAST_USED_NAME(bundle -> {
        requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                superiorPlayer.setName(columns.getString("last_used_name"))
        );
    }),

    UPDATE_PLAYERS_LAST_USED_SKIN(bundle -> {
        requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                superiorPlayer.setTextureValue(columns.getString("last_used_skin"))
        );
    }),

    UPDATE_PLAYERS_SETTINGS_BORDER_COLOR(bundle -> {
        requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                superiorPlayer.setBorderColor(columns.getEnum("border_color", BorderColor.class))
        );
    }),

    UPDATE_PLAYERS_SETTINGS_ISLAND_FLY(bundle -> {
        requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                superiorPlayer.setIslandFly(columns.getBoolean("island_fly"))
        );
    }),

    UPDATE_PLAYERS_SETTINGS_LANGUAGE(bundle -> {
        requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) -> {
            String[] language = columns.getString("language").split("-");
            superiorPlayer.setUserLocale(new Locale(language[0], language[1]));
        });
    }),

    UPDATE_PLAYERS_SETTINGS_TOGGLED_BORDER(bundle -> {
        requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                superiorPlayer.setWorldBorderEnabled(columns.getBoolean("toggled_border"))
        );
    }),

    UPDATE_PLAYERS_SETTINGS_TOGGLED_PANEL(bundle -> {
        requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                superiorPlayer.setToggledPanel(columns.getBoolean("toggled_panel"))
        );
    }),

    UPDATE_PLAYERS_TEAM_CHAT(bundle -> {
        requirePlayer(bundle.getExtra("filters"), bundle.getExtra("columns"), (columns, superiorPlayer) ->
                superiorPlayer.setTeamChat(columns.getBoolean("team_chat"))
        );
    });

    private static final SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();
    private static final EnumMap<DataSyncType, Long> SEQUENCE_NUMBERS = new EnumMap<>(DataSyncType.class);

    private final IRequestHandler requestHandler;

    DataSyncType(IRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    DataSyncType() {
        this(null);
    }

    @Nullable
    public IRequestHandler getHandler() {
        return requestHandler;
    }

    public boolean onSent(Bundle bundle) {
        long sequenceNumber = System.currentTimeMillis();
        bundle.setSender(module.getSettings().serverName);
        bundle.setChannelName(module.getSettings().messagingServiceDataChannelName);
        bundle.setLong("sequenceNumber", sequenceNumber);
        Long oldSequenceNumber = SEQUENCE_NUMBERS.put(this, sequenceNumber);
        // We make sure the old sequence number is lower than the current one
        // Because we use times for sequence number this situation should never occur, however better being safe.
        return oldSequenceNumber == null || oldSequenceNumber < sequenceNumber;
    }

    public boolean onReceive(Bundle bundle) {
        long sequenceNumber = bundle.getLong("sequenceNumber");

        if (sequenceNumber <= SEQUENCE_NUMBERS.getOrDefault(this, 0L)) {
            System.out.println("Seq Number of packet: " + sequenceNumber);
            System.out.println("Cached Seq Number: " + SEQUENCE_NUMBERS.getOrDefault(this, 0L));
            return false;
        }

        return true;
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
