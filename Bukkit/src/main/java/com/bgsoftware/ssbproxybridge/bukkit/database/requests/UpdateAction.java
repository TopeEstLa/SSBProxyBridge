package com.bgsoftware.ssbproxybridge.bukkit.database.requests;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.gson.JsonPrimitive;

public interface UpdateAction {

    void apply(SuperiorPlayer superiorPlayer, JsonPrimitive value);

}
