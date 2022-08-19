package com.bgsoftware.ssbproxybridge.bukkit.database;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.connector.JsonConnectorListener;
import com.bgsoftware.ssbproxybridge.bukkit.database.requests.IRequestHandler;
import com.bgsoftware.ssbproxybridge.bukkit.database.requests.IslandRequests;
import com.bgsoftware.ssbproxybridge.bukkit.database.requests.PlayerRequests;
import com.bgsoftware.ssbproxybridge.bukkit.database.requests.RequestHandlerException;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Map;

public class DatabaseBridgeListener extends JsonConnectorListener {

    private static final Map<String, IRequestHandler> REQUEST_HANDLERS = new ImmutableMap.Builder<String, IRequestHandler>()
            .put("players", PlayerRequests::handleRequest)
            .put("players_settings", PlayerRequests::handleRequest)
            .put("players_missions", PlayerRequests::handleRequest)
            .put("players_custom_data", PlayerRequests::handleRequest)
            .put("islands", IslandRequests::handleRequest)
            .put("islands_members", IslandRequests::handleRequest)
            .put("islands_bans", IslandRequests::handleRequest)
            .put("islands_settings", IslandRequests::handleRequest)
            .put("islands_homes", IslandRequests::handleRequest)
            .put("islands_visitor_homes", IslandRequests::handleRequest)
            .put("islands_player_permissions", IslandRequests::handleRequest)
            .put("islands_role_permissions", IslandRequests::handleRequest)
            .put("islands_upgrades", IslandRequests::handleRequest)
            .put("islands_block_limits", IslandRequests::handleRequest)
            .put("islands_entity_limits", IslandRequests::handleRequest)
            .put("islands_effects", IslandRequests::handleRequest)
            .put("islands_role_limits", IslandRequests::handleRequest)
            .put("islands_warps", IslandRequests::handleRequest)
            .put("islands_ratings", IslandRequests::handleRequest)
            .put("islands_missions", IslandRequests::handleRequest)
            .put("islands_flags", IslandRequests::handleRequest)
            .put("islands_generators", IslandRequests::handleRequest)
            .put("islands_chests", IslandRequests::handleRequest)
            .put("islands_banks", IslandRequests::handleRequest)
            .put("islands_visitors", IslandRequests::handleRequest)
            .put("islands_warp_categories", IslandRequests::handleRequest)
            .put("bank_transactions", IslandRequests::handleRequest)
            .put("islands_custom_data", IslandRequests::handleRequest)
            .build();

    public DatabaseBridgeListener(SSBProxyBridgeModule module) {
        super(module, module.getSettings().messagingServiceDataChannelName);
    }

    @Override
    protected void processRequest(JsonObject dataObject) {
        JsonElement tableElement = dataObject.get("table");
        String table = tableElement instanceof JsonPrimitive ? tableElement.getAsString() : "";
        try {
            REQUEST_HANDLERS.getOrDefault(table, this::handleRequestsFallback).handle(dataObject);
        } catch (RequestHandlerException error) {
            this.module.getLogger().warning("Received an unexpected error while handling request:");
            this.module.getLogger().warning(dataObject + "");
            error.printStackTrace();
        }
    }



}
