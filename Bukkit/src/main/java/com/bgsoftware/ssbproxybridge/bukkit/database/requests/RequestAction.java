package com.bgsoftware.ssbproxybridge.bukkit.database.requests;

import com.google.gson.JsonElement;

public interface RequestAction<E, V extends JsonElement> {

    void apply(E element, V value) throws RequestHandlerException;

}
