package com.bgsoftware.ssbproxybridge.core.requests;

import com.google.gson.JsonObject;

public interface IRequestHandler {

    void handle(JsonObject data) throws RequestHandlerException;

}
