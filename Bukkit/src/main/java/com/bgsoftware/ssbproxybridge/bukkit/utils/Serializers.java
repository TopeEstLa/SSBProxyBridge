package com.bgsoftware.ssbproxybridge.bukkit.utils;

import org.bukkit.Location;

import javax.annotation.Nullable;

public class Serializers {

    private Serializers() {

    }

    @Nullable
    public static Location deserializeLocation(@Nullable String serialized) {
        if (Text.isBlank(serialized))
            return null;

        String[] sections = serialized.split(",");

        double x = Double.parseDouble(sections[1]);
        double y = Double.parseDouble(sections[2]);
        double z = Double.parseDouble(sections[3]);
        float yaw = sections.length > 5 ? Float.parseFloat(sections[4]) : 0;
        float pitch = sections.length > 4 ? Float.parseFloat(sections[5]) : 0;

        return new LazyWorldLocation(sections[0], x, y, z, yaw, pitch);
    }

    @Nullable
    public static String serializeLocation(@Nullable Location location) {
        if (location == null)
            return null;

        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," +
                location.getZ() + "," + location.getYaw() + "," + location.getPitch();
    }

}
