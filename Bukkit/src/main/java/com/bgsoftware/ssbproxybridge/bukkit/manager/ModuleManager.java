package com.bgsoftware.ssbproxybridge.bukkit.manager;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.core.connector.ConnectionFailureException;
import com.bgsoftware.ssbproxybridge.core.connector.EmptyConnector;
import com.bgsoftware.ssbproxybridge.core.connector.IConnectionArguments;
import com.bgsoftware.ssbproxybridge.core.connector.IConnector;
import com.bgsoftware.ssbproxybridge.core.http.HttpConnectionArguments;
import com.bgsoftware.ssbproxybridge.core.http.HttpConnector;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ModuleManager {

    private static final Gson gson = new Gson();

    private final SSBProxyBridgeModule module;

    private long requestIds = 0;

    @SuppressWarnings("rawtypes")
    private IConnector connector = EmptyConnector.getInstance();

    public ModuleManager(SSBProxyBridgeModule module) {
        this.module = module;
    }

    public void setupManager() {
        IConnectionArguments connectionArguments;

        boolean sendHello = false;

        switch (module.getSettings().managerType.toUpperCase(Locale.ENGLISH)) {
            case "REST":
                this.connector = HttpConnector.getConnector();
                connectionArguments = new HttpConnectionArguments(module.getSettings().managerRestUrl,
                        module.getSettings().managerRestSecret);
                sendHello = true;
                break;
            case "PROXY":
                // TODO
                return;
            default:
                throw new RuntimeException("Invalid connector: " + module.getSettings().managerType);
        }

        try {
            // noinspection unchecked
            this.connector.connect(connectionArguments);
            if (sendHello)
                sendPing();
        } catch (ConnectionFailureException error) {
            throw new RuntimeException("Failed to connect to manager connector:", error);
        }
    }

    public boolean isLocalIsland(UUID islandUUID) {
        JsonObject response = sendRequest(RequestType.CHECK_ISLAND, islandUUID.toString()).join();
        return module.getSettings().serverName.equals(response.get("result").getAsString());
    }

    public CompletableFuture<JsonObject> getServerForNextIsland(UUID islandUUID) {
        return sendRequest(RequestType.CREATE_ISLAND, islandUUID.toString());
    }

    public void sendPing() {
        CompletableFuture<JsonObject> responseFuture = sendRequest(RequestType.HELLO, "");

        try {
            // Should block until we get a response.
            JsonObject response = responseFuture.get(10, TimeUnit.SECONDS);
            System.out.println(response);
            if (response.has("error"))
                throw new RuntimeException("Failed to register to the manager: " + response.get("error").getAsString());
        } catch (Exception error) {
            throw new RuntimeException("Failed to connect to the manager, aborting.", error);
        }
    }

    private CompletableFuture<JsonObject> sendRequest(RequestType requestType, String params) {
        JsonObject args = new JsonObject();

        long requestId = this.requestIds++;

        args.addProperty("method", requestType.getMethod());
        args.addProperty("id", requestId);
        args.addProperty("route", requestType.getRoute() + params);
        args.addProperty("server", module.getSettings().serverName);

        this.connector.sendData(requestType.name(), gson.toJson(args));

        CompletableFuture<JsonObject> response = new CompletableFuture<>();

        this.connector.listenOnce(requestType.name(), responseData -> {
            try {
                JsonObject responseBody = gson.fromJson(responseData, JsonObject.class);
                if (requestId == responseBody.get("id").getAsLong())
                    response.complete(responseBody);
            } catch (JsonSyntaxException error) {
                System.out.println(responseData);
                error.printStackTrace();
            }
        });

        return response;
    }

}
