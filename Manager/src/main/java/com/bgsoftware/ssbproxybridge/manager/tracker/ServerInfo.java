package com.bgsoftware.ssbproxybridge.manager.tracker;

public class ServerInfo {

    private final String serverName;
    private int islandsCount = 0;
    private long lastPingTime;


    public ServerInfo(String serverName) {
        this.serverName = serverName;
        this.updateLastPingTime();
    }

    public String getServerName() {
        return serverName;
    }

    public void increaseIslandsCount() {
        ++this.islandsCount;
    }

    public void decreaseIslandsCount() {
        --this.islandsCount;
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
