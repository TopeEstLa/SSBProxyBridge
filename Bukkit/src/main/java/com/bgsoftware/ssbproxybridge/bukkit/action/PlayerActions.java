package com.bgsoftware.ssbproxybridge.bukkit.action;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerActions {

    private static final SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();
    private static final Gson gson = new Gson();

    private PlayerActions() {

    }

    public static void teleportToIsland(Player player, String targetServer, UUID islandUUID) {
        JsonObject data = new JsonObject();
        data.addProperty("action", "teleport");
        data.addProperty("island", islandUUID.toString());
        sendData(player, targetServer, data);
    }

    private static void sendData(Player player, String server, JsonObject data) {
        data.addProperty("sender", module.getSettings().serverName);
        data.addProperty("channel", module.getSettings().messagingServiceActionsChannelName);
        data.addProperty("player", player.getUniqueId().toString());

        JsonArray recipients = new JsonArray();
        recipients.add(new JsonPrimitive(server));
        data.add("recipients", recipients);

        module.getMessaging().sendData(module.getSettings().messagingServiceActionsChannelName, gson.toJson(data));
    }

}
