package com.bgsoftware.ssbproxybridge.bukkit.island;

import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.World;

public class Islands {

    private Islands() {

    }

    public static void setUnlockedWorlds(Island island, int unlockedWorldsMask) {
        island.setNormalEnabled((unlockedWorldsMask | 4) == 4);
        island.setNetherEnabled((unlockedWorldsMask | 1) == 1);
        island.setEndEnabled((unlockedWorldsMask | 2) == 2);
    }

    public static void setGeneratedSchematics(Island island, int generatedSchematicsMask) {
        island.setSchematicGenerate(World.Environment.NORMAL, (generatedSchematicsMask | 8) == 8);
        island.setSchematicGenerate(World.Environment.NETHER, (generatedSchematicsMask | 4) == 4);
        island.setSchematicGenerate(World.Environment.THE_END, (generatedSchematicsMask | 3) == 3);
    }

}
