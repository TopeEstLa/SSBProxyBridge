package com.bgsoftware.ssbproxybridge.bukkit.action;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.connector.BaseConnectorListener;
import com.bgsoftware.ssbproxybridge.bukkit.utils.Consts;
import com.bgsoftware.ssbproxybridge.core.bundle.Bundle;
import com.bgsoftware.ssbproxybridge.core.requests.RequestHandlerException;

import java.util.NoSuchElementException;

public class ActionsListener extends BaseConnectorListener {

    public ActionsListener(SSBProxyBridgeModule module) {
        super(module, module.getSettings().messagingServiceActionsChannelName);
    }

    @Override
    protected void processRequest(Bundle bundle) {
        ActionType actionType;

        try {
            actionType = bundle.getEnum(Consts.Action.ACTION, ActionType.class);
        } catch (NoSuchElementException error) {
            handleFailureRequest(bundle, new RequestHandlerException("Missing field \"" + Consts.Action.ACTION + "\""));
            return;
        }

        try {
            actionType.onReceive(bundle);
        } catch (Throwable error) {
            handleFailureRequest(bundle, error);
        }
    }

    @Override
    protected String getFallbackMessage() {
        return "Received an action without an appropriate handler:";
    }

}
