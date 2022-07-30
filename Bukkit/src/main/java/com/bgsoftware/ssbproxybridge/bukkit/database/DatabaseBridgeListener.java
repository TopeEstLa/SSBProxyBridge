package com.bgsoftware.ssbproxybridge.bukkit.database;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.core.connector.IConnector;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

public class DatabaseBridgeListener implements IConnector.IListener {

    private static final Gson gson = new Gson();

    @Override
    public void onReceive(String data) {
        JsonElement dataElement = gson.fromJson(data, JsonElement.class);

        if (dataElement instanceof JsonObject) {
            handleDataObject((JsonObject) dataElement);
        } else if (dataElement instanceof JsonArray) {
            for (JsonElement jsonElement : (JsonArray) dataElement) {
                if (jsonElement instanceof JsonObject)
                    handleDataObject((JsonObject) jsonElement);
            }
        }

    }

    private void handleDataObject(JsonObject dataObject) {
        String senderName = dataObject.get("sender").getAsString();
        String currentServerName = SSBProxyBridgeModule.getModule().getServerName();

        if (senderName.equals(currentServerName))
            return;

        JsonElement recipients = dataObject.get("recipients"); // Option to send requests for specific servers.
        if (recipients instanceof JsonArray) {
            for (JsonElement recipient : (JsonArray) recipients) {
                if (recipient.getAsString().equals(currentServerName)) {
                    processRequest(dataObject);
                    return;
                }
            }

            return;
        }

        processRequest(dataObject);
    }

    private void processRequest(JsonObject dataObject) {
        // TODO: Process request
        Bukkit.broadcastMessage(dataObject + "");
    }

}
