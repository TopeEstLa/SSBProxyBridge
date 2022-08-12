package com.bgsoftware.ssbproxybridge.bukkit.proxy;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.core.Singleton;
import com.bgsoftware.ssbproxybridge.core.connector.ConnectorAbstract;
import com.bgsoftware.ssbproxybridge.core.connector.EmptyConnectionArguments;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.LinkedList;
import java.util.List;

public class ProxyConnector extends ConnectorAbstract<EmptyConnectionArguments> {

    private static final SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

    private static final Singleton<ProxyConnector> SINGLETON = new Singleton<ProxyConnector>() {
        @Override
        protected ProxyConnector create() {
            return new ProxyConnector();
        }
    };

    public static final String CHANNEL_NAME = "SSBProxyBridge";

    private final List<byte[]> pendingRequests = new LinkedList<>();

    public static ProxyConnector getConnector() {
        return SINGLETON.get();
    }

    private ProxyConnector() {

    }

    @Override
    public void connect(EmptyConnectionArguments unused) {
        Plugin plugin = module.getPlugin();
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL_NAME);
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, CHANNEL_NAME, new ListenerImpl());
    }

    @Override
    public void shutdown() {
        Plugin plugin = module.getPlugin();
        Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin, CHANNEL_NAME);
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin, CHANNEL_NAME);
    }

    @Override
    public void sendData(String channel, String data) {
        ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();

        dataOutput.writeInt(channel.length());
        dataOutput.write(channel.getBytes());

        dataOutput.writeInt(data.length());
        dataOutput.write(data.getBytes());

        Player onlinePlayer = Bukkit.getOnlinePlayers().stream().findAny().orElse(null);

        if (onlinePlayer == null) {
            this.pendingRequests.add(dataOutput.toByteArray());
        } else {
            Plugin plugin = module.getPlugin();
            onlinePlayer.sendPluginMessage(plugin, CHANNEL_NAME, dataOutput.toByteArray());
        }
    }

    public void flushPendingRequests(Player player) {
        Plugin plugin = module.getPlugin();
        pendingRequests.forEach(data -> player.sendPluginMessage(plugin, CHANNEL_NAME, data));
    }

    private class ListenerImpl implements PluginMessageListener {

        @Override
        public void onPluginMessageReceived(String channel, Player player, byte[] data) {
            ByteArrayDataInput dataInput = ByteStreams.newDataInput(data);

            int channelNameLength = dataInput.readInt();
            byte[] channelNameBytes = new byte[channelNameLength];
            dataInput.readFully(channelNameBytes);

            String channelName = new String(channelNameBytes);

            int bodyLength = dataInput.readInt();
            byte[] bodyBytes = new byte[bodyLength];
            dataInput.readFully(bodyBytes);

            String body = new String(bodyBytes);

            notifyListeners(channelName, body);
        }

    }

}
