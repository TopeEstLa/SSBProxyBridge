package com.bgsoftware.ssbproxybridge.manager.tracker;

import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServersTracker {

    private final Map<UUID, String> islandsToServers = new HashMap<>();
    private final Map<String, Counter> serversToIslands = new HashMap<>();

    public void registerNewServer(String serverName) {
        serversToIslands.put(serverName, new Counter());
    }

    public void trackIsland(UUID islandUUID, String serverName) throws IllegalStateException {
        Counter serverIslands = serversToIslands.get(serverName);

        if (serverIslands == null)
            throw new IllegalStateException("Cannot track island for invalid server \"" + serverName + "\"");

        serverIslands.increase();
        islandsToServers.put(islandUUID, serverName);
    }

    @Nullable
    public String getServerForNewIsland() {
        String chosenServer = null;
        int chosenServerIslandsCount = 0;

        for (Map.Entry<String, Counter> entry : serversToIslands.entrySet()) {
            if (chosenServer == null || entry.getValue().get() > chosenServerIslandsCount) {
                chosenServer = entry.getKey();
                chosenServerIslandsCount = entry.getValue().get();
            }
        }

        return chosenServer;
    }

    @Nullable
    public String getServerOfIsland(UUID islandUUID) {
        return islandsToServers.get(islandUUID);
    }

    public Map<String, Counter> getServers() {
        return Collections.unmodifiableMap(this.serversToIslands);
    }

    public void clear() {
        this.serversToIslands.clear();
        this.islandsToServers.clear();
    }
}
