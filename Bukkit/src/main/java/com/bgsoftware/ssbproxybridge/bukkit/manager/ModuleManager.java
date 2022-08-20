package com.bgsoftware.ssbproxybridge.bukkit.manager;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.core.connector.EmptyConnector;
import com.bgsoftware.ssbproxybridge.core.connector.IConnector;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ModuleManager {

    private static final String MANAGER_CHANNEL = "ssbproxybridge-manager"; // TODO - Actual configurable

    private static final Gson gson = new Gson();

    private final SSBProxyBridgeModule module;

    private long requestIds = 0;

    @SuppressWarnings("rawtypes")
    private IConnector connector = EmptyConnector.getInstance();

    public ModuleManager(SSBProxyBridgeModule module) {
        this.module = module;
    }

    public void setupManager() {
        // TODO - Setup connector
        this.sendPing();
    }

    public boolean isLocalIsland(UUID islandUUID) {
        JsonObject request = new JsonObject();
        request.addProperty("island", islandUUID.toString());

        JsonObject response = sendRequest(RequestType.CHECK_ISLAND, request).join();

        return response.get("result").getAsBoolean();
    }

    public String getServerForNextIsland() {
        JsonObject request = new JsonObject();

        JsonObject response = sendRequest(RequestType.CREATE_ISLAND, request).join();

        return response.get("result").getAsString();
    }

    public void sendPing() {
        JsonObject helloRequest = new JsonObject();

        CompletableFuture<JsonObject> responseFuture = sendRequest(RequestType.HELLO, helloRequest);

        try {
            // Should block until we get a response.
            JsonObject response = responseFuture.get(10, TimeUnit.SECONDS);
            if (response.has("error"))
                throw new RuntimeException("Failed to register to the manager: " + response.get("error").getAsString());
        } catch (Exception error) {
            throw new RuntimeException("Failed to connect to the manager, aborting.", error);
        }
    }

    private CompletableFuture<JsonObject> sendRequest(RequestType requestType, JsonObject requestBody) {
        requestBody.addProperty("type", requestType.name());
        requestBody.addProperty("server", module.getSettings().serverName);

        long requestId = this.requestIds++;

        requestBody.addProperty("id", requestId);

        this.connector.sendData(MANAGER_CHANNEL, gson.toJson(requestBody));

        CompletableFuture<JsonObject> response = new CompletableFuture<>();

        this.connector.listenOnce(MANAGER_CHANNEL, responseData -> {
            JsonObject responseBody = gson.fromJson(responseData, JsonObject.class);

            long responseId = responseBody.get("id").getAsLong();

            if (responseId == requestId)
                response.complete(responseBody);
        });

        return response;
    }

}
