package com.bgsoftware.ssbproxybridge.bukkit.utils;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import org.bukkit.Bukkit;

public class BukkitExecutor {

    private static final SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

    private BukkitExecutor() {

    }

    public static void runTask(Runnable runnable) {
        Bukkit.getScheduler().runTask(module.getPlugin(), runnable);
    }

}
