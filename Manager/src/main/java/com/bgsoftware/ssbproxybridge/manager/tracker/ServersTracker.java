package com.bgsoftware.ssbproxybridge.manager.tracker;

import com.bgsoftware.ssbproxybridge.manager.ManagerServer;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ServersTracker {

    private final Map<UUID, String> islandsToServers = new HashMap<>();
    private final Map<String, ServerInfo> servers = new HashMap<>();

    private final ManagerServer managerServer;

    public ServersTracker(ManagerServer managerServer) {
        this.managerServer = managerServer;
    }

    public void registerNewServer(String serverName) {
        servers.put(serverName, new ServerInfo(serverName));
    }

    @Nullable
    public ServerInfo getServerInfo(String serverName) {
        return servers.get(serverName);
    }

    public void trackIsland(UUID islandUUID, String serverName) throws IllegalStateException {
        ServerInfo serverInfo = servers.get(serverName);

        if (serverInfo == null)
            throw new IllegalStateException("Cannot track island for invalid server \"" + serverName + "\"");

        serverInfo.increaseIslandsCount();
        serverInfo.updateLastPingTime();
        islandsToServers.put(islandUUID, serverName);
    }

    public void untrackIsland(UUID islandUUID) {
        String serverName = islandsToServers.remove(islandUUID);
        if (serverName != null) {
            ServerInfo serverInfo = servers.get(serverName);
            if (serverInfo != null) {
                serverInfo.decreaseIslandsCount();
                serverInfo.updateLastPingTime();
            }
        }
    }

    @Nullable
    public String getServerForNewIsland() {
        List<ServerInfo> serverInfoList = new ArrayList<>(servers.values());
        serverInfoList.sort(Comparator.comparingInt(ServerInfo::getIslandsCount));

        for (ServerInfo serverInfo : serverInfoList) {
            if (checkLastPing(serverInfo) && !managerServer.getConfig().excludedServers.contains(serverInfo.getServerName()))
                return serverInfo.getServerName();
        }

        return null;
    }

    @Nullable
    public String getServerOfIsland(UUID islandUUID) {
        return islandsToServers.get(islandUUID);
    }

    public Map<String, ServerInfo> getServers() {
        return Collections.unmodifiableMap(this.servers);
    }

    public void clear() {
        this.servers.clear();
        this.islandsToServers.clear();
    }

    private boolean checkLastPing(ServerInfo serverInfo) {
        long timeFromLastPing = System.currentTimeMillis() - serverInfo.getLastPingTime();
        return timeFromLastPing <= managerServer.getConfig().keepAlive * 2;
    }

}
