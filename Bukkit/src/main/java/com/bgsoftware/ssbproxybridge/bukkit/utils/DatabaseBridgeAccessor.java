package com.bgsoftware.ssbproxybridge.bukkit.utils;

import com.bgsoftware.ssbproxybridge.core.requests.RequestHandlerAction;
import com.bgsoftware.ssbproxybridge.core.requests.RequestHandlerException;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.data.IDatabaseBridgeHolder;

import java.util.function.Consumer;

public class DatabaseBridgeAccessor {

    private DatabaseBridgeAccessor() {

    }

    public static <T extends IDatabaseBridgeHolder> void runWithoutDataSave(T databaseBridgeHolder, Consumer<T> runnable) {
        try {
            databaseBridgeHolder.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.IDLE);
            runnable.accept(databaseBridgeHolder);
        } finally {
            databaseBridgeHolder.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.SAVE_DATA);
        }
    }

    public static void runWithoutDataSave(IDatabaseBridgeHolder databaseBridgeHolder, Runnable runnable) {
        try {
            databaseBridgeHolder.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.IDLE);
            runnable.run();
        } finally {
            databaseBridgeHolder.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.SAVE_DATA);
        }
    }

    public static void runWithoutDataSave(IDatabaseBridgeHolder databaseBridgeHolder, RequestHandlerAction runnable) throws RequestHandlerException {
        try {
            databaseBridgeHolder.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.IDLE);
            runnable.run();
        } finally {
            databaseBridgeHolder.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.SAVE_DATA);
        }
    }

}
