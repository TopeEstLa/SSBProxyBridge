package com.bgsoftware.ssbproxybridge.core.bundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class BundleSerializer implements JsonSerializer<Bundle> {

    /* package */ static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Bundle.class, new BundleSerializer())
            .create();

    public static String serializeBundle(Bundle bundle) {
        return GSON.toJson(bundle);
    }

    @Override
    public JsonElement serialize(Bundle src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject output = src.getJson();

        if (src.getChannelName() != null)
            output.addProperty("$channel", src.getChannelName());
        if (src.getSender() != null)
            output.addProperty("$sender", src.getSender());
        if (src.getRecipients() != null && !src.getRecipients().isEmpty()) {
            JsonArray recipients = new JsonArray();
            src.getRecipients().forEach(recipients::add);
            output.add("$recipients", recipients);
        }

        return output;
    }

}
