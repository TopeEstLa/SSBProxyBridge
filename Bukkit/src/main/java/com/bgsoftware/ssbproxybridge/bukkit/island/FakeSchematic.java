package com.bgsoftware.ssbproxybridge.bukkit.island;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * This class is used for creating a new island object so this module doesn't need to handle all of its logic alone.
 */
public class FakeSchematic implements Schematic {

    private final String name;

    public FakeSchematic(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void pasteSchematic(Island island, Location location, Runnable callback) {
        this.pasteSchematic(island, location, callback, null);
    }

    @Override
    public void pasteSchematic(Island island, Location location, Runnable callback, @Nullable Consumer<Throwable> onError) {
        // We just call the callback as we do not actually paste the schematic.
        callback.run();
    }

    @Override
    public Location adjustRotation(Location location) {
        // We do not care about this.
        return location;
    }

}
