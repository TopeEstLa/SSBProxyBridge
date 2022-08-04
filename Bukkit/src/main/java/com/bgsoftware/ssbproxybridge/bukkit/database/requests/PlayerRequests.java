package com.bgsoftware.ssbproxybridge.bukkit.database.requests;

import com.bgsoftware.ssbproxybridge.bukkit.database.ProxyDatabaseBridge;
import com.bgsoftware.ssbproxybridge.bukkit.database.ProxyDatabaseBridgeFactory;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PlayerRequests {

    private static final Map<String, UpdateAction> UPDATE_ACTION_MAP = new ImmutableMap.Builder<String, UpdateAction>()
            .put("players:last_time_updated", (player, value) -> player.updateLastTimeStatus())
            .put("players:last_used_skin", (player, value) -> player.setTextureValue(value.getAsString()))

            .put("players_settings:language", (player, value) -> {
                String[] language = value.getAsString().split("-");
                player.setUserLocale(new Locale(language[0], language[1]));
            })
            .put("players_settings:toggled_border", (player, value) -> {
                boolean currentlyToggled = player.hasWorldBorderEnabled();
                if (currentlyToggled != value.getAsBoolean())
                    player.toggleWorldBorder();
            })
            .put("players_settings:toggled_panel", (player, value) -> player.setToggledPanel(value.getAsBoolean()))
            .put("players_settings:island_fly", (player, value) -> {
                boolean currentlyToggled = player.hasIslandFlyEnabled();
                if (currentlyToggled != value.getAsBoolean())
                    player.toggleIslandFly();
            })
            .put("players_settings:border_color", (player, value) ->
                    player.setBorderColor(BorderColor.valueOf(value.getAsString())))

            .build();

    private PlayerRequests() {

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

        switch (table) {
            case "players":
                Bukkit.broadcastMessage("Received players insert");
                // We want to create a new player, which is done by calling the getPlayer method.
                UUID playerUUID = UUID.fromString(columns.get("uuid").getAsString());
                try {
                    ProxyDatabaseBridgeFactory.getInstance().setCreateActivatedBridge(false);
                    SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(playerUUID);
                    ((ProxyDatabaseBridge) superiorPlayer.getDatabaseBridge()).activate();
                } finally {
                    ProxyDatabaseBridgeFactory.getInstance().setCreateActivatedBridge(true);
                }
                break;
            case "players_settings":
                // Do nothing
                break;
            case "players_missions":
                Bukkit.broadcastMessage(dataObject + "");
                // TODO
                break;
            default:
                throw new RequestHandlerException("Cannot find a valid table \"" + table + "\"");
        }
    }

    private static void handleUpdate(JsonObject dataObject) throws RequestHandlerException {
        String table = dataObject.get("table").getAsString();
        JsonObject filters = Requests.convertFilters(dataObject.get("filters").getAsJsonArray());

        UUID playerUUID;

        if (filters.has("uuid")) {
            playerUUID = UUID.fromString(filters.get("uuid").getAsString());
        } else if (filters.has("player")) {
            playerUUID = UUID.fromString(filters.get("player").getAsString());
        } else {
            throw new RequestHandlerException("Cannot find a valid uuid of a player.");
        }

        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(playerUUID);
        try {
            superiorPlayer.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.IDLE);
            for (JsonElement columnElement : dataObject.get("columns").getAsJsonArray()) {
                JsonObject column = columnElement.getAsJsonObject();
                String name = column.get("name").getAsString();
                JsonPrimitive value = column.get("value").getAsJsonPrimitive();
                UpdateAction updateAction = UPDATE_ACTION_MAP.get(table + ":" + name);

                if (updateAction == null)
                    throw new RequestHandlerException("Invalid update column: \"" + name + "\" for table \"" + table + "\"");

                updateAction.apply(superiorPlayer, value);
            }
        } finally {
            superiorPlayer.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.SAVE_DATA);
        }

    }

}
