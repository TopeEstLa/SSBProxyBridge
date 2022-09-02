package com.bgsoftware.ssbproxybridge.bukkit.bridge;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.core.database.Column;
import com.bgsoftware.ssbproxybridge.core.database.Filter;
import com.bgsoftware.ssbproxybridge.core.database.OperationSerializer;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.data.DatabaseFilter;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ProxyDatabaseBridge implements DatabaseBridge {

    private static final SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

    private static final Gson gson = new Gson();

    private final DatabaseBridge original;

    private DatabaseBridgeMode databaseBridgeMode = DatabaseBridgeMode.IDLE;
    private boolean isActivated;
    @Nullable
    private JsonArray batchOperations = null;

    public ProxyDatabaseBridge(DatabaseBridge original, boolean isActivated) {
        this.original = original;
        this.isActivated = isActivated;
    }

    @Override
    public void loadAllObjects(String table, Consumer<Map<String, Object>> consumer) {
        original.loadAllObjects(table, data -> tryLoadData(table, data, consumer));
    }

    @Override
    public void batchOperations(boolean batchOperations) {
        if (batchOperations) {
            this.batchOperations = new JsonArray();
        } else if (this.batchOperations != null) {
            if (this.batchOperations.size() > 0)
                commitData(this.batchOperations);
            this.batchOperations = null;
        }
        original.batchOperations(batchOperations);
    }

    @Override
    public void updateObject(String table, @Nullable DatabaseFilter databaseFilter, Pair<String, Object>[] pairs) {
        if (isActivated && databaseBridgeMode == DatabaseBridgeMode.SAVE_DATA) {
            String type = buildDataSyncType("update", table, pairs);
            JsonObject operation = OperationSerializer.serializeOperation(type, createFilters(databaseFilter), createColumns(pairs));
            commitData(finishData(operation));
            original.updateObject(table, databaseFilter, pairs);
        }
    }

    @Override
    public void insertObject(String table, Pair<String, Object>[] pairs) {
        if (isActivated && databaseBridgeMode == DatabaseBridgeMode.SAVE_DATA) {
            String type = buildDataSyncType("insert", table, null);
            JsonObject operation = OperationSerializer.serializeOperation(type, Collections.emptyList(), createColumns(pairs));
            commitData(finishData(operation));
            original.insertObject(table, pairs);
        }
    }

    @Override
    public void deleteObject(String table, @Nullable DatabaseFilter databaseFilter) {
        if (isActivated && databaseBridgeMode == DatabaseBridgeMode.SAVE_DATA) {
            String type = buildDataSyncType("delete", table, null);
            JsonObject operation = OperationSerializer.serializeOperation(type, createFilters(databaseFilter));
            commitData(finishData(operation));
            original.deleteObject(table, databaseFilter);
        }
    }

    @Override
    public void loadObject(String table, @Nullable DatabaseFilter databaseFilter, Consumer<Map<String, Object>> consumer) {
        original.loadObject(table, databaseFilter, data -> tryLoadData(table, data, consumer));
    }

    @Override
    public void setDatabaseBridgeMode(DatabaseBridgeMode databaseBridgeMode) {
        this.databaseBridgeMode = databaseBridgeMode;
        original.setDatabaseBridgeMode(databaseBridgeMode);
    }

    public DatabaseBridgeMode getDatabaseBridgeMode() {
        return this.databaseBridgeMode;
    }

    public void activate() {
        isActivated = true;
    }

    private JsonObject finishData(JsonObject dataObject) {
        dataObject.addProperty("sender", module.getSettings().serverName);
        dataObject.addProperty("channel", module.getSettings().messagingServiceDataChannelName);
        return dataObject;
    }

    private void commitData(JsonElement data) {
        if (this.batchOperations != null) {
            this.batchOperations.add(data);
        } else {
            module.getMessaging().sendData(module.getSettings().messagingServiceDataChannelName, gson.toJson(data));
        }
    }

    private static Collection<Filter> createFilters(@Nullable DatabaseFilter databaseFilter) {
        return databaseFilter == null ? Collections.emptyList() :
                Lists.transform(new LinkedList<>(databaseFilter.getFilters()), new Function<Pair<String, Object>, Filter>() {
                    @Nullable
                    @Override
                    public Filter apply(@Nullable Pair<String, Object> pair) {
                        return pair == null ? null : new Filter(pair.getKey(), pair.getValue());
                    }
                });
    }

    private static Column[] createColumns(Pair<String, Object>[] pairs) {
        Column[] columns = new Column[pairs.length];
        for (int i = 0; i < pairs.length; ++i) {
            Pair<String, Object> pair = pairs[i];
            columns[i] = new Column(pair.getKey(), pair.getValue());
        }
        return columns;
    }

    private static String buildDataSyncType(String operationType, String table, @Nullable Pair<String, Object>[] actionIdentifiers) {
        StringBuilder dataSyncType = new StringBuilder()
                .append(operationType).append("_").append(table);

        if (actionIdentifiers != null) {
            for (Pair<String, Object> identifier : actionIdentifiers) {
                dataSyncType.append("_").append(identifier.getKey());
            }
        }

        return dataSyncType.toString();
    }

    private static void tryLoadData(String table, Map<String, Object> loadedData, Consumer<Map<String, Object>> original) {
        if (table.contains("island_")) {
            // We want to find the UUID of the island.

            UUID islandUUID = null;

            if (loadedData.containsKey("island")) {
                islandUUID = UUID.fromString((String) loadedData.get("island"));
            } else if (loadedData.containsKey("uuid")) {
                islandUUID = UUID.fromString((String) loadedData.get("uuid"));
            }

            if (islandUUID != null && !module.getManager().isLocalIsland(islandUUID))
                return; // We don't want to load non-local islands. The RemoteIslands will be loaded later.
        }

        original.accept(loadedData);
    }

}
