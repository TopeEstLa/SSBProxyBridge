package com.bgsoftware.ssbproxybridge.bukkit.island;

import com.bgsoftware.ssbproxybridge.bukkit.database.requests.RequestHandlerException;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.missions.IMissionsHolder;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.google.gson.JsonObject;
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

    public static void setMissionCompletedCount(IMissionsHolder missionsHolder, JsonObject data) throws RequestHandlerException {
        setMissionCompletedCount(missionsHolder, data.get("name").getAsString(), data.get("finish_count").getAsInt());
    }

    public static void setMissionCompletedCount(IMissionsHolder missionsHolder, String missionName, int newFinishCount) throws RequestHandlerException {
        Mission<?> mission = SuperiorSkyblockAPI.getMissions().getMission(missionName);

        if (mission == null)
            throw new RequestHandlerException("Cannot find a valid mission \"" + missionName + "\"");

        int currentFinishCount = missionsHolder.getAmountMissionCompleted(mission);
        int countsDelta = Math.abs(currentFinishCount - newFinishCount);

        if (currentFinishCount == newFinishCount)
            return;

        // TODO: Change it to setAmountMissionCompleted using the new API method
        if (currentFinishCount > newFinishCount) {
            for (int i = 0; i < countsDelta; ++i)
                missionsHolder.resetMission(mission);
        } else {
            for (int i = 0; i < countsDelta; ++i)
                missionsHolder.completeMission(mission);
        }
    }

}
