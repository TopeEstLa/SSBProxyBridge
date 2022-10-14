package com.bgsoftware.ssbproxybridge.core;

import com.bgsoftware.ssbproxybridge.core.bundle.Bundle;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    private JsonUtil() {

    }

    public static JsonElement getJsonFromObject(Object object) {
        if (object instanceof String) {
            return new JsonPrimitive((String) object);
        } else if (object instanceof byte[]) {
            return new JsonPrimitive(new String((byte[]) object));
        } else if (object instanceof Number) {
            return new JsonPrimitive((Number) object);
        } else if (object instanceof Boolean) {
            return new JsonPrimitive((Boolean) object);
        } else if (object instanceof Character) {
            return new JsonPrimitive((Character) object);
        } else if (object instanceof List) {
            JsonArray array = new JsonArray();
            ((List<?>) object).forEach(element -> array.add(getJsonFromObject(element)));
            return array;
        } else if (object instanceof Map) {
            JsonObject jsonObject = new JsonObject();
            ((Map<?, ?>) object).forEach((key, value) -> jsonObject.add(key.toString(), getJsonFromObject(value)));
            return jsonObject;
        } else if (object instanceof Bundle) {
            return ((Bundle) object).getJson();
        }

        throw new IllegalStateException("Cannot find valid json for " + object);
    }

    public static Object getValueFromElement(JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
            if (jsonPrimitive.isString()) {
                return jsonPrimitive.getAsString();
            } else if (jsonPrimitive.isBoolean()) {
                return jsonPrimitive.getAsBoolean();
            } else if (jsonPrimitive.isNumber()) {
                return jsonPrimitive.getAsNumber();
            }
        } else if (jsonElement.isJsonArray()) {
            List<Object> array = new LinkedList<>();
            jsonElement.getAsJsonArray().forEach(element -> array.add(getValueFromElement(element)));
            return Collections.unmodifiableList(array);
        } else if (jsonElement.isJsonObject()) {
            Map<String, Object> map = new LinkedHashMap<>();
            jsonElement.getAsJsonObject().entrySet().forEach(entry -> map.put(entry.getKey(), getValueFromElement(entry.getValue())));
            return Collections.unmodifiableMap(map);
        }

        throw new IllegalStateException("Cannot find valid object from json " + jsonElement);
    }

}
