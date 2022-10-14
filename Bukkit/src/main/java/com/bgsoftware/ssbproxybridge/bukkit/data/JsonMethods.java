package com.bgsoftware.ssbproxybridge.bukkit.data;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonMethods {

    private static final Gson gson = new Gson();

    private JsonMethods() {

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

}
