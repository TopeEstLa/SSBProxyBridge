package com.bgsoftware.ssbproxybridge.manager.tracker;

import com.bgsoftware.ssbproxybridge.manager.ManagerServer;
import com.bgsoftware.ssbproxybridge.manager.util.Pair;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ServersTracker {

    private final Map<UUID, IslandInfo> islands = new HashMap<>();
    private final Map<String, ServerInfo> servers = new HashMap<>();

    private final ManagerServer managerServer;

    public ServersTracker(ManagerServer managerServer) {
        this.managerServer = managerServer;
    }

    public void registerNewServer(String serverName) {
        ServerInfo oldServerInfo = servers.put(serverName, new ServerInfo(serverName));
        if (oldServerInfo != null)
            oldServerInfo.getServerIslands().forEach(islandInfo -> islands.remove(islandInfo.getUniqueId()));
    }

    @Nullable
    public ServerInfo getServerInfo(String serverName) {
        return servers.get(serverName);
    }

    public void trackIsland(UUID islandUUID, String serverName) throws IllegalStateException {
        ServerInfo serverInfo = servers.get(serverName);

        if (serverInfo == null)
            throw new IllegalStateException("Cannot track island for invalid server \"" + serverName + "\"");

        IslandInfo islandInfo = new IslandInfo(serverName, islandUUID);

        serverInfo.addIsland(islandInfo);
        islands.put(islandUUID, islandInfo);
    }

    public void untrackIsland(UUID islandUUID) {
        IslandInfo islandInfo = islands.remove(islandUUID);
        if (islandInfo != null) {
            ServerInfo serverInfo = servers.get(islandInfo.getServerName());
            if (serverInfo != null) {
                serverInfo.removeIsland(islandInfo);
            }
        }
    }

    @Nullable
    public String getServerForNewIsland() {
        List<Pair<ServerInfo, Integer>> serverInfoList = new ArrayList<>();

        servers.values().forEach(serverInfo -> {
            if (!checkLastPing(serverInfo) || managerServer.getConfig().excludedServers.contains(serverInfo.getServerName()))
                return;

            int activeIslandsCount = 0;

            for (IslandInfo islandInfo : serverInfo.getServerIslands()) {
                if (checkIfActive(islandInfo))
                    ++activeIslandsCount;
            }

            serverInfoList.add(new Pair<>(serverInfo, activeIslandsCount));
        });

        if (serverInfoList.isEmpty()) {
            return null;
        }

        if (serverInfoList.size() > 1)
            serverInfoList.sort(Comparator.comparingInt(o -> o.second));

        return serverInfoList.get(0).first.getServerName();
    }

    @Nullable
    public IslandInfo getIslandInfo(UUID islandUUID) {
        return islands.get(islandUUID);
    }

    public Map<String, ServerInfo> getServers() {
        return Collections.unmodifiableMap(this.servers);
    }

    public void clear() {
        this.servers.clear();
        this.islands.clear();
    }

    public boolean checkLastPing(ServerInfo serverInfo) {
        long timeFromLastPing = System.currentTimeMillis() - serverInfo.getLastPingTime();
        return timeFromLastPing <= managerServer.getConfig().keepAlive * 2;
    }

    private boolean checkIfActive(IslandInfo islandInfo) {
        if (managerServer.getConfig().inactiveTime < 0)
            return true;

        long timeFromLastUpdate = System.currentTimeMillis() - islandInfo.getLastUpdateTime();
        return timeFromLastUpdate <= managerServer.getConfig().inactiveTime;
    }

}
