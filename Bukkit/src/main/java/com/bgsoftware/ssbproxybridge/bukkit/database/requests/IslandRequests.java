package com.bgsoftware.ssbproxybridge.bukkit.database.requests;

import com.bgsoftware.ssbproxybridge.bukkit.island.RemoteIsland;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

public class IslandRequests {

    private static final Gson gson = new Gson();

    private IslandRequests() {

    }

    public static void handleIslands(JsonObject dataObject) throws RequestHandlerException {
        handleRequest(dataObject, (type) -> {
            switch (type) {
                case "insert": {
                    JsonObject columns = Requests.convertColumns(dataObject.get("columns").getAsJsonArray());
                    String[] center = columns.get("center").getAsString().split(",");
                    // SuperiorWorld,600.5,100.0,0.5,0.0,0.0
                    // We want to u
                    RemoteIsland remoteIsland = new RemoteIsland(
                            SuperiorSkyblockAPI.getPlayer(UUID.fromString(columns.get("owner").getAsString())),
                            UUID.fromString(columns.get("uuid").getAsString()),
                            SuperiorSkyblockAPI.getFactory().createBlockPosition(center[0], (int) Double.parseDouble(center[1]),
                                    (int) Double.parseDouble(center[2]), (int) Double.parseDouble(center[3])),
                            columns.get("creation_time").getAsLong(),
                            columns.get("island_type").getAsString(),
                            columns.get("discord").getAsString(),
                            columns.get("paypal").getAsString(),
                            new BigDecimal(columns.get("worth_bonus").getAsString()),
                            new BigDecimal(columns.get("levels_bonus").getAsString()),
                            columns.get("locked").getAsBoolean(),
                            columns.get("ignored").getAsBoolean(),
                            columns.get("name").getAsString(),
                            columns.get("description").getAsString(),
                            columns.get("generated_schematics").getAsByte(),
                            columns.get("unlocked_worlds").getAsByte(),
                            columns.get("last_time_updated").getAsLong(),
                            parseBlockCounts(columns.get("block_counts").getAsString())
                    );
                    SuperiorSkyblockAPI.getGrid().getIslandsContainer().addIsland(remoteIsland);
                    break;
                }
                case "delete": {
                    JsonObject filters = Requests.convertFilters(dataObject.get("filters").getAsJsonArray());
                    UUID islandUUID = UUID.fromString(filters.get("uuid").getAsString());
                    Island island = SuperiorSkyblockAPI.getIslandByUUID(islandUUID);

                    if(!(island instanceof RemoteIsland))
                        throw new RequestHandlerException("Tried to delete invalid island: \"" + islandUUID + "\", \"" + island + "\"");

                    ((RemoteIsland) island).removeIsland();
                }
                default:
                    throw new RequestHandlerException("Cannot find a valid type \"" + type + "\"");
            }
        });
    }

    private static void handleRequest(JsonObject dataObject, Action action) throws RequestHandlerException {
        try {
            JsonElement typeElement = dataObject.get("type");

            if (!(typeElement instanceof JsonPrimitive))
                throw new RequestHandlerException("Missing field \"type\"");

            String type = typeElement.getAsString();

            action.accept(type);
        } catch (RequestHandlerException error) {
            throw error;
        } catch (Throwable error) {
            throw new RequestHandlerException(error);
        }
    }

    private static KeyMap<BigInteger> parseBlockCounts(String blockCountsSerialized) {
        KeyMap<BigInteger> blockCounts = KeyMap.createKeyMap();

        JsonArray blockCountsArray = gson.fromJson(blockCountsSerialized, JsonArray.class);
        blockCountsArray.forEach(blockCountElement -> {
            JsonObject blockCount = blockCountElement.getAsJsonObject();
            Key key = Key.of(blockCount.get("id").getAsString());
            BigInteger amount = new BigInteger(blockCount.get("amount").getAsString());
            blockCounts.put(key, amount);
        });

        return blockCounts;
    }

    interface Action {

        void accept(String type) throws RequestHandlerException;

    }

}
