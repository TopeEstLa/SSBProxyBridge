package com.bgsoftware.ssbproxybridge.bukkit.data;

import com.bgsoftware.ssbproxybridge.bukkit.bridge.ProxyDatabaseBridge;
import com.bgsoftware.ssbproxybridge.bukkit.bridge.ProxyDatabaseBridgeFactory;
import com.bgsoftware.ssbproxybridge.bukkit.island.BankTransactionImpl;
import com.bgsoftware.ssbproxybridge.bukkit.island.FakeSchematic;
import com.bgsoftware.ssbproxybridge.bukkit.island.Islands;
import com.bgsoftware.ssbproxybridge.bukkit.island.RemoteIsland;
import com.bgsoftware.ssbproxybridge.bukkit.island.creation.RemoteIslandCreationAlgorithm;
import com.bgsoftware.ssbproxybridge.bukkit.player.RemoteSuperiorPlayer;
import com.bgsoftware.ssbproxybridge.bukkit.utils.BukkitExecutor;
import com.bgsoftware.ssbproxybridge.bukkit.utils.DatabaseBridgeAccessor;
import com.bgsoftware.ssbproxybridge.bukkit.utils.Serializers;
import com.bgsoftware.ssbproxybridge.core.requests.IRequestHandler;
import com.bgsoftware.ssbproxybridge.core.requests.RequestHandlerAction;
import com.bgsoftware.ssbproxybridge.core.requests.RequestHandlerConsumer;
import com.bgsoftware.ssbproxybridge.core.requests.RequestHandlerException;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.enums.BankAction;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.world.algorithm.IslandCreationAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

public enum DataSyncType {

    /* Delete Operations */

    DELETE_GRID(unused -> { /* Do nothing */ }),

    DELETE_ISLANDS(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        optionalIsland(JsonMethods.convertFilters(filtersArray), island -> {
            if (island instanceof RemoteIsland) {
                ((RemoteIsland) island).removeIsland();
            } else {
                BukkitExecutor.runTask(() -> DatabaseBridgeAccessor.runWithoutDataSave(island, (Runnable) island::disbandIsland));
            }
        });
    }),

    DELETE_ISLANDS_BANKS(unused -> { /* Do nothing */ }),

    DELETE_ISLANDS_BANS(unused -> { /* Do nothing */ }),

    DELETE_ISLANDS_BANS_PLAYER(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        optionalIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(filtersArray, value ->
                island.unbanMember(SuperiorSkyblockAPI.getPlayer(UUID.fromString(value.getAsString())))
        ));
    }),

    DELETE_ISLANDS_BLOCK_LIMITS(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        optionalIsland(JsonMethods.convertFilters(filtersArray), Island::clearBlockLimits);
    }),

    DELETE_ISLANDS_BLOCK_LIMITS_BLOCK(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        optionalIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(filtersArray, value ->
                island.removeBlockLimit(Key.of(value.getAsString()))
        ));
    }),

    DELETE_ISLANDS_CHESTS(unused -> { /* Do nothing */ }),

    DELETE_ISLANDS_CUSTOM_DATA(unused -> { /* Do nothing */ }),

    DELETE_ISLANDS_EFFECTS(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        optionalIsland(JsonMethods.convertFilters(filtersArray), Island::clearEffects);
    }),

    DELETE_ISLANDS_ENTITY_LIMITS(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        optionalIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(filtersArray, value ->
                island.removePotionEffect(PotionEffectType.getByName(value.getAsString()))
        ));
    }),

    DELETE_ISLANDS_FLAGS(unused -> { /* Do nothing */ }),

    DELETE_ISLANDS_GENERATORS(unused -> { /* Do nothing */ }),

    DELETE_ISLANDS_GENERATORS_ENVIRONMENT(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        optionalIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(filtersArray, value ->
                island.clearGeneratorAmounts(World.Environment.valueOf(value.getAsString()))
        ));
    }),

    DELETE_ISLANDS_GENERATORS_ENVIRONMENT_BLOCK(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonObject filters = JsonMethods.convertFilters(filtersArray);
        optionalIsland(filters, island -> island.removeGeneratorAmount(
                Key.of(filters.get("block").getAsString()),
                World.Environment.valueOf(filters.get("environment").getAsString())
        ));
    }),

    DELETE_ISLANDS_HOMES(unused -> { /* Do nothing */ }),

    DELETE_ISLANDS_HOMES_ENVIRONMENT(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        optionalIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(filtersArray, value ->
                island.setIslandHome(World.Environment.valueOf(value.getAsString()), null)
        ));
    }),

    DELETE_ISLANDS_ISLAND_EFFECTS_EFFECT_TYPE(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        optionalIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(filtersArray, value ->
                island.removePotionEffect(PotionEffectType.getByName(value.getAsString()))
        ));
    }),

    DELETE_ISLANDS_MEMBERS(unused -> { /* Do nothing */ }),

    DELETE_ISLANDS_MEMBERS_PLAYER(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        optionalIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(filtersArray, value -> {
            SuperiorPlayer islandMember = SuperiorSkyblockAPI.getPlayer(UUID.fromString(value.getAsString()));
            DatabaseBridgeAccessor.runWithoutDataSave(islandMember, (Runnable) () -> island.kickMember(islandMember));
        }));
    }),

    DELETE_ISLANDS_MISSIONS(unused -> { /* Do nothing */ }),

    DELETE_ISLANDS_MISSIONS_NAME(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        optionalIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEachOrThrow(filtersArray, value -> {
            String missionName = value.getAsString();
            Mission<?> mission = SuperiorSkyblockAPI.getMissions().getMission(missionName);

            if (mission == null)
                throw new RequestHandlerException("Cannot find a valid mission \"" + missionName + "\"");

            island.setAmountMissionCompleted(mission, 0);
        }));
    }),

    DELETE_ISLANDS_PLAYER_PERMISSIONS(unused -> { /* Do nothing */ }),

    DELETE_ISLANDS_PLAYER_PERMISSIONS_PLAYER(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        optionalIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(filtersArray, value ->
                island.resetPermissions(SuperiorSkyblockAPI.getPlayer(UUID.fromString(value.getAsString())))
        ));
    }),

    DELETE_ISLANDS_RATINGS(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        optionalIsland(JsonMethods.convertFilters(filtersArray), Island::removeRatings);
    }),

    DELETE_ISLANDS_RATINGS_PLAYER(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        optionalIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(filtersArray, value ->
                island.removeRating(SuperiorSkyblockAPI.getPlayer(UUID.fromString(value.getAsString())))
        ));
    }),

    DELETE_ISLANDS_ROLE_LIMITS(unused -> { /* Do nothing */ }),

    DELETE_ISLANDS_ROLE_LIMITS_ROLE(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        optionalIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(filtersArray, value ->
                island.removeRoleLimit(SuperiorSkyblockAPI.getRoles().getPlayerRole(value.getAsInt()))
        ));
    }),

    DELETE_ISLANDS_ROLE_PERMISSIONS(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        optionalIsland(JsonMethods.convertFilters(filtersArray), Island::resetPermissions);
    }),

    DELETE_ISLANDS_SETTINGS(unused -> { /* Do nothing */ }),

    DELETE_ISLANDS_UPGRADES(unused -> { /* Do nothing */ }),

    DELETE_ISLANDS_VISITORS(unused -> { /* Do nothing */ }),

    DELETE_ISLANDS_VISITOR_HOMES(unused -> { /* Do nothing */ }),

    DELETE_ISLANDS_VISITOR_HOMES_ENVIRONMENT(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        optionalIsland(JsonMethods.convertFilters(filtersArray), island -> island.setVisitorsLocation(null));
    }),

    DELETE_ISLANDS_WARPS(unused -> { /* Do nothing */ }),

    DELETE_ISLANDS_WARPS_NAME(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        optionalIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(filtersArray, value ->
                island.deleteWarp(value.getAsString())
        ));
    }),

    DELETE_ISLANDS_WARP_CATEGORIES(unused -> { /* Do nothing */ }),

    DELETE_ISLANDS_WARP_CATEGORIES_NAME(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        optionalIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(filtersArray, value ->
                island.deleteCategory(island.getWarpCategory(value.getAsString()))
        ));
    }),

    DELETE_PLAYERS(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        requirePlayer(JsonMethods.convertFilters(filtersArray), superiorPlayer ->
                SuperiorSkyblockAPI.getPlayers().getPlayersContainer().removePlayer(superiorPlayer)
        );
    }),

    DELETE_PLAYERS_CUSTOM_DATA(unused -> { /* Do nothing */ }),

    DELETE_PLAYERS_MISSIONS(unused -> { /* Do nothing */ }),

    DELETE_PLAYERS_MISSIONS_NAME(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        requirePlayer(JsonMethods.convertFilters(filtersArray), superiorPlayer -> JsonMethods.forEachOrThrow(filtersArray, value -> {
            String missionName = value.getAsString();
            Mission<?> mission = SuperiorSkyblockAPI.getMissions().getMission(missionName);

            if (mission == null)
                throw new RequestHandlerException("Cannot find a valid mission \"" + missionName + "\"");

            superiorPlayer.setAmountMissionCompleted(mission, 0);
        }));
    }),

    DELETE_PLAYERS_SETTINGS(unused -> { /* Do nothing */ }),

    DELETE_STACKED_BLOCKS(unused -> { /* Do nothing */ }),

    /* Insert Operations */

    INSERT_BANK_TRANSACTIONS(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> island.getIslandBank().loadTransaction(new BankTransactionImpl(
                UUID.fromString(columns.get("player").getAsString()),
                BankAction.valueOf(columns.get("bank_action").getAsString()),
                columns.get("position").getAsInt(),
                columns.get("time").getAsLong(),
                columns.get("failure_reason").getAsString(),
                new BigDecimal(columns.get("amount").getAsString())
        )));
    }),

    INSERT_GRID(unused -> { /* Do nothing */ }),

    INSERT_ISLANDS(dataObject -> {
        JsonObject columns = JsonMethods.convertColumns(dataObject.get("columns").getAsJsonArray());
        UUID islandUUID = UUID.fromString(columns.get("uuid").getAsString());

        Island island = SuperiorSkyblockAPI.getIslandByUUID(islandUUID);

        if (island != null) // Only if the island doesn't exist already we try to insert it again.
            return;

        String center = columns.get("center").getAsString();
        SuperiorPlayer islandLeader = SuperiorSkyblockAPI.getPlayer(UUID.fromString(columns.get("owner").getAsString()));

        islandLeader.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.IDLE);

        // We create our RemoteIsland
        IslandCreationAlgorithm islandCreationAlgorithm = SuperiorSkyblockAPI.getGrid().getIslandCreationAlgorithm();
        if (islandCreationAlgorithm instanceof RemoteIslandCreationAlgorithm)
            islandCreationAlgorithm = ((RemoteIslandCreationAlgorithm) islandCreationAlgorithm).getOriginal();

        islandCreationAlgorithm.createIsland(
                islandUUID,
                islandLeader,
                SuperiorSkyblockAPI.getFactory().createBlockPosition(SuperiorSkyblockAPI.getGrid().getLastIslandLocation()),
                columns.get("name").getAsString(),
                new FakeSchematic(columns.get("island_type").getAsString())
        ).whenComplete((result, error) -> {
            boolean createdSuccessfully = false;

            try {
                if (error != null) {
                    error.printStackTrace();
                } else {
                    createdSuccessfully = islandCreationCallback(result, dataObject, columns, center);
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

    INSERT_ISLANDS_BANKS(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> {
            island.getIslandBank().setBalance(new BigDecimal(columns.get("balance").getAsString()));
            island.setLastInterestTime(columns.get("last_interest_time").getAsLong());
        });
    }),

    INSERT_ISLANDS_BANS(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> island.banMember(
                SuperiorSkyblockAPI.getPlayer(UUID.fromString(columns.get("player").getAsString())),
                SuperiorSkyblockAPI.getPlayer(UUID.fromString(columns.get("banned_by").getAsString()))
        ));
    }),

    INSERT_ISLANDS_BLOCK_LIMITS(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> island.setBlockLimit(
                Key.of(columns.get("block").getAsString()),
                columns.get("limit").getAsInt()
        ));
    }),

    INSERT_ISLANDS_CHESTS(unused -> { /* We do not update chests */ }),

    INSERT_ISLANDS_CUSTOM_DATA(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> {
            byte[] data = columns.get("data").getAsString().getBytes(StandardCharsets.UTF_8);
            island.getPersistentDataContainer().load(data);
        });
    }),

    INSERT_ISLANDS_EFFECTS(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> island.setPotionEffect(
                PotionEffectType.getByName(columns.get("effect_type").getAsString()),
                columns.get("level").getAsInt()
        ));
    }),

    INSERT_ISLANDS_ENTITY_LIMITS(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> island.setEntityLimit(
                Key.of(columns.get("entity").getAsString()),
                columns.get("limit").getAsInt()
        ));
    }),

    INSERT_ISLANDS_FLAGS(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> {
            IslandFlag islandFlag = IslandFlag.getByName(columns.get("name").getAsString());
            if (columns.get("status").getAsBoolean()) {
                island.enableSettings(islandFlag);
            } else {
                island.disableSettings(islandFlag);
            }
        });
    }),

    INSERT_ISLANDS_GENERATORS(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> island.setGeneratorAmount(
                Key.of(columns.get("block").getAsString()),
                columns.get("rate").getAsInt(),
                World.Environment.valueOf(columns.get("environment").getAsString())
        ));
    }),

    INSERT_ISLANDS_HOMES(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> island.setIslandHome(
                World.Environment.valueOf(columns.get("environment").getAsString()),
                Serializers.deserializeLocation(columns.get("location").getAsString())
        ));
    }),

    INSERT_ISLANDS_MEMBERS(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> island.addMember(
                SuperiorSkyblockAPI.getPlayer(UUID.fromString(columns.get("player").getAsString())),
                SuperiorSkyblockAPI.getRoles().getPlayerRole(columns.get("role").getAsInt())
        ));
    }),

    INSERT_ISLANDS_MISSIONS(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> {
            String missionName = columns.get("name").getAsString();
            Mission<?> mission = SuperiorSkyblockAPI.getMissions().getMission(missionName);

            if (mission == null)
                throw new RequestHandlerException("Cannot find a valid mission \"" + missionName + "\"");

            island.setAmountMissionCompleted(mission, columns.get("finish_count").getAsInt());
        });
    }),

    INSERT_ISLANDS_PLAYER_PERMISSIONS(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> island.setPermission(
                SuperiorSkyblockAPI.getPlayer(UUID.fromString(columns.get("player").getAsString())),
                IslandPrivilege.getByName(columns.get("permission").getAsString()),
                columns.get("status").getAsBoolean()
        ));
    }),

    INSERT_ISLANDS_RATINGS(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> island.setRating(
                SuperiorSkyblockAPI.getPlayer(UUID.fromString(columns.get("player").getAsString())),
                Rating.valueOf(columns.get("rating").getAsInt())
        ));
    }),

    INSERT_ISLANDS_ROLE_LIMITS(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> island.setRoleLimit(
                SuperiorSkyblockAPI.getRoles().getPlayerRole(columns.get("role").getAsInt()),
                columns.get("limit").getAsInt()
        ));
    }),

    INSERT_ISLANDS_ROLE_PERMISSIONS(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> island.setPermission(
                SuperiorSkyblockAPI.getRoles().getPlayerRole(columns.get("role").getAsInt()),
                IslandPrivilege.getByName(columns.get("permission").getAsString())
        ));
    }),

    INSERT_ISLANDS_SETTINGS(unused -> { /* Do nothing, as upgrades will not be synced otherwise */ }),

    INSERT_ISLANDS_UPGRADES(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> island.setUpgradeLevel(
                SuperiorSkyblockAPI.getUpgrades().getUpgrade(columns.get("upgrade").getAsString()),
                columns.get("level").getAsInt()
        ));
    }),

    INSERT_ISLANDS_VISITORS(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> {
            SuperiorPlayer islandVisitor = SuperiorSkyblockAPI.getPlayer(UUID.fromString(columns.get("player").getAsString()));
            // We use a fake player so we can fake his online status
            RemoteSuperiorPlayer remoteSuperiorPlayer = new RemoteSuperiorPlayer(islandVisitor);
            remoteSuperiorPlayer.setOnlineStatus(true);
            island.setPlayerInside(remoteSuperiorPlayer, true);
            island.setPlayerInside(remoteSuperiorPlayer, false);
            remoteSuperiorPlayer.setOnlineStatus(false);
        });
    }),

    INSERT_ISLANDS_VISITORS_HOMES(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> island.setVisitorsLocation(
                Serializers.deserializeLocation(columns.get("location").getAsString())
        ));
    }),

    INSERT_ISLANDS_WARPS(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> {
            IslandWarp islandWarp = island.createWarp(columns.get("name").getAsString(),
                    Serializers.deserializeLocation(columns.get("location").getAsString()),
                    island.getWarpCategory(columns.get("category").getAsString())
            );
            islandWarp.setPrivateFlag(columns.get("private").getAsBoolean());
//            // TODO
//            islandWarp.setIcon(columns.get("private").getAsBoolean());
        });
    }),

    INSERT_ISLANDS_WARP_CATEGORIES(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requireIsland(columns, island -> {
            WarpCategory warpCategory = island.createWarpCategory(columns.get("name").getAsString());
            warpCategory.setSlot(columns.get("slot").getAsInt());
//            // TODO
//            warpCategory.setIcon(columns.get("icon").getAsString());
        });
    }),

    INSERT_PLAYERS(dataObject -> {
        JsonObject columns = JsonMethods.convertColumns(dataObject.get("columns").getAsJsonArray());
        UUID playerUUID = UUID.fromString(columns.get("uuid").getAsString());

        // We want to create a new player, which is done by calling the getPlayer method.
        try {
            ProxyDatabaseBridgeFactory.getInstance().setCreateActivatedBridge(false);
            SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(playerUUID);

            RemoteSuperiorPlayer remoteSuperiorPlayer = new RemoteSuperiorPlayer(superiorPlayer);
            remoteSuperiorPlayer.setName(columns.get("last_used_name").getAsString());
            remoteSuperiorPlayer.setTextureValue(columns.get("last_used_skin").getAsString());
            remoteSuperiorPlayer.setDisbands(columns.get("disbands").getAsInt());

            ((ProxyDatabaseBridge) superiorPlayer.getDatabaseBridge()).activate();
        } finally {
            ProxyDatabaseBridgeFactory.getInstance().setCreateActivatedBridge(true);
        }
    }),

    INSERT_PLAYERS_CUSTOM_DATA(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requirePlayer(columns, superiorPlayer -> {
            byte[] data = columns.get("data").getAsString().getBytes(StandardCharsets.UTF_8);
            superiorPlayer.getPersistentDataContainer().load(data);
        });
    }),

    INSERT_PLAYERS_MISSIONS(dataObject -> {
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        JsonObject columns = JsonMethods.convertColumns(columnsArray);
        requirePlayer(columns, superiorPlayer -> {
            String missionName = columns.get("name").getAsString();
            Mission<?> mission = SuperiorSkyblockAPI.getMissions().getMission(missionName);

            if (mission == null)
                throw new RequestHandlerException("Cannot find a valid mission \"" + missionName + "\"");

            superiorPlayer.setAmountMissionCompleted(mission, columns.get("finish_count").getAsInt());
        });
    }),

    INSERT_PLAYERS_SETTINGS(unused -> { /* Do nothing */ }),

    INSERT_STACKED_BLOCKS(unused -> { /* Do nothing */ }),

    /* Updates Operations */

    UPDATE_GRID_LAST_ISLAND(unused -> { /* Last islands are updated when new islands are created */ }),

    UPDATE_ISLANDS_BANKS_BALANCE(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                island.getIslandBank().setBalance(new BigDecimal(value.getAsString()))
        ));
    }),

    UPDATE_ISLANDS_BANKS_LAST_INTEREST_TIME(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                island.setLastInterestTime(value.getAsLong())
        ));
    }),

    UPDATE_ISLANDS_BLOCK_COUNTS(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value -> {
            island.clearBlockCounts();
            island.handleBlocksPlace(JsonMethods.parseBlockCounts(value.getAsString()));
        }));
    }),

    UPDATE_ISLANDS_DESCRIPTION(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                island.setDescription(value.getAsString())
        ));
    }),

    UPDATE_ISLANDS_DIRTY_CHUNKS(unused -> { /* We do not care about dirty chunks */ }),

    UPDATE_ISLANDS_DISCORD(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                island.setDiscord(value.getAsString())
        ));
    }),

    UPDATE_ISLANDS_GENERATED_SCHEMATICS(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                Islands.setGeneratedSchematics(island, value.getAsInt())
        ));
    }),

    UPDATE_ISLANDS_IGNORED(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                island.setIgnored(value.getAsBoolean())
        ));
    }),

    UPDATE_ISLANDS_LAST_TIME_UPDATED(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                island.setLastTimeUpdate(value.getAsLong())
        ));
    }),

    UPDATE_ISLANDS_LEVELS_BONUS(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                island.setBonusLevel(new BigDecimal(value.getAsString()))
        ));
    }),

    UPDATE_ISLANDS_LOCKED(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                island.setLocked(value.getAsBoolean())
        ));
    }),

    UPDATE_ISLANDS_NAME(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                island.setName(value.getAsString())
        ));
    }),

    UPDATE_ISLANDS_OWNER(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                island.transferIsland(SuperiorSkyblockAPI.getPlayer(UUID.fromString(value.getAsString())))
        ));
    }),

    UPDATE_ISLANDS_PAYPAL(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                island.setPaypal(value.getAsString())
        ));
    }),

    UPDATE_ISLANDS_SETTINGS_BANK_LIMIT(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                island.setBankLimit(new BigDecimal(value.getAsString()))
        ));
    }),

    UPDATE_ISLANDS_SETTINGS_COOPS_LIMIT(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                island.setCoopLimit(value.getAsInt())
        ));
    }),

    UPDATE_ISLANDS_SETTINGS_CROP_GROWTH_MULTIPLIER(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                island.setCropGrowthMultiplier(value.getAsDouble())
        ));
    }),

    UPDATE_ISLANDS_SETTINGS_MEMBERS_LIMIT(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                island.setTeamLimit(value.getAsInt())
        ));
    }),

    UPDATE_ISLANDS_SETTINGS_MOB_DROPS_MULTIPLIER(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                island.setMobDropsMultiplier(value.getAsDouble())
        ));
    }),

    UPDATE_ISLANDS_SETTINGS_SIZE(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                island.setIslandSize(value.getAsInt())
        ));
    }),

    UPDATE_ISLANDS_SETTINGS_SPAWNER_RATES_MULTIPLIER(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                island.setSpawnerRatesMultiplier(value.getAsDouble())
        ));
    }),

    UPDATE_ISLANDS_SETTINGS_WARPS_LIMIT(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                island.setWarpsLimit(value.getAsInt())
        ));
    }),

    UPDATE_ISLANDS_UNLOCKED_WORLDS(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                Islands.setUnlockedWorlds(island, value.getAsInt())
        ));
    }),

    UPDATE_ISLANDS_WARPS_ICON(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonObject filters = JsonMethods.convertFilters(filtersArray);
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(filters, island -> {
            String islandWarpName = filters.get("name").getAsString();
            IslandWarp islandWarp = island.getWarp(islandWarpName);

            if (islandWarp == null)
                throw new RequestHandlerException("Cannot find a valid warp \"" + islandWarpName + "\"");

            // TODO
        });
    }),

    UPDATE_ISLANDS_WARPS_LOCATION(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonObject filters = JsonMethods.convertFilters(filtersArray);
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(filters, island -> {
            String islandWarpName = filters.get("name").getAsString();
            IslandWarp islandWarp = island.getWarp(islandWarpName);

            if (islandWarp == null)
                throw new RequestHandlerException("Cannot find a valid warp \"" + islandWarpName + "\"");

            JsonMethods.forEach(columnsArray, value ->
                    islandWarp.setLocation(Serializers.deserializeLocation(value.getAsString()))
            );
        });
    }),

    UPDATE_ISLANDS_WARPS_NAME(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonObject filters = JsonMethods.convertFilters(filtersArray);
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(filters, island -> {
            String islandWarpName = filters.get("name").getAsString();
            IslandWarp islandWarp = island.getWarp(islandWarpName);

            if (islandWarp == null)
                throw new RequestHandlerException("Cannot find a valid warp \"" + islandWarpName + "\"");

            JsonMethods.forEach(columnsArray, value ->
                    islandWarp.setName(value.getAsString())
            );
        });
    }),

    UPDATE_ISLANDS_WARPS_PRIVATE(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonObject filters = JsonMethods.convertFilters(filtersArray);
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(filters, island -> {
            String islandWarpName = filters.get("name").getAsString();
            IslandWarp islandWarp = island.getWarp(islandWarpName);

            if (islandWarp == null)
                throw new RequestHandlerException("Cannot find a valid warp \"" + islandWarpName + "\"");

            JsonMethods.forEach(columnsArray, value ->
                    islandWarp.setPrivateFlag(value.getAsBoolean())
            );
        });
    }),

    UPDATE_ISLANDS_WARP_CATEGORIES_ICON(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonObject filters = JsonMethods.convertFilters(filtersArray);
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(filters, island -> {
            String warpCategoryName = filters.get("name").getAsString();
            WarpCategory warpCategory = island.getWarpCategory(warpCategoryName);

            if (warpCategory == null)
                throw new RequestHandlerException("Invalid warp category update with name \"" + filters.get("name").getAsString() + "\"");

            // TODO
        });
    }),

    UPDATE_ISLANDS_WARP_CATEGORIES_NAME(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonObject filters = JsonMethods.convertFilters(filtersArray);
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(filters, island -> {
            String warpCategoryName = filters.get("name").getAsString();
            WarpCategory warpCategory = island.getWarpCategory(warpCategoryName);

            if (warpCategory == null)
                throw new RequestHandlerException("Invalid warp category update with name \"" + filters.get("name").getAsString() + "\"");

            JsonMethods.forEach(columnsArray, value ->
                    warpCategory.getIsland().renameCategory(warpCategory, value.getAsString())
            );
        });
    }),

    UPDATE_ISLANDS_WARP_CATEGORIES_SLOT(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonObject filters = JsonMethods.convertFilters(filtersArray);
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(filters, island -> {
            String warpCategoryName = filters.get("name").getAsString();
            WarpCategory warpCategory = island.getWarpCategory(warpCategoryName);

            if (warpCategory == null)
                throw new RequestHandlerException("Invalid warp category update with name \"" + filters.get("name").getAsString() + "\"");

            JsonMethods.forEach(columnsArray, value ->
                    warpCategory.setSlot(value.getAsInt())
            );
        });
    }),

    UPDATE_ISLANDS_WORTH_BONUS(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requireIsland(JsonMethods.convertFilters(filtersArray), island -> JsonMethods.forEach(columnsArray, value ->
                island.setBonusWorth(new BigDecimal(value.getAsString()))
        ));
    }),

    UPDATE_PLAYERS_CUSTOM_DATA_DATA(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requirePlayer(JsonMethods.convertFilters(filtersArray), superiorPlayer -> JsonMethods.forEach(columnsArray, value -> {
            byte[] data = value.getAsString().getBytes(StandardCharsets.UTF_8);
            superiorPlayer.getPersistentDataContainer().load(data);
        }));
    }),

    UPDATE_PLAYERS_DISBANDS(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requirePlayer(JsonMethods.convertFilters(filtersArray), superiorPlayer -> JsonMethods.forEach(columnsArray, value ->
                superiorPlayer.setDisbands(value.getAsInt())));
    }),

    UPDATE_PLAYERS_LAST_TIME_UPDATED(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requirePlayer(JsonMethods.convertFilters(filtersArray), superiorPlayer -> JsonMethods.forEach(columnsArray, value ->
                superiorPlayer.setLastTimeStatus(value.getAsLong())
        ));
    }),

    UPDATE_PLAYERS_LAST_USED_NAME(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requirePlayer(JsonMethods.convertFilters(filtersArray), superiorPlayer -> JsonMethods.forEach(columnsArray, value ->
                superiorPlayer.setName(value.getAsString())
        ));
    }),

    UPDATE_PLAYERS_LAST_USED_SKIN(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requirePlayer(JsonMethods.convertFilters(filtersArray), superiorPlayer -> JsonMethods.forEach(columnsArray, value ->
                superiorPlayer.setTextureValue(value.getAsString())
        ));
    }),

    UPDATE_PLAYERS_SETTINGS_BORDER_COLOR(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requirePlayer(JsonMethods.convertFilters(filtersArray), superiorPlayer -> JsonMethods.forEach(columnsArray, value ->
                superiorPlayer.setBorderColor(BorderColor.valueOf(value.getAsString()))
        ));
    }),

    UPDATE_PLAYERS_SETTINGS_ISLAND_FLY(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requirePlayer(JsonMethods.convertFilters(filtersArray), superiorPlayer -> JsonMethods.forEach(columnsArray, value ->
                superiorPlayer.setIslandFly(value.getAsBoolean())
        ));
    }),

    UPDATE_PLAYERS_SETTINGS_LANGUAGE(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requirePlayer(JsonMethods.convertFilters(filtersArray), superiorPlayer -> JsonMethods.forEach(columnsArray, value -> {
            String[] language = value.getAsString().split("-");
            superiorPlayer.setUserLocale(new Locale(language[0], language[1]));
        }));
    }),

    UPDATE_PLAYERS_SETTINGS_TOGGLED_BORDER(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requirePlayer(JsonMethods.convertFilters(filtersArray), superiorPlayer -> JsonMethods.forEach(columnsArray, value ->
                superiorPlayer.setWorldBorderEnabled(value.getAsBoolean())
        ));
    }),

    UPDATE_PLAYERS_SETTINGS_TOGGLED_PANEL(dataObject -> {
        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
        requirePlayer(JsonMethods.convertFilters(filtersArray), superiorPlayer -> JsonMethods.forEach(columnsArray, value ->
                superiorPlayer.setToggledPanel(value.getAsBoolean())
        ));
    });

    private final IRequestHandler requestHandler;

    DataSyncType(IRequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    public IRequestHandler getHandler() {
        return requestHandler;
    }

    private static void requireIsland(JsonObject data, RequestHandlerConsumer<Island> consumer) throws RequestHandlerException {
        Island island = JsonMethods.getIsland(data);

        if (island == null)
            throw new RequestHandlerException("Cannot find a valid uuid of an island.");

        DatabaseBridgeAccessor.runWithoutDataSave(island, (RequestHandlerAction) () -> consumer.accept(island));
    }

    private static void optionalIsland(JsonObject data, RequestHandlerConsumer<Island> consumer) throws RequestHandlerException {
        Island island = JsonMethods.getIsland(data);
        if (island != null) {
            DatabaseBridgeAccessor.runWithoutDataSave(island, (RequestHandlerAction) () -> consumer.accept(island));
        }
    }

    private static void requirePlayer(JsonObject data, RequestHandlerConsumer<SuperiorPlayer> consumer) throws RequestHandlerException {
        SuperiorPlayer superiorPlayer = JsonMethods.getSuperiorPlayer(data);

        if (superiorPlayer == null)
            throw new RequestHandlerException("Cannot find a valid uuid of a player.");

        DatabaseBridgeAccessor.runWithoutDataSave(superiorPlayer, (RequestHandlerAction) () -> consumer.accept(superiorPlayer));
    }

    private static boolean islandCreationCallback(IslandCreationAlgorithm.IslandCreationResult result,
                                                  JsonObject dataObject, JsonObject columns, String center) {
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

        RemoteIsland remoteIsland = new RemoteIsland(dataObject.get("sender").getAsString(), result.getIsland());

        SuperiorSkyblockAPI.getGrid().getIslandsContainer().addIsland(remoteIsland);

        SuperiorSkyblockAPI.getGrid().setLastIslandLocation(result.getIslandLocation());

        // We want to update the leader of the island with the new RemoteIsland
        DatabaseBridgeAccessor.runWithoutDataSave(remoteIsland.getOwner(),
                islandLeader -> islandLeader.setIsland(remoteIsland));

        DatabaseBridgeAccessor.runWithoutDataSave(remoteIsland, (Runnable) () -> {
            remoteIsland.setBonusWorth(new BigDecimal(columns.get("worth_bonus").getAsString()));
            remoteIsland.setBonusLevel(new BigDecimal(columns.get("levels_bonus").getAsString()));
            remoteIsland.setDiscord(columns.get("discord").getAsString());
            remoteIsland.setPaypal(columns.get("paypal").getAsString());
            remoteIsland.setLocked(columns.get("locked").getAsBoolean());
            remoteIsland.setIgnored(columns.get("ignored").getAsBoolean());
            remoteIsland.setDescription(columns.get("description").getAsString());
            remoteIsland.handleBlocksPlace(JsonMethods.parseBlockCounts(columns.get("block_counts").getAsString()));
            Islands.setGeneratedSchematics(remoteIsland, columns.get("generated_schematics").getAsByte());
            Islands.setUnlockedWorlds(remoteIsland, columns.get("unlocked_worlds").getAsByte());
        });

        return true;
    }

}
