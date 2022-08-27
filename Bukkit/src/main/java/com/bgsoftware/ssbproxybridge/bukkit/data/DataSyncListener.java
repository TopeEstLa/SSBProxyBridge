package com.bgsoftware.ssbproxybridge.bukkit.data;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.connector.JsonConnectorListener;
import com.bgsoftware.ssbproxybridge.core.requests.RequestHandlerException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Locale;

public class DataSyncListener extends JsonConnectorListener {

    public DataSyncListener(SSBProxyBridgeModule module) {
        super(module, module.getSettings().messagingServiceDataChannelName);
    }

    @Override
    protected void processRequest(JsonObject dataObject) {
        JsonElement typeElement = dataObject.get("type");

        if (!(typeElement instanceof JsonPrimitive)) {
            handleFailureRequest(dataObject, new RequestHandlerException("Missing field \"type\""));
            return;
        }

        String type = typeElement.getAsString();

        try {
            DataSyncType dataSyncType = DataSyncType.valueOf(type.toUpperCase(Locale.ENGLISH));
            dataSyncType.getHandler().handle(dataObject);
        } catch (IllegalArgumentException error) {
            handleRequestsFallback(dataObject);
        } catch (Throwable error) {
            handleFailureRequest(dataObject, error);
        }
    }

    @Override
    protected String getFallbackMessage() {
        return "Received data without an appropriate handler:";
    }

}
