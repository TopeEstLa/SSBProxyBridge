package com.bgsoftware.ssbproxybridge.bukkit.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nullable;

public class LazyWorldLocation extends Location {

    private final String worldName;

    public LazyWorldLocation(String worldName, double x, double y, double z, float pitch, float yaw) {
        super(Bukkit.getWorld(worldName), x, y, z, pitch, yaw);
        this.worldName = worldName;
    }

    @Override
    public World getWorld() {
        if (worldName != null)
            setWorld(Bukkit.getWorld(worldName));

        return super.getWorld();
    }

    @Override
    public Location clone() {
        return getWorld() == null ? new LazyWorldLocation(this.worldName, getX(), getY(), getZ(), getPitch(), getYaw()) :
                super.clone();
    }

    public String getWorldName() {
        return worldName;
    }

    @Nullable
    public static String getWorldName(Location location) {
        return location instanceof LazyWorldLocation ? ((LazyWorldLocation) location).getWorldName() :
                location.getWorld() == null ? null : location.getWorld().getName();
    }

}
