package com.bgsoftware.ssbproxybridge.bukkit.action;

import com.bgsoftware.ssbproxybridge.core.requests.RequestHandlerConsumer;
import com.bgsoftware.ssbproxybridge.core.requests.RequestHandlerException;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public class ActionsQueue<K, V> {

    private static final ActionsQueue<UUID, Player> PLAYER_ACTIONS_QUEUE = new ActionsQueue<>();

    private final Map<K, List<ActionRecord<V>>> queuedActions = new HashMap<>();

    public static ActionsQueue<UUID, Player> getPlayersQueue() {
        return PLAYER_ACTIONS_QUEUE;
    }

    private ActionsQueue() {

    }

    public void addAction(JsonObject dataObject, K key, RequestHandlerConsumer<V> action) {
        ActionRecord<V> actionRecord = new ActionRecord<>(dataObject, action);
        queuedActions.computeIfAbsent(key, c -> new LinkedList<>()).add(actionRecord);
    }

    public void poll(K key, V value, @Nullable BiConsumer<RequestHandlerException, JsonObject> onError) {
        List<ActionRecord<V>> actionRecords = queuedActions.remove(key);
        if (actionRecords != null)
            actionRecords.forEach(actionRecord -> {
                try {
                    actionRecord.action.accept(value);
                } catch (RequestHandlerException error) {
                    if (onError != null)
                        onError.accept(error, actionRecord.request);
                }
            });
    }

    private static class ActionRecord<E> {

        private final JsonObject request;
        private final RequestHandlerConsumer<E> action;

        ActionRecord(JsonObject request, RequestHandlerConsumer<E> action) {
            this.request = request;
            this.action = action;
        }

    }

}
