package com.bgsoftware.ssbproxybridge.bukkit.connector;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.core.connector.IConnector;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class JsonConnectorListener implements IConnector.IListener {

    private static final Gson gson = new Gson();

    protected final SSBProxyBridgeModule module;
    protected final String listeningChannelName;

    protected JsonConnectorListener(SSBProxyBridgeModule module, String listeningChannelName) {
        this.module = module;
        this.listeningChannelName = listeningChannelName;
    }

    @Override
    public final void onReceive(String data) {
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
        String channel = dataObject.get("channel").getAsString();

        if (!channel.equals(listeningChannelName))
            return;

        String senderName = dataObject.get("sender").getAsString();
        String currentServerName = module.getSettings().serverName;

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

    protected abstract void processRequest(JsonObject dataObject);

    protected void handleRequestsFallback(JsonObject dataObject) {
        this.module.getLogger().warning("Received data without an appropriate handler:");
        this.module.getLogger().warning(dataObject + "");
    }

}
