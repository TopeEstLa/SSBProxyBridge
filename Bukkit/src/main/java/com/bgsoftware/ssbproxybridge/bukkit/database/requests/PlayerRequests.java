package com.bgsoftware.ssbproxybridge.bukkit.database.requests;

import com.bgsoftware.ssbproxybridge.bukkit.database.ProxyDatabaseBridge;
import com.bgsoftware.ssbproxybridge.bukkit.database.ProxyDatabaseBridgeFactory;
import com.bgsoftware.ssbproxybridge.bukkit.island.Islands;
import com.bgsoftware.ssbproxybridge.bukkit.player.RemoteSuperiorPlayer;
import com.bgsoftware.ssbproxybridge.core.MapBuilder;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PlayerRequests {

    private static final Map<String, RequestAction<SuperiorPlayer, JsonPrimitive>> UPDATE_ACTION_MAP = new MapBuilder<String, RequestAction<SuperiorPlayer, JsonPrimitive>>()
            .put("players:last_used_skin", (player, value) -> player.setTextureValue(value.getAsString()))
            .put("players:last_used_name", (player, value) -> {
                RemoteSuperiorPlayer remoteSuperiorPlayer = new RemoteSuperiorPlayer(player);
                remoteSuperiorPlayer.setName(value.getAsString());
            })
            .put("players:disbands", (player, value) -> player.setDisbands(value.getAsInt()))
            .put("players:last_time_updated", (player, value) -> player.updateLastTimeStatus())

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
            .put("players_custom_data:data", (player, value) -> {
                byte[] data = value.getAsString().getBytes(StandardCharsets.UTF_8);
                player.getPersistentDataContainer().load(data);
            })

            .build();
    private static final Map<String, RequestAction<SuperiorPlayer, JsonElement>> DELETE_ACTION_MAP = new MapBuilder<String, RequestAction<SuperiorPlayer, JsonElement>>()
            .put("players_missions:name", (superiorPlayer, value) -> Islands.setMissionCompletedCount(superiorPlayer, value.getAsString(), 0))
            .put("players_custom_data", (superiorPlayer, unused) -> { /* TODO */ })
            .put("players_missions", (superiorPlayer, unused) -> { /* Do nothing */ })
            .put("players", (superiorPlayer, unused) -> SuperiorSkyblockAPI.getPlayers().getPlayersContainer().removePlayer(superiorPlayer))
            .put("players_settings", (superiorPlayer, unused) -> { /* Do nothing */ })
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

        UUID playerUUID;

        if (columns.has("uuid")) {
            playerUUID = UUID.fromString(columns.get("uuid").getAsString());
        } else if (columns.has("player")) {
            playerUUID = UUID.fromString(columns.get("player").getAsString());
        } else {
            throw new RequestHandlerException("Cannot find a valid uuid of a player.");
        }

        switch (table) {
            case "players": {
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
                break;
            }
            case "players_settings":
                // Do nothing
                break;
            case "players_missions": {
                SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayers().getPlayersContainer().getSuperiorPlayer(playerUUID);

                if (superiorPlayer == null)
                    throw new RequestHandlerException("Received update for an invalid island: \"" + playerUUID + "\"");

                disableDatabaseBridge(superiorPlayer, () -> Islands.setMissionCompletedCount(superiorPlayer, columns));
                break;
            }
            case "players_custom_data": {
                SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayers().getPlayersContainer().getSuperiorPlayer(playerUUID);

                if (superiorPlayer == null)
                    throw new RequestHandlerException("Received update for an invalid island: \"" + playerUUID + "\"");

                byte[] data = columns.get("data").getAsString().getBytes(StandardCharsets.UTF_8);

                disableDatabaseBridge(superiorPlayer, () -> {
                    superiorPlayer.getPersistentDataContainer().load(data);
                });

                break;
            }
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

        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayers().getPlayersContainer().getSuperiorPlayer(playerUUID);

        if (superiorPlayer == null)
            throw new RequestHandlerException("Received update for an invalid island: \"" + playerUUID + "\"");

        disableDatabaseBridge(superiorPlayer, () -> {
            for (JsonElement columnElement : dataObject.get("columns").getAsJsonArray()) {
                JsonObject column = columnElement.getAsJsonObject();
                String name = column.get("name").getAsString();
                JsonPrimitive value = column.get("value").getAsJsonPrimitive();
                RequestAction<SuperiorPlayer, JsonPrimitive> updateAction = UPDATE_ACTION_MAP.get(table + ":" + name);

                if (updateAction == null)
                    throw new RequestHandlerException("Invalid update column: \"" + name + "\" for table \"" + table + "\"");

                updateAction.apply(superiorPlayer, value);
            }
        });
    }

    private static void handleDelete(JsonObject dataObject) throws RequestHandlerException {
        String table = dataObject.get("table").getAsString();

        JsonArray filtersArray = dataObject.get("filters").getAsJsonArray();
        JsonObject filters = Requests.convertFilters(filtersArray);

        UUID playerUUID;

        if (filters.has("uuid")) {
            playerUUID = UUID.fromString(filters.get("uuid").getAsString());
        } else if (filters.has("player")) {
            playerUUID = UUID.fromString(filters.get("player").getAsString());
        } else {
            throw new RequestHandlerException("Cannot find a valid uuid of a player.");
        }

        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayers().getPlayersContainer().getSuperiorPlayer(playerUUID);

        if (superiorPlayer == null)
            throw new RequestHandlerException("Received update for an invalid island: \"" + playerUUID + "\"");

        disableDatabaseBridge(superiorPlayer, () -> {
            if (filtersArray.size() == 1) {
                RequestAction<SuperiorPlayer, JsonElement> deleteAction = DELETE_ACTION_MAP.get(table);

                if (deleteAction == null)
                    throw new RequestHandlerException("Invalid delete table: \"" + table + "\"");

                deleteAction.apply(superiorPlayer, null /* unused */);
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

                RequestAction<SuperiorPlayer, JsonElement> deleteAction = DELETE_ACTION_MAP.get(actionMapKey.toString());

                if (deleteAction == null)
                    throw new RequestHandlerException("Invalid delete table: \"" + table + "\"");

                deleteAction.apply(superiorPlayer, value);
            }
        });
    }

    private static void disableDatabaseBridge(SuperiorPlayer superiorPlayer, PlayerAction action) throws RequestHandlerException {
        try {
            superiorPlayer.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.IDLE);
            action.run();
        } finally {
            superiorPlayer.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.SAVE_DATA);
        }
    }

    interface PlayerAction {

        void run() throws RequestHandlerException;

    }

}
