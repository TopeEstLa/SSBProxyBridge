package com.bgsoftware.ssbproxybridge.core.messaging;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class ConnectorAbstract implements IConnector {

    private final Map<String, List<IListener>> listeners = new HashMap<>();

    @Override
    public boolean registerListener(String channel, IListener listener) {
        return listeners.computeIfAbsent(channel, ch -> new LinkedList<>()).add(listener);
    }

    @Override
    public boolean unregisterListener(String channel, IListener listener) {
        List<IListener> listeners = this.listeners.get(channel);
        return listeners != null && listeners.remove(listener) && listeners.isEmpty();
    }

    protected void notifyListeners(String channel, String data) {
        List<IListener> listeners = this.listeners.get(channel);
        if (listeners != null)
            listeners.forEach(listener -> listener.onReceive(data));
    }

}
