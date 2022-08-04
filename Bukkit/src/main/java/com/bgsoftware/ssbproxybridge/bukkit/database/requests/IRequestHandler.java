package com.bgsoftware.ssbproxybridge.bukkit.database.requests;

import com.google.gson.JsonObject;

public interface IRequestHandler {

    void handle(JsonObject data) throws RequestHandlerException;

}
