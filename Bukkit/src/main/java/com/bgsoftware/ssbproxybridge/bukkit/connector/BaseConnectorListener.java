package com.bgsoftware.ssbproxybridge.bukkit.connector;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.core.bundle.Bundle;
import com.bgsoftware.ssbproxybridge.core.bundle.BundleSerializer;
import com.bgsoftware.ssbproxybridge.core.connector.IConnector;

import java.util.List;

public abstract class BaseConnectorListener implements IConnector.IListener {

    protected final SSBProxyBridgeModule module;
    protected final String listeningChannelName;

    protected BaseConnectorListener(SSBProxyBridgeModule module, String listeningChannelName) {
        this.module = module;
        this.listeningChannelName = listeningChannelName;
    }

    @Override
    public final void onReceive(Bundle bundle) {
        String channel = bundle.getChannelName();

        if (!listeningChannelName.equals(channel))
            return;

        String senderName = bundle.getSender();
        String currentServerName = module.getSettings().serverName;

        if (currentServerName.equals(senderName))
            return;

        List<String> recipients = bundle.getRecipients();

        if (recipients != null) {
            for (String recipient : recipients) {
                if (recipient.equals(currentServerName)) {
                    processRequest(bundle);
                    return;
                }
            }
        }

        processRequest(bundle);

    }

    protected abstract void processRequest(Bundle bundle);

    protected abstract String getFallbackMessage();

    protected void handleRequestsFallback(Bundle bundle) {
        this.module.getLogger().warning(getFallbackMessage());
        this.module.getLogger().warning(BundleSerializer.serializeBundle(bundle));
    }

    protected void handleFailureRequest(Bundle bundle, Throwable error) {
        this.module.getLogger().warning("Received an unexpected error while handling request:");
        this.module.getLogger().warning(BundleSerializer.serializeBundle(bundle));
        error.printStackTrace();
    }

}
