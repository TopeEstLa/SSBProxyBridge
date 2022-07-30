package com.bgsoftware.ssbproxybridge.bukkit.database;

import com.bgsoftware.ssbproxybridge.core.messaging.IConnector;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.bukkit.Bukkit;

public class DatabaseBridgeListener implements IConnector.IListener {

    private static final Gson gson = new Gson();

    @Override
    public void onReceive(String data) {
        JsonElement jsonElement = gson.fromJson(data, JsonElement.class);
        Bukkit.broadcastMessage("Received data: " + jsonElement);
    }

}
