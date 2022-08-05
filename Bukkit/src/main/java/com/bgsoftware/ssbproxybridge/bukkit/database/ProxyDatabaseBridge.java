package com.bgsoftware.ssbproxybridge.bukkit.database;

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
import java.util.function.Consumer;

public class ProxyDatabaseBridge implements DatabaseBridge {

    private static final SSBProxyBridgeModule module = SSBProxyBridgeModule.getModule();

    public static String CHANNEL_NAME = "ssb-data";

    private static final Gson gson = new Gson();

    private DatabaseBridgeMode databaseBridgeMode = DatabaseBridgeMode.IDLE;
    private boolean isActivated;
    @Nullable
    private JsonArray batchOperations = null;

    public ProxyDatabaseBridge(boolean isActivated) {
        this.isActivated = isActivated;
    }

    @Override
    public void loadAllObjects(String table, Consumer<Map<String, Object>> consumer) {
        // TODO: Load data from manager.
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
    }

    @Override
    public void updateObject(String table, @Nullable DatabaseFilter databaseFilter, Pair<String, Object>... pairs) {
        if (isActivated && databaseBridgeMode == DatabaseBridgeMode.SAVE_DATA) {
            JsonObject operation = OperationSerializer.serializeOperation(table, createFilters(databaseFilter), createColumns(pairs));
            commitData(finishData(operation, "update"));
        }
    }

    @Override
    public void insertObject(String table, Pair<String, Object>... pairs) {
        if (isActivated && databaseBridgeMode == DatabaseBridgeMode.SAVE_DATA) {
            JsonObject operation = OperationSerializer.serializeOperation(table, Collections.emptyList(), createColumns(pairs));
            commitData(finishData(operation, "insert"));
        }
    }

    @Override
    public void deleteObject(String table, @Nullable DatabaseFilter databaseFilter) {
        if (isActivated && databaseBridgeMode == DatabaseBridgeMode.SAVE_DATA) {
            JsonObject operation = OperationSerializer.serializeOperation(table, createFilters(databaseFilter));
            commitData(finishData(operation, "delete"));
        }
    }

    @Override
    public void loadObject(String table, @Nullable DatabaseFilter databaseFilter, Consumer<Map<String, Object>> consumer) {
        // TODO: Load data from manager.
    }

    @Override
    public void setDatabaseBridgeMode(DatabaseBridgeMode databaseBridgeMode) {
        this.databaseBridgeMode = databaseBridgeMode;
    }

    public DatabaseBridgeMode getDatabaseBridgeMode() {
        return this.databaseBridgeMode;
    }

    public void activate() {
        isActivated = true;
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

    private static Column[] createColumns(Pair<String, Object>... pairs) {
        Column[] columns = new Column[pairs.length];
        for (int i = 0; i < pairs.length; ++i) {
            Pair<String, Object> pair = pairs[i];
            columns[i] = new Column(pair.getKey(), pair.getValue());
        }
        return columns;
    }

    private JsonObject finishData(JsonObject dataObject, String type) {
        dataObject.addProperty("type", type);
        dataObject.addProperty("sender", module.getSettings().serverName);
        return dataObject;
    }

    private void commitData(JsonElement data) {
        if (this.batchOperations != null) {
            this.batchOperations.add(data);
        } else {
            module.getMessaging().sendData(CHANNEL_NAME, gson.toJson(data));
        }
    }

}
