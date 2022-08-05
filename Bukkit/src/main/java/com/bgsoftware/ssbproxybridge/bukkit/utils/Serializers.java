package com.bgsoftware.ssbproxybridge.bukkit.utils;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.math.BigInteger;

public class Serializers {

    private Serializers() {

    }

    @Nullable
    public static Location deserializeLocation(String serialized) {
        if (serialized == null || serialized.isEmpty())
            return null;

        String[] sections = serialized.split(",");

        double x = Double.parseDouble(sections[1]);
        double y = Double.parseDouble(sections[2]);
        double z = Double.parseDouble(sections[3]);
        float yaw = sections.length > 5 ? Float.parseFloat(sections[4]) : 0;
        float pitch = sections.length > 4 ? Float.parseFloat(sections[5]) : 0;

        return new LazyWorldLocation(sections[0], x, y, z, yaw, pitch);
    }

}
