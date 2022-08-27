package com.bgsoftware.ssbproxybridge.manager.tracker;

public class ServerInfo {

    private int islandsCount = 0;
    private long lastPingTime;

    public ServerInfo() {
        this.updateLastPingTime();
    }

    public void increaseIslandsCount() {
        ++this.islandsCount;
    }

    public int getIslandsCount() {
        return islandsCount;
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
