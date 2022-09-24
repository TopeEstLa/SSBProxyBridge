package com.bgsoftware.ssbproxybridge.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import reactor.util.annotation.Nullable;

public class JsonUtil {

    private JsonUtil() {

    }

    @Nullable
    public static JsonElement getJsonFromObject(Object object) {
        if (object instanceof String) {
            return new JsonPrimitive((String) object);
        }
        if (object instanceof byte[]) {
            return new JsonPrimitive(new String((byte[]) object));
        } else if (object instanceof Number) {
            return new JsonPrimitive((Number) object);
        } else if (object instanceof Boolean) {
            return new JsonPrimitive((Boolean) object);
        } else if (object instanceof Character) {
            return new JsonPrimitive((Character) object);
        } else {
            return null;
        }
    }

    @Nullable
    public static Object getValueFromPrimitive(JsonPrimitive jsonPrimitive) {
        if (jsonPrimitive.isString()) {
            return jsonPrimitive.getAsString();
        } else if (jsonPrimitive.isBoolean()) {
            return jsonPrimitive.getAsBoolean();
        } else if (jsonPrimitive.isNumber()) {
            return jsonPrimitive.getAsNumber();
        } else {
            return null;
        }
    }

}
