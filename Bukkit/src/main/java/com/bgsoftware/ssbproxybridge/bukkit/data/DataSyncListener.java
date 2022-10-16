package com.bgsoftware.ssbproxybridge.bukkit.data;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.connector.BaseConnectorListener;
import com.bgsoftware.ssbproxybridge.core.bundle.Bundle;
import com.bgsoftware.ssbproxybridge.core.requests.RequestHandlerException;

import java.util.NoSuchElementException;

public class DataSyncListener extends BaseConnectorListener {

    public DataSyncListener(SSBProxyBridgeModule module) {
        super(module, module.getSettings().messagingServiceDataChannelName);
    }

    @Override
    protected void processRequest(Bundle bundle) {
        DataSyncType dataSyncType;

        try {
            dataSyncType = bundle.getEnum("type", DataSyncType.class);
        } catch (NoSuchElementException error) {
            handleFailureRequest(bundle, new RequestHandlerException("Missing field \"type\""));
            return;
        }

        try {
            if (!dataSyncType.canReceive(bundle))
                throw new IllegalStateException("Cannot receive the packet " + dataSyncType);

            dataSyncType.onReceive(bundle);
        } catch (Throwable error) {
            handleFailureRequest(bundle, error);
        }
    }

    @Override
    protected String getFallbackMessage() {
        return "Received data without an appropriate handler:";
    }

}
