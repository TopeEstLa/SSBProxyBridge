package com.bgsoftware.ssbproxybridge.bukkit.database.requests;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.missions.IMissionsHolder;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Requests {

    private Requests() {

    }

    public static JsonObject convertColumns(JsonArray columnsArray) {
        return convertArrayToObject(columnsArray, "name", "value");
    }

    public static JsonObject convertFilters(JsonArray filtersArray) {
        return convertArrayToObject(filtersArray, "column", "value");
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
