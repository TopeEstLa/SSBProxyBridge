package com.bgsoftware.ssbproxybridge.bukkit.proxy;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.plugin.messaging.PluginMessageRecipient;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ProxyPlayerBridge {

    private static final String CHANNEL_NAME = "BungeeCord";

    private static final Map<String, List<IListener>> listeners = new HashMap<>();

    private static Plugin plugin;

    private ProxyPlayerBridge() {

    }

    interface IListener {

        void onReceive(ByteArrayDataInput dataInput);

    }

    public static void register(Plugin plugin) {
        ProxyPlayerBridge.plugin = plugin;
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL_NAME);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, CHANNEL_NAME, new ListenerImpl());
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

    public static CompletableFuture<String> fetchServerName(PluginMessageRecipient recipient) {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        if (plugin == null) {
            completableFuture.completeExceptionally(new NullPointerException("plugin is null"));
            return completableFuture;
        }

        ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
        dataOutput.writeUTF("GetServer");
        recipient.sendPluginMessage(plugin, CHANNEL_NAME, dataOutput.toByteArray());
        listenOnce("GetServer", dataInput -> {
            String serverName = dataInput.readUTF();
            completableFuture.complete(serverName);
        });
        return completableFuture;
    }

    private static void registerListener(String channel, IListener listener) {
        listeners.computeIfAbsent(channel, ch -> new LinkedList<>()).add(listener);
    }

    private static void unregisterListener(String channel, IListener listener) {
        List<IListener> listeners = ProxyPlayerBridge.listeners.get(channel);
        if (listeners != null)
            listeners.remove(listener);
    }

    private static void listenOnce(String channel, IListener listener) {
        registerListener(channel, new IListener() {
            @Override
            public void onReceive(ByteArrayDataInput dataInput) {
                try {
                    listener.onReceive(dataInput);
                } finally {
                    unregisterListener(channel, this);
                }
            }
        });
    }

    private static void notifyListeners(String channel, byte[] data) {
        List<IListener> listeners = ProxyPlayerBridge.listeners.get(channel);
        if (listeners != null) {
            listeners.forEach(listener -> {
                ByteArrayDataInput dataInput = ByteStreams.newDataInput(data);
                dataInput.readUTF(); // Read channel name
                listener.onReceive(dataInput);
            });
        }
    }

    private static class ListenerImpl implements PluginMessageListener {

        @Override
        public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
            if (!channel.equals(CHANNEL_NAME))
                return;

            ByteArrayDataInput dataInput = ByteStreams.newDataInput(bytes);
            String subChannel = dataInput.readUTF();
            notifyListeners(subChannel, bytes);
        }

    }

}
