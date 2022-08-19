package com.bgsoftware.ssbproxybridge.bukkit.proxy;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageRecipient;

public class ProxyPlayerBridge {

    private static final String CHANNEL_NAME = "BungeeCord";

    private static Plugin plugin;

    private ProxyPlayerBridge() {

    }

    public static void register(Plugin plugin) {
        ProxyPlayerBridge.plugin = plugin;
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL_NAME);
    }

    public static void teleportPlayer(Player player, String targetServer) {
        if (plugin != null) {
            ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
            dataOutput.writeUTF("Connect");
            dataOutput.writeUTF(targetServer);
            player.sendPluginMessage(plugin, CHANNEL_NAME, dataOutput.toByteArray());
        }
    }

    public static void sendMessage(PluginMessageRecipient recipient, String otherPlayer, String message) {
        if (plugin != null) {
            ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
            dataOutput.writeUTF("MessageRaw");
            dataOutput.writeUTF(otherPlayer);
            dataOutput.writeUTF(message);
            recipient.sendPluginMessage(plugin, CHANNEL_NAME, dataOutput.toByteArray());
        }
    }

}
