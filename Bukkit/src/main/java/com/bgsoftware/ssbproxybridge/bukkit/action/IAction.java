package com.bgsoftware.ssbproxybridge.bukkit.action;

import com.google.gson.JsonObject;

public interface IAction<T> {

    boolean run(JsonObject dataObject, T data);

}
