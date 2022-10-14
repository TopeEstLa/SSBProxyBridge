package com.bgsoftware.ssbproxybridge.bukkit.utils;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class BukkitExecutor {

    private static final SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

    private BukkitExecutor() {

    }

    public static void runTask(Runnable task) {
        Bukkit.getScheduler().runTask(module.getPlugin(), task);
    }

    public static BukkitTask runTaskTimerAsynchronously(Runnable task, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(module.getPlugin(), task, delay, period);
    }

    public static void runTaskLater(Runnable task, long delay) {
        Bukkit.getScheduler().runTaskLater(module.getPlugin(), task, delay);
    }

}
