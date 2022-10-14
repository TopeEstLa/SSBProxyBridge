package com.bgsoftware.ssbproxybridge.manager.endpoint;

import com.bgsoftware.ssbproxybridge.manager.ManagerServer;
import com.bgsoftware.ssbproxybridge.manager.tracker.IslandInfo;
import com.bgsoftware.ssbproxybridge.manager.tracker.ServerInfo;
import com.bgsoftware.ssbproxybridge.manager.tracker.ServersTracker;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class APIEndpoints {

    private static final ManagerServer managerServer = ManagerServer.getInstance();

    @RequestMapping(value = "/", method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
    public @ResponseBody ResponseEntity<String> hello(@RequestHeader Map<String, String> headers) {
        if (!checkSecret(headers.get(Headers.AUTHORIZATION)))
            return Response.UNAUTHORIZED.build(headers);

        String serverName = headers.get(Headers.SERVER);

        if (serverName == null) {
            return Response.BAD_REQUEST.newBuilder(headers)
                    .set("error", "MISSING_HEADER")
                    .set("header", Headers.SERVER)
                    .build();
        }

        managerServer.getServersTracker().registerNewServer(serverName);

        return Response.RESULT.newBuilder(headers)
                .set("result", "hello")
                .set("keep-alive", managerServer.getConfig().keepAlive)
                .build();
    }

    @RequestMapping(value = "/sleep", method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
    public @ResponseBody ResponseEntity<String> sleep(@RequestHeader Map<String, String> headers) {
        if (!checkSecret(headers.get(Headers.AUTHORIZATION)))
            return Response.UNAUTHORIZED.build(headers);

        String serverName = headers.get(Headers.SERVER);

        if (serverName == null) {
            return Response.BAD_REQUEST.newBuilder(headers)
                    .set("error", "MISSING_HEADER")
                    .set("header", Headers.SERVER)
                    .build();
        }

        ServerInfo serverInfo = managerServer.getServersTracker().getServerInfo(serverName);

        if (serverInfo == null)
            return Response.INVALID_SERVER.build(headers);

        serverInfo.sleep();

        return Response.RESULT.newBuilder(headers)
                .set("result", "OK")
                .build();
    }

    @RequestMapping(value = "/keep-alive", method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
    public @ResponseBody ResponseEntity<String> keepAlive(@RequestHeader Map<String, String> headers) {
        if (!checkSecret(headers.get(Headers.AUTHORIZATION)))
            return Response.UNAUTHORIZED.build(headers);

        String serverName = headers.get(Headers.SERVER);

        if (serverName == null) {
            return Response.BAD_REQUEST.newBuilder(headers)
                    .set("error", "MISSING_HEADER")
                    .set("header", Headers.SERVER)
                    .build();
        }

        ServerInfo serverInfo = managerServer.getServersTracker().getServerInfo(serverName);

        if (serverInfo == null)
            return Response.INVALID_SERVER.build(headers);

        serverInfo.updateLastPingTime();

        return Response.RESULT.newBuilder(headers)
                .set("result", "OK")
                .build();
    }

    @RequestMapping(value = "/island/{islandUUID}", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody ResponseEntity<String> createIsland(@RequestHeader Map<String, String> headers,
                                                             @PathVariable(value = "islandUUID") String islandUUIDParam) {
        if (!checkSecret(headers.get(Headers.AUTHORIZATION)))
            return Response.UNAUTHORIZED.build(headers);

        String serverName = headers.get(Headers.SERVER);

        if (serverName == null) {
            return Response.BAD_REQUEST.newBuilder(headers)
                    .set("error", "MISSING_HEADER")
                    .set("header", Headers.SERVER)
                    .build();
        }

        ServersTracker serversTracker = managerServer.getServersTracker();

        ServerInfo serverInfo = serversTracker.getServerInfo(serverName);

        if (serverInfo == null)
            return Response.INVALID_SERVER.build(headers);

        serverInfo.updateLastPingTime();

        UUID islandUUID;

        try {
            islandUUID = UUID.fromString(islandUUIDParam);
        } catch (IllegalArgumentException error) {
            return Response.INVALID_ISLAND_UUID.build(headers);
        }

        if (serversTracker.getIslandInfo(islandUUID) != null)
            return Response.ISLAND_ALREADY_EXISTS.build(headers);

        String newServer = serversTracker.getServerForNewIsland();

        if (newServer == null)
            return Response.NO_VALID_SERVERS.build(headers);

        try {
            serversTracker.trackIsland(islandUUID, newServer);
            return Response.RESULT.newBuilder(headers)
                    .set("result", newServer)
                    .build();
        } catch (IllegalStateException error) {
            return Response.NO_VALID_SERVERS.build(headers);
        }
    }

    @RequestMapping(value = "/island/{islandUUID}", method = RequestMethod.DELETE, produces = "application/json")
    public @ResponseBody ResponseEntity<String> deleteIsland(@RequestHeader Map<String, String> headers,
                                                             @PathVariable(value = "islandUUID") String islandUUIDParam) {
        if (!checkSecret(headers.get(Headers.AUTHORIZATION)))
            return Response.UNAUTHORIZED.build(headers);

        String serverName = headers.get(Headers.SERVER);

        if (serverName == null) {
            return Response.BAD_REQUEST.newBuilder(headers)
                    .set("error", "MISSING_HEADER")
                    .set("header", Headers.SERVER)
                    .build();
        }

        UUID islandUUID;

        try {
            islandUUID = UUID.fromString(islandUUIDParam);
        } catch (IllegalArgumentException error) {
            return Response.INVALID_ISLAND_UUID.build(headers);
        }

        ServersTracker serversTracker = managerServer.getServersTracker();

        ServerInfo serverInfo = serversTracker.getServerInfo(serverName);

        if (serverInfo == null)
            return Response.INVALID_SERVER.build(headers);

        serverInfo.updateLastPingTime();

        serversTracker.untrackIsland(islandUUID);

        return Response.RESULT.newBuilder(headers)
                .set("result", "OK")
                .build();
    }

    @RequestMapping(value = "/island/{islandUUID}", method = RequestMethod.PUT, produces = "application/json")
    public @ResponseBody ResponseEntity<String> updateIsland(@RequestHeader Map<String, String> headers,
                                                             @PathVariable(value = "islandUUID") String islandUUIDParam) {
        if (!checkSecret(headers.get(Headers.AUTHORIZATION)))
            return Response.UNAUTHORIZED.build(headers);

        String serverName = headers.get(Headers.SERVER);

        if (serverName == null) {
            return Response.BAD_REQUEST.newBuilder(headers)
                    .set("error", "MISSING_HEADER")
                    .set("header", Headers.SERVER)
                    .build();
        }

        UUID islandUUID;

        try {
            islandUUID = UUID.fromString(islandUUIDParam);
        } catch (IllegalArgumentException error) {
            return Response.INVALID_ISLAND_UUID.build(headers);
        }

        ServersTracker serversTracker = managerServer.getServersTracker();

        ServerInfo serverInfo = serversTracker.getServerInfo(serverName);

        if (serverInfo == null)
            return Response.INVALID_SERVER.build(headers);

        IslandInfo islandInfo = serversTracker.getIslandInfo(islandUUID);

        if (islandInfo == null) {
            // We create a new island info for the island.
            serversTracker.trackIsland(islandUUID, serverName);
        } else {
            islandInfo.updateLastUpdateTime();
        }

        serverInfo.updateLastPingTime();

        return Response.RESULT.newBuilder(headers)
                .set("result", "OK")
                .build();
    }

    @RequestMapping(value = "/island/{islandUUID}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity<String> getIslandInfo(@RequestHeader Map<String, String> headers,
                                                              @PathVariable(value = "islandUUID") String islandUUIDParam) {
        if (!checkSecret(headers.get(Headers.AUTHORIZATION)))
            return Response.UNAUTHORIZED.build(headers);

        String serverName = headers.get(Headers.SERVER);

        if (serverName == null) {
            return Response.BAD_REQUEST.newBuilder(headers)
                    .set("error", "MISSING_HEADER")
                    .set("header", Headers.SERVER)
                    .build();
        }

        ServersTracker serversTracker = managerServer.getServersTracker();

        ServerInfo serverInfo = serversTracker.getServerInfo(serverName);

        if (serverInfo == null)
            return Response.INVALID_SERVER.build(headers);

        UUID islandUUID;

        try {
            islandUUID = UUID.fromString(islandUUIDParam);
        } catch (IllegalArgumentException error) {
            return Response.INVALID_ISLAND_UUID.build(headers);
        }

        serverInfo.updateLastPingTime();

        IslandInfo islandInfo = serversTracker.getIslandInfo(islandUUID);

        if (islandInfo == null)
            return Response.ISLAND_DOES_NOT_EXIST.build(headers);

        return Response.RESULT.newBuilder(headers)
                .set("server", islandInfo.getServerName())
                .set("last_update_time", islandInfo.getLastUpdateTime())
                .build();
    }

    private static boolean checkSecret(@Nullable String secret) {
        return managerServer.getSecret().equals(secret);
    }

}
