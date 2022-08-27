package com.bgsoftware.ssbproxybridge.bukkit.action;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.connector.JsonConnectorListener;
import com.bgsoftware.ssbproxybridge.core.requests.RequestHandlerException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Locale;

public class ActionsListener extends JsonConnectorListener {

    public ActionsListener(SSBProxyBridgeModule module) {
        super(module, module.getSettings().messagingServiceActionsChannelName);
    }

    @Override
    protected void processRequest(JsonObject dataObject) {
        JsonElement actionElement = dataObject.get("action");

        if (!(actionElement instanceof JsonPrimitive)) {
            handleFailureRequest(dataObject, new RequestHandlerException("Missing field \"action\""));
            return;
        }

        String action = actionElement.getAsString();

        try {
            ActionType actionType = ActionType.valueOf(action.toUpperCase(Locale.ENGLISH));
            actionType.getHandler().handle(dataObject);
        } catch (IllegalArgumentException error) {
            handleRequestsFallback(dataObject);
        } catch (Throwable error) {
            handleFailureRequest(dataObject, error);
        }
    }

    @Override
    protected String getFallbackMessage() {
        return "Received an action without an appropriate handler:";
    }

}
