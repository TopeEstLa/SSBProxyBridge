package com.bgsoftware.ssbproxybridge.bukkit.database.requests;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.island.BankTransactionImpl;
import com.bgsoftware.ssbproxybridge.bukkit.island.FakeSchematic;
import com.bgsoftware.ssbproxybridge.bukkit.island.Islands;
import com.bgsoftware.ssbproxybridge.bukkit.island.RemoteIsland;
import com.bgsoftware.ssbproxybridge.bukkit.island.creation.RemoteIslandCreationAlgorithm;
import com.bgsoftware.ssbproxybridge.bukkit.player.RemoteSuperiorPlayer;
import com.bgsoftware.ssbproxybridge.bukkit.utils.DatabaseBridgeAccessor;
import com.bgsoftware.ssbproxybridge.bukkit.utils.Serializers;
import com.bgsoftware.ssbproxybridge.core.MapBuilder;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.enums.BankAction;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.world.algorithm.IslandCreationAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public class IslandRequests {

    private static final SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

    private static final Gson gson = new Gson();

    private static final Map<String, RequestAction<Island, JsonObject>> INSERT_ACTION_MAP = new MapBuilder<String, RequestAction<Island, JsonObject>>()
            .put("islands_members", (island, columns) -> island.addMember(
                    SuperiorSkyblockAPI.getPlayer(UUID.fromString(columns.get("player").getAsString())),
                    SuperiorSkyblockAPI.getRoles().getPlayerRole(columns.get("role").getAsInt())
            ))
            .put("islands_bans", (island, columns) -> island.banMember(
                    SuperiorSkyblockAPI.getPlayer(UUID.fromString(columns.get("player").getAsString())),
                    SuperiorSkyblockAPI.getPlayer(UUID.fromString(columns.get("banned_by").getAsString()))
            ))
            .put("islands_homes", (island, columns) -> island.setIslandHome(
                    World.Environment.valueOf(columns.get("environment").getAsString()),
                    Serializers.deserializeLocation(columns.get("location").getAsString())
            ))
            .put("islands_visitor_homes", (island, columns) -> island.setVisitorsLocation(
                    Serializers.deserializeLocation(columns.get("location").getAsString())
            ))
            .put("islands_player_permissions", (island, columns) -> island.setPermission(
                    SuperiorSkyblockAPI.getPlayer(UUID.fromString(columns.get("player").getAsString())),
                    IslandPrivilege.getByName(columns.get("permission").getAsString()),
                    columns.get("status").getAsBoolean()
            ))
            .put("islands_role_permissions", (island, columns) -> island.setPermission(
                    SuperiorSkyblockAPI.getRoles().getPlayerRole(columns.get("role").getAsInt()),
                    IslandPrivilege.getByName(columns.get("permission").getAsString())
            ))
            .put("islands_upgrades", (island, columns) -> island.setUpgradeLevel(
                    SuperiorSkyblockAPI.getUpgrades().getUpgrade(columns.get("upgrade").getAsString()),
                    columns.get("level").getAsInt()
            ))
            .put("islands_block_limits", (island, columns) -> island.setBlockLimit(
                    Key.of(columns.get("block").getAsString()),
                    columns.get("limit").getAsInt()
            ))
            .put("islands_entity_limits", (island, columns) -> island.setEntityLimit(
                    Key.of(columns.get("entity").getAsString()),
                    columns.get("limit").getAsInt()
            ))
            .put("islands_effects", (island, columns) -> island.setPotionEffect(
                    PotionEffectType.getByName(columns.get("effect_type").getAsString()),
                    columns.get("level").getAsInt()
            ))
            .put("islands_role_limits", (island, columns) -> island.setRoleLimit(
                    SuperiorSkyblockAPI.getRoles().getPlayerRole(columns.get("role").getAsInt()),
                    columns.get("limit").getAsInt()
            ))
            .put("islands_warps", (island, columns) -> {
                IslandWarp islandWarp = island.createWarp(columns.get("name").getAsString(),
                        Serializers.deserializeLocation(columns.get("location").getAsString()),
                        island.getWarpCategory(columns.get("category").getAsString())
                );
                islandWarp.setPrivateFlag(columns.get("private").getAsBoolean());
                // TODO
//                islandWarp.setIcon(columns.get("private").getAsBoolean());
            })
            .put("islands_ratings", (island, columns) -> island.setRating(
                    SuperiorSkyblockAPI.getPlayer(UUID.fromString(columns.get("player").getAsString())),
                    Rating.valueOf(columns.get("rating").getAsInt())
            ))
            .put("islands_missions", (island, columns) -> {
                String missionName = columns.get("name").getAsString();
                Mission<?> mission = SuperiorSkyblockAPI.getMissions().getMission(missionName);

                if (mission == null)
                    throw new RequestHandlerException("Cannot find a valid mission \"" + missionName + "\"");

                island.setAmountMissionCompleted(mission, columns.get("finish_count").getAsInt());
            })
            .put("islands_flags", (island, columns) -> {
                IslandFlag islandFlag = IslandFlag.getByName(columns.get("name").getAsString());
                if (columns.get("status").getAsBoolean()) {
                    island.enableSettings(islandFlag);
                } else {
                    island.disableSettings(islandFlag);
                }
            })
            .put("islands_generators", (island, columns) -> island.setGeneratorAmount(
                    Key.of(columns.get("block").getAsString()),
                    columns.get("rate").getAsInt(),
                    World.Environment.valueOf(columns.get("environment").getAsString())
            ))
            .put("islands_chests", (island, columns) -> { /* We don't update chests */ })
            .put("islands_visitors", (island, columns) -> {
                SuperiorPlayer islandVisitor = SuperiorSkyblockAPI.getPlayer(UUID.fromString(columns.get("player").getAsString()));
                // We use a fake player so we can fake his online status
                RemoteSuperiorPlayer remoteSuperiorPlayer = new RemoteSuperiorPlayer(islandVisitor);
                remoteSuperiorPlayer.setOnlineStatus(true);
                island.setPlayerInside(remoteSuperiorPlayer, true);
                island.setPlayerInside(remoteSuperiorPlayer, false);
                remoteSuperiorPlayer.setOnlineStatus(false);
            })
            .put("islands_warp_categories", (island, columns) -> {
                WarpCategory warpCategory = island.createWarpCategory(columns.get("name").getAsString());
                warpCategory.setSlot(columns.get("slot").getAsInt());
                // TODO
//                warpCategory.setIcon(columns.get("icon").getAsString());
            })
            .put("bank_transactions", (island, columns) -> island.getIslandBank().loadTransaction(new BankTransactionImpl(
                    UUID.fromString(columns.get("player").getAsString()),
                    BankAction.valueOf(columns.get("bank_action").getAsString()),
                    columns.get("position").getAsInt(),
                    columns.get("time").getAsLong(),
                    columns.get("failure_reason").getAsString(),
                    new BigDecimal(columns.get("amount").getAsString())
            )))
            .put("islands_custom_data", (island, columns) -> {
                byte[] data = columns.get("data").getAsString().getBytes(StandardCharsets.UTF_8);
                island.getPersistentDataContainer().load(data);
            })
            .put("islands_banks", (island, columns) -> {
                island.getIslandBank().setBalance(new BigDecimal(columns.get("balance").getAsString()));
                island.setLastInterestTime(columns.get("last_interest_time").getAsLong());
            })
            .put("islands_settings", (island, columns) -> { /* Do nothing, as upgrades will not be synced otherwise */ })
            .build();

    private static final Map<String, RequestAction<Island, JsonPrimitive>> UPDATE_ACTION_MAP = new MapBuilder<String, RequestAction<Island, JsonPrimitive>>()
            .put("islands:unlocked_worlds", (island, value) -> Islands.setUnlockedWorlds(island, value.getAsInt()))
            .put("islands:name", (island, value) -> island.setName(value.getAsString()))
            .put("islands:description", (island, value) -> island.setDescription(value.getAsString()))
            .put("islands:discord", (island, value) -> island.setDiscord(value.getAsString()))
            .put("islands:paypal", (island, value) -> island.setPaypal(value.getAsString()))
            .put("islands:locked", (island, value) -> island.setLocked(value.getAsBoolean()))
            .put("islands:ignored", (island, value) -> island.setIgnored(value.getAsBoolean()))
            .put("islands:last_time_updated", (island, value) -> island.setLastTimeUpdate(value.getAsLong()))
            .put("islands:worth_bonus", (island, value) -> island.setBonusWorth(new BigDecimal(value.getAsString())))
            .put("islands:levels_bonus", (island, value) -> island.setBonusLevel(new BigDecimal(value.getAsString())))
            .put("islands:generated_schematics", (island, value) -> Islands.setGeneratedSchematics(island, value.getAsInt()))
            .put("islands:dirty_chunks", (island, value) -> {/* Do not care about dirty chunks */})
            .put("islands:block_counts", (island, value) -> {
                island.clearBlockCounts();
                island.handleBlocksPlace(parseBlockCounts(value.getAsString()));
            }).put("islands:owner", (island, value) -> island.transferIsland(SuperiorSkyblockAPI.getPlayer(UUID.fromString(value.getAsString()))))
            .put("islands_settings:coops_limit", (island, value) -> island.setCoopLimit(value.getAsInt()))
            .put("islands_settings:size", (island, value) -> island.setIslandSize(value.getAsInt()))
            .put("islands_settings:bank_limit", (island, value) -> island.setBankLimit(new BigDecimal(value.getAsString())))
            .put("islands_settings:crop_growth_multiplier", (island, value) -> island.setCropGrowthMultiplier(value.getAsDouble()))
            .put("islands_settings:spawner_rates_multiplier", (island, value) -> island.setSpawnerRatesMultiplier(value.getAsDouble()))
            .put("islands_settings:mob_drops_multiplier", (island, value) -> island.setMobDropsMultiplier(value.getAsDouble()))
            .put("islands_settings:members_limit", (island, value) -> island.setTeamLimit(value.getAsInt()))
            .put("islands_settings:warps_limit", (island, value) -> island.setWarpsLimit(value.getAsInt()))
            .put("islands_banks:last_interest_time", (island, value) -> island.setLastInterestTime(value.getAsLong()))
            .put("islands_banks:balance", (island, value) -> island.getIslandBank().setBalance(new BigDecimal(value.getAsString())))
            .build();

    private static final Map<String, RequestAction<IslandWarp, JsonPrimitive>> WARPS_UPDATE_ACTION_MAP = new MapBuilder<String, RequestAction<IslandWarp, JsonPrimitive>>()
            .put("islands_warps:name", (islandWarp, value) -> islandWarp.setName(value.getAsString()))
            .put("islands_warps:location", (islandWarp, value) -> islandWarp.setLocation(Serializers.deserializeLocation(value.getAsString())))
            .put("islands_warps:private", (islandWarp, value) -> islandWarp.setPrivateFlag(value.getAsBoolean()))
            .put("islands_warps:icon", (islandWarp, value) -> { /* TODO */ })
            .build();

    private static final Map<String, RequestAction<WarpCategory, JsonPrimitive>> WARP_CATEGORIES_UPDATE_ACTION_MAP = new MapBuilder<String, RequestAction<WarpCategory, JsonPrimitive>>()
            .put("islands_warp_categories:name", (warpCategory, value) -> warpCategory.getIsland().renameCategory(warpCategory, value.getAsString()))
            .put("islands_warp_categories:slot", (warpCategory, value) -> warpCategory.setSlot(value.getAsInt()))
            .put("islands_warp_categories:icon", (warpCategory, value) -> { /* TODO */ })
            .build();

    private static final Map<String, RequestAction<Island, JsonElement>> DELETE_ACTION_MAP = new MapBuilder<String, RequestAction<Island, JsonElement>>()
            .put("islands", (island, unused) -> {
                if (island instanceof RemoteIsland) {
                    ((RemoteIsland) island).removeIsland();
                } else {
                    Bukkit.getScheduler().runTask(module.getPlugin(), () ->
                            DatabaseBridgeAccessor.runWithoutDataSave(island, (Runnable) island::disbandIsland));
                }
            })
            .put("islands_banks", (unused1, unused2) -> { /* Do nothing */ })
            .put("islands_custom_data", (unused1, unused2) -> { /* Do nothing */ })
            .put("islands_chests", (unused1, unused2) -> { /* Do nothing */ })
            .put("islands_flags", (unused1, unused2) -> { /* Do nothing */ })
            .put("islands_settings", (unused1, unused2) -> { /* Do nothing */ })
            .put("islands_upgrades", (unused1, unused2) -> { /* Do nothing */ })
            .put("islands_visitors", (unused1, unused2) -> { /* Do nothing */ })
            .put("islands_members:player", (island, value) -> {
                SuperiorPlayer islandMember = SuperiorSkyblockAPI.getPlayer(UUID.fromString(value.getAsString()));
                DatabaseBridgeAccessor.runWithoutDataSave(islandMember, (Runnable) () -> island.kickMember(islandMember));
            })
            .put("islands_members", (unused1, unused2) -> { /* Do nothing */ })
            .put("islands_bans:player", (island, value) -> island.unbanMember(SuperiorSkyblockAPI.getPlayer(UUID.fromString(value.getAsString()))))
            .put("islands_bans", (unused1, unused2) -> { /* Do nothing */ })
            .put("islands_homes:environment", (island, value) -> island.setIslandHome(World.Environment.valueOf(value.getAsString()), null))
            .put("islands_homes", (unused1, unused2) -> { /* Do nothing */ })
            .put("islands_visitor_homes:environment", (island, value) -> island.setVisitorsLocation(null))
            .put("islands_visitor_homes", (unused1, unused2) -> { /* Do nothing */ })
            .put("islands_player_permissions:player", (island, value) -> island.resetPermissions(SuperiorSkyblockAPI.getPlayer(UUID.fromString(value.getAsString()))))
            .put("islands_player_permissions", (unused1, unused2) -> { /* Do nothing */ })
            .put("islands_role_permissions", (island, unused) -> island.resetPermissions())
            .put("islands_block_limits", (island, unused) -> island.clearBlockLimits())
            .put("islands_block_limits:block", (island, value) -> island.removeBlockLimit(Key.of(value.getAsString())))
            .put("islands_entity_limits", (island, unused) -> island.clearEntitiesLimits())
            .put("islands_effects:effect_type", (island, value) -> island.removePotionEffect(PotionEffectType.getByName(value.getAsString())))
            .put("islands_effects", (island, unused) -> island.clearEffects())
            .put("islands_role_limits:role", (island, value) -> island.removeRoleLimit(SuperiorSkyblockAPI.getRoles().getPlayerRole(value.getAsInt())))
            .put("islands_role_limits", (unused1, unused2) -> { /* Do nothing */ })
            .put("islands_warps:name", (island, value) -> island.deleteWarp(value.getAsString()))
            .put("islands_warps", (unused1, unused2) -> { /* Do nothing */ })
            .put("islands_ratings:player", (island, value) -> island.removeRating(SuperiorSkyblockAPI.getPlayer(UUID.fromString(value.getAsString()))))
            .put("islands_ratings", (island, unused) -> island.removeRatings())
            .put("islands_missions:name", (island, value) -> {
                String missionName = value.getAsString();
                Mission<?> mission = SuperiorSkyblockAPI.getMissions().getMission(missionName);

                if (mission == null)
                    throw new RequestHandlerException("Cannot find a valid mission \"" + missionName + "\"");

                island.setAmountMissionCompleted(mission, 0);
            })
            .put("islands_missions", (unused1, unused2) -> { /* Do nothing */ })
            .put("islands_generators:environment:block", (island, filters) -> {
                JsonObject value = filters.getAsJsonObject();
                island.removeGeneratorAmount(Key.of(value.get("block").getAsString()), World.Environment.valueOf(value.get("environment").getAsString()));
            })
            .put("islands_generators:environment", (island, value) -> island.clearGeneratorAmounts(World.Environment.valueOf(value.getAsString())))
            .put("islands_generators", (unused1, unused2) -> { /* Do nothing */ })
            .put("islands_warp_categories:name", (island, value) -> island.deleteCategory(island.getWarpCategory(value.getAsString())))
            .put("islands_warp_categories", (unused1, unused2) -> { /* Do nothing */ })
            .build();

    private IslandRequests() {

    }

    public static void handleRequest(JsonObject dataObject) throws RequestHandlerException {
        try {
            JsonElement typeElement = dataObject.get("type");

            if (!(typeElement instanceof JsonPrimitive))
                throw new RequestHandlerException("Missing field \"type\"");

            String type = typeElement.getAsString();

            switch (type) {
                case "insert":
                    handleInsert(dataObject);
                    break;
                case "update":
                    handleUpdate(dataObject);
                    break;
                case "delete":
                    handleDelete(dataObject);
                    break;
                default:
                    throw new RequestHandlerException("Received invalid type: \"" + type + "\"");
            }
        } catch (RequestHandlerException error) {
            throw error;
        } catch (Throwable error) {
            throw new RequestHandlerException(error);
        }
    }

    private static void handleInsert(JsonObject dataObject) throws RequestHandlerException {
        String table = dataObject.get("table").getAsString();
        JsonObject columns = Requests.convertColumns(dataObject.get("columns").getAsJsonArray());

        UUID islandUUID;

        if (columns.has("uuid")) {
            islandUUID = UUID.fromString(columns.get("uuid").getAsString());
        } else if (columns.has("island")) {
            islandUUID = UUID.fromString(columns.get("island").getAsString());
        } else {
            throw new RequestHandlerException("Cannot find a valid uuid of a player.");
        }

        Island island = SuperiorSkyblockAPI.getIslandByUUID(islandUUID);

        if (table.equals("islands") || island == null) {
            if (!table.equals("islands"))
                throw new RequestHandlerException("Received update for an invalid island: \"" + islandUUID + "\"");

            if (island == null) {
                // Create our new island.
                createIsland(dataObject, columns);
            }

            return;
        }

        DatabaseBridgeAccessor.runWithoutDataSave(island, (RequestHandlerAction) () -> {
            RequestAction<Island, JsonObject> insertAction = INSERT_ACTION_MAP.get(table);

            if (insertAction == null)
                throw new RequestHandlerException("Invalid insert column: \"" + table + "\"");

            insertAction.apply(island, columns);
        });
    }

    private static void handleUpdate(JsonObject dataObject) throws RequestHandlerException {
        String table = dataObject.get("table").getAsString();
        JsonObject filters = Requests.convertFilters(dataObject.get("filters").getAsJsonArray());

        UUID islandUUID;

        if (filters.has("uuid")) {
            islandUUID = UUID.fromString(filters.get("uuid").getAsString());
        } else if (filters.has("island")) {
            islandUUID = UUID.fromString(filters.get("island").getAsString());
        } else {
            throw new RequestHandlerException("Cannot find a valid uuid of a player.");
        }

        Island island = SuperiorSkyblockAPI.getIslandByUUID(islandUUID);

        if (island == null)
            throw new RequestHandlerException("Received update for an invalid island: \"" + islandUUID + "\"");

        DatabaseBridgeAccessor.runWithoutDataSave(island, (RequestHandlerAction) () -> {
            JsonArray columnsArray = dataObject.get("columns").getAsJsonArray();
            JsonObject columns = Requests.convertColumns(columnsArray);
            if (table.equals("islands_members")) {
                SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(filters.get("player").getAsString());

                if (superiorPlayer == null)
                    throw new RequestHandlerException("Invalid roles update for player \"" + filters.get("player").getAsString() + "\"");

                DatabaseBridgeAccessor.runWithoutDataSave(superiorPlayer, (Runnable) () -> superiorPlayer.setPlayerRole(
                        SuperiorSkyblockAPI.getRoles().getPlayerRole(columns.get("role").getAsInt())));
            } else if (table.equals("islands_warps")) {
                if (!filters.has("category")) { // category updates is ignored here.
                    IslandWarp islandWarp = island.getWarp(filters.get("name").getAsString());
                    handleAction(columnsArray, table, WARPS_UPDATE_ACTION_MAP, islandWarp);
                }
            } else if (table.equals("islands_warp_categories")) {
                WarpCategory warpCategory = island.getWarpCategory(filters.get("name").getAsString());

                if (warpCategory == null)
                    throw new RequestHandlerException("Invalid warp category update with name \"" + filters.get("name").getAsString() + "\"");

                handleAction(columnsArray, table, WARP_CATEGORIES_UPDATE_ACTION_MAP, warpCategory);
            } else {
                handleAction(columnsArray, table, UPDATE_ACTION_MAP, island);
            }
        });
    }

    private static void handleDelete(JsonObject dataObject) throws RequestHandlerException {
        String table = dataObject.get("table").getAsString();

        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonObject filters = Requests.convertFilters(filtersArray);

        UUID islandUUID;

        if (filters.has("uuid")) {
            islandUUID = UUID.fromString(filters.get("uuid").getAsString());
        } else if (filters.has("island")) {
            islandUUID = UUID.fromString(filters.get("island").getAsString());
        } else {
            throw new RequestHandlerException("Cannot find a valid uuid of a player.");
        }

        Island island = SuperiorSkyblockAPI.getIslandByUUID(islandUUID);

        if (island == null)
            return; // We ignore deletion of invalid islands.

        DatabaseBridgeAccessor.runWithoutDataSave(island, (RequestHandlerAction) () -> {
            if (filtersArray.size() == 1) {
                RequestAction<Island, JsonElement> deleteAction = DELETE_ACTION_MAP.get(table);

                if (deleteAction == null)
                    throw new RequestHandlerException("Invalid delete table: \"" + table + "\"");

                deleteAction.apply(island, null /* unused */);
            } else {
                StringBuilder actionMapKey = null;
                JsonElement value = null;

                if (filtersArray.size() == 2) {
                    for (JsonElement filterElement : filtersArray) {
                        JsonObject filter = filterElement.getAsJsonObject();

                        String column = filter.get("column").getAsString();

                        if (column.equals("uuid") || column.equals("island"))
                            continue;

                        actionMapKey = new StringBuilder(table + ":" + column);
                        value = filter.get("value").getAsJsonPrimitive();
                    }
                } else {
                    actionMapKey = new StringBuilder(table);
                    value = new JsonObject();
                    for (JsonElement filterElement : filtersArray) {
                        JsonObject filter = filterElement.getAsJsonObject();

                        String column = filter.get("column").getAsString();

                        if (column.equals("uuid") || column.equals("island"))
                            continue;

                        actionMapKey.append(":").append(column);
                        ((JsonObject) value).add(column, filter.get("value"));
                    }
                }

                // Not possible.
                assert actionMapKey != null;
                assert value != null;

                RequestAction<Island, JsonElement> deleteAction = DELETE_ACTION_MAP.get(actionMapKey.toString());

                if (deleteAction == null)
                    throw new RequestHandlerException("Invalid delete table: \"" + table + "\"");

                deleteAction.apply(island, value);
            }
        });
    }

    private static void createIsland(JsonObject dataObject, JsonObject columns) {
        String center = columns.get("center").getAsString();
        SuperiorPlayer islandLeader = SuperiorSkyblockAPI.getPlayer(UUID.fromString(columns.get("owner").getAsString()));

        islandLeader.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.IDLE);

        // We create our RemoteIsland
        IslandCreationAlgorithm islandCreationAlgorithm = SuperiorSkyblockAPI.getGrid().getIslandCreationAlgorithm();
        if (islandCreationAlgorithm instanceof RemoteIslandCreationAlgorithm)
            islandCreationAlgorithm = ((RemoteIslandCreationAlgorithm) islandCreationAlgorithm).getOriginal();

        islandCreationAlgorithm.createIsland(
                UUID.fromString(columns.get("uuid").getAsString()),
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
            remoteIsland.handleBlocksPlace(parseBlockCounts(columns.get("block_counts").getAsString()));
            Islands.setGeneratedSchematics(remoteIsland, columns.get("generated_schematics").getAsByte());
            Islands.setUnlockedWorlds(remoteIsland, columns.get("unlocked_worlds").getAsByte());
        });

        return true;
    }

    private static KeyMap<Integer> parseBlockCounts(String blockCountsSerialized) {
        KeyMap<Integer> blockCounts = KeyMap.createKeyMap();

        JsonArray blockCountsArray = gson.fromJson(blockCountsSerialized, JsonArray.class);
        blockCountsArray.forEach(blockCountElement -> {
            JsonObject blockCount = blockCountElement.getAsJsonObject();
            Key key = Key.of(blockCount.get("id").getAsString());
            int amount = Integer.parseInt(blockCount.get("amount").getAsString());
            blockCounts.put(key, amount);
        });

        return blockCounts;
    }

    private static <K> void handleAction(JsonArray columnsArray, String table,
                                         Map<String, RequestAction<K, JsonPrimitive>> actionMap,
                                         K key) throws RequestHandlerException {
        for (JsonElement columnElement : columnsArray) {
            JsonObject column = columnElement.getAsJsonObject();
            String name = column.get("name").getAsString();
            JsonPrimitive value = column.get("value").getAsJsonPrimitive();
            RequestAction<K, JsonPrimitive> updateAction = actionMap.get(table + ":" + name);

            if (updateAction == null)
                throw new RequestHandlerException("Invalid update column: \"" + name + "\" for table \"" + table + "\"");

            updateAction.apply(key, value);
        }
    }

}
