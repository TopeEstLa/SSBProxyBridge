package com.bgsoftware.ssbproxybridge.manager.tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerInfo {

    private final List<IslandInfo> serverIslands = new ArrayList<>();
    private final String serverName;
    private long lastPingTime;


    public ServerInfo(String serverName) {
        this.serverName = serverName;
        this.updateLastPingTime();
    }

    public String getServerName() {
        return serverName;
    }

    public void addIsland(IslandInfo islandInfo) {
        serverIslands.add(islandInfo);
        updateLastPingTime();
    }

    public void removeIsland(IslandInfo islandInfo) {
        serverIslands.remove(islandInfo);
        updateLastPingTime();
    }

    public int getIslandsCount() {
        return serverIslands.size();
    }

    public List<IslandInfo> getServerIslands() {
        return Collections.unmodifiableList(serverIslands);
    }

    public void updateLastPingTime() {
        this.lastPingTime = System.currentTimeMillis();
    }

    public void sleep() {
        this.lastPingTime = 0;
    }

    public long getLastPingTime() {
        return lastPingTime;
    }

}
