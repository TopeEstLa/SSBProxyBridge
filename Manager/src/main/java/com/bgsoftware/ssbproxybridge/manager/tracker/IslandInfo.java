package com.bgsoftware.ssbproxybridge.manager.tracker;

import java.util.Objects;
import java.util.UUID;

public class IslandInfo {

    private final String serverName;
    private final UUID islandUUID;
    private long lastUpdateTime;


    public IslandInfo(String serverName, UUID islandUUID) {
        this.serverName = serverName;
        this.islandUUID = islandUUID;
        this.updateLastUpdateTime();
    }

    public String getServerName() {
        return serverName;
    }

    public UUID getUniqueId() {
        return islandUUID;
    }

    public void updateLastUpdateTime() {
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IslandInfo that = (IslandInfo) o;
        return islandUUID.equals(that.islandUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(islandUUID);
    }

}
