package com.bgsoftware.ssbproxybridge.bukkit.manager;

import com.bgsoftware.ssbproxybridge.bukkit.SSBProxyBridgeModule;
import com.bgsoftware.ssbproxybridge.bukkit.island.RemoteIsland;
import com.bgsoftware.ssbproxybridge.bukkit.utils.BukkitExecutor;
import com.bgsoftware.ssbproxybridge.core.bundle.Bundle;
import com.bgsoftware.ssbproxybridge.core.bundle.BundleParseError;
import com.bgsoftware.ssbproxybridge.core.connector.ConnectionFailureException;
import com.bgsoftware.ssbproxybridge.core.connector.EmptyConnector;
import com.bgsoftware.ssbproxybridge.core.connector.IConnectionArguments;
import com.bgsoftware.ssbproxybridge.core.connector.IConnector;
import com.bgsoftware.ssbproxybridge.core.http.HttpConnectionArguments;
import com.bgsoftware.ssbproxybridge.core.http.HttpConnector;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.scheduler.BukkitTask;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ModuleManager {

    private static final Logger logger = Logger.getLogger("SSBProxyModule");

    private final SSBProxyBridgeModule module;

    private long requestIds = 0;

    @SuppressWarnings("rawtypes")
    private IConnector connector = EmptyConnector.getInstance();
    private boolean sendHello = false;
    private long keepAlive = 0;
    private BukkitTask keepAliveTask;
    private boolean failedCommunication = false;

    public ModuleManager(SSBProxyBridgeModule module) {
        this.module = module;
    }

    public void setupManager() {
        IConnectionArguments connectionArguments;

        switch (module.getSettings().managerType.toUpperCase(Locale.ENGLISH)) {
            case "REST":
                this.connector = HttpConnector.getConnector();
                connectionArguments = new HttpConnectionArguments(module.getSettings().managerRestUrl,
                        module.getSettings().managerRestSecret);
                sendHello = true;
                break;
            case "PROXY":
                // TODO
                return;
            default:
                throw new RuntimeException("Invalid connector: " + module.getSettings().managerType);
        }

        try {
            // noinspection unchecked
            this.connector.connect(connectionArguments);
            startCommunication();
        } catch (ConnectionFailureException error) {
            // Failed to connect with the manager.
            // Instead, we try to send keep-alive packets until we have a connection, then we send a HELLO packet.
            this.failedCommunication = true;
            this.keepAliveTask = BukkitExecutor.runTaskTimerAsynchronously(this::sendKeepAlive, 100L, 100L);
        }
    }

    public boolean isLocalIsland(UUID islandUUID) {
        Bundle response = sendRequest(RequestType.CHECK_ISLAND, islandUUID.toString()).join();
        return module.getSettings().serverName.equals(response.getString("result"));
    }

    public CompletableFuture<Bundle> getServerForNextIsland(UUID islandUUID) {
        return sendRequest(RequestType.CREATE_ISLAND, islandUUID.toString());
    }

    public void deleteIsland(UUID islandUUID) {
        sendRequest(RequestType.DELETE_ISLAND, islandUUID.toString());
    }

    public void updateIsland(UUID islandUUID) {
        sendRequest(RequestType.UPDATE_ISLAND, islandUUID.toString());
    }

    public void sendHello() {
        CompletableFuture<Bundle> responseFuture = sendRequest(RequestType.HELLO, "");

        try {
            // Should block until we get a response.
            Bundle response = responseFuture.get(10, TimeUnit.SECONDS);

            if (response.contains("error"))
                throw new RuntimeException("Failed to register to the manager: " + response.getString("error"));

            this.keepAlive = response.getLong("keep-alive") / 50; // Converting milliseconds to ticks
        } catch (Exception error) {
            error.printStackTrace();
            throw new RuntimeException("Failed to connect to the manager, aborting.", error);
        }
    }

    private void startCommunication() {
        if (sendHello)
            sendHello();

        if (this.keepAliveTask != null) {
            this.keepAliveTask.cancel();
        }

        if (this.keepAlive > 0) {
            this.keepAliveTask = BukkitExecutor.runTaskTimerAsynchronously(this::sendKeepAlive, this.keepAlive, this.keepAlive);
        } else {
            this.keepAliveTask = null;
        }

        // We want to update the module manager with our current data.
        for (Island island : SuperiorSkyblockAPI.getGrid().getIslands()) {
            if (!(island instanceof RemoteIsland))
                updateIsland(island.getUniqueId());
        }
    }

    private void sendKeepAlive() {
        sendRequest(RequestType.KEEP_ALIVE, "").whenCompleteAsync((result, error) -> {
            if (error == null) {
                if (failedCommunication) {
                    startCommunication();

                    logger.info("Reconnected to the module-manager.");

                    failedCommunication = false;
                }
            } else {
                // An error occurred while sending a keep alive.
                logger.warning("Failed to connect to the module-manager. Retrying in " + (this.keepAlive / 20L) + " seconds.");
                failedCommunication = true;
            }
        });
    }

    private CompletableFuture<Bundle> sendRequest(RequestType requestType, String params) {
        Bundle args = new Bundle();

        long requestId = this.requestIds++;

        args.setChannelName(requestType.name());
        args.setString("method", requestType.getMethod());
        args.setLong("id", requestId);
        args.setString("route", requestType.getRoute() + params);
        args.setString("server", module.getSettings().serverName);

        CompletableFuture<Bundle> result = new CompletableFuture<>();

        this.connector.sendBundle(args, (Consumer<Throwable>) result::completeExceptionally);

        this.connector.listenOnce(requestType.name(), responseData -> {
            try {
                Bundle response = new Bundle(responseData);
                if (requestId == response.getLong("id"))
                    result.complete(response);
            } catch (BundleParseError error) {
                System.out.println(responseData);
                error.printStackTrace();
            }
        });

        return result;
    }

}
