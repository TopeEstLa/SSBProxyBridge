package com.bgsoftware.ssbproxybridge.core.requests;

import com.bgsoftware.ssbproxybridge.core.requests.RequestHandlerException;
import com.google.gson.JsonElement;

public interface RequestAction<E, V extends JsonElement> {

    void apply(E element, V value) throws RequestHandlerException;

}
