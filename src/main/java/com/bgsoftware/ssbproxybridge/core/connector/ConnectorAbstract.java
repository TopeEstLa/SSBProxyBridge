package com.bgsoftware.ssbproxybridge.core.connector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class ConnectorAbstract<Args extends IConnectionArguments> implements IConnector<Args> {

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

    @Override
    public boolean unregisterListeners(String channel) {
        return this.listeners.get(channel) != null;
    }

    protected void notifyListeners(String channel, String data) {
        List<IListener> listeners = this.listeners.get(channel);
        if (listeners != null) {
            Iterator<IListener> listenerIterator = listeners.iterator();
            while (listenerIterator.hasNext()) {
                IListener listener = listenerIterator.next();
                try {
                    listener.onReceive(data);
                } catch (Throwable error) {
                    error.printStackTrace();
                } finally {
                    if (listener instanceof IOneTimeListener)
                        listenerIterator.remove();
                }
            }
        }
    }

}
