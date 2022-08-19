package com.bgsoftware.ssbproxybridge.bukkit.action;

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ActionsQueue<K, V> {

    private static final ActionsQueue<UUID, Player> PLAYER_ACTIONS_QUEUE = new ActionsQueue<>();

    private final Map<K, List<ActionRecord<V>>> queuedActions = new HashMap<>();

    public static ActionsQueue<UUID, Player> getPlayersQueue() {
        return PLAYER_ACTIONS_QUEUE;
    }

    private ActionsQueue() {

    }

    public void addAction(JsonObject dataObject, K key, IAction<V> action) {
        ActionRecord<V> actionRecord = new ActionRecord<>(dataObject, action);
        queuedActions.computeIfAbsent(key, c -> new LinkedList<>()).add(actionRecord);
    }

    public void poll(K key, V value) {
        List<ActionRecord<V>> actionRecords = queuedActions.remove(key);
        if (actionRecords != null)
            actionRecords.forEach(actionRecord -> actionRecord.action.run(actionRecord.dataObject, value));
    }

    private static class ActionRecord<E> {

        private final JsonObject dataObject;
        private final IAction<E> action;

        ActionRecord(JsonObject dataObject, IAction<E> action) {
            this.dataObject = dataObject;
            this.action = action;
        }

    }

}
