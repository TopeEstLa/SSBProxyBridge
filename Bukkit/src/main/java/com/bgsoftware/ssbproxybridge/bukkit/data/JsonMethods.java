package com.bgsoftware.ssbproxybridge.bukkit.data;

import com.bgsoftware.ssbproxybridge.core.requests.RequestHandlerConsumer;
import com.bgsoftware.ssbproxybridge.core.requests.RequestHandlerException;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Consumer;

public class JsonMethods {

    private static final Gson gson = new Gson();

    private JsonMethods() {

    }

    public static JsonObject convertColumns(JsonArray columnsArray) {
        return convertArrayToObject(columnsArray, "name", "value");
    }

    public static JsonObject convertFilters(JsonArray filtersArray) {
        return convertArrayToObject(filtersArray, "column", "value");
    }

    @Nullable
    public static Island getIsland(JsonObject data) {
        if (data.has("uuid")) {
            return SuperiorSkyblockAPI.getIslandByUUID(UUID.fromString(data.get("uuid").getAsString()));
        } else if (data.has("island")) {
            return SuperiorSkyblockAPI.getIslandByUUID(UUID.fromString(data.get("island").getAsString()));
        }

        return null;
    }

    @Nullable
    public static SuperiorPlayer getSuperiorPlayer(JsonObject data) {
        UUID playerUUID;

        if (data.has("uuid")) {
            playerUUID = UUID.fromString(data.get("uuid").getAsString());
        } else if (data.has("player")) {
            playerUUID = UUID.fromString(data.get("player").getAsString());
        } else {
            return null;
        }

        return SuperiorSkyblockAPI.getPlayers().getPlayersContainer().getSuperiorPlayer(playerUUID);
    }

    public static void forEach(JsonArray jsonArray, Consumer<JsonElement> consumer) {
        try {
            forEachOrThrow(jsonArray, consumer::accept);
        } catch (RequestHandlerException ignored) {
        }
    }

    public static void forEachOrThrow(JsonArray jsonArray, RequestHandlerConsumer<JsonElement> consumer) throws RequestHandlerException {
        for (JsonElement filterElement : jsonArray) {
            JsonObject filter = filterElement.getAsJsonObject();

            String column = filter.has("column") ? filter.get("column").getAsString() :
                    filter.get("name").getAsString();

            if (column.equals("uuid") || column.equals("island"))
                continue;

            consumer.accept(filter.get("value").getAsJsonPrimitive());
        }
    }

    public static KeyMap<Integer> parseBlockCounts(String blockCountsSerialized) {
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

    private static JsonObject convertArrayToObject(JsonArray array, String key, String value) {
        JsonObject result = new JsonObject();
        array.forEach(element -> {
            JsonObject object = element.getAsJsonObject();
            result.add(object.get(key).getAsString(), object.get(value));
        });
        return result;
    }

}
