package com.bgsoftware.ssbproxybridge.manager.endpoint;

import com.bgsoftware.ssbproxybridge.manager.Main;
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

    private static final String SERVER_SECRET = "SECRET";

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

        if (!Main.getInstance().getConfig().spawnServerName.equals(serverName))
            Main.getInstance().getServersTracker().registerNewServer(serverName);

        return Response.RESULT.newBuilder(headers)
                .set("result", "hello")
                .set("keep-alive", Main.getInstance().getConfig().keepAlive)
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

        ServersTracker serversTracker = Main.getInstance().getServersTracker();

        ServerInfo serverInfo = serversTracker.getServerInfo(serverName);

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

        ServersTracker serversTracker = Main.getInstance().getServersTracker();

        ServerInfo serverInfo = serversTracker.getServerInfo(serverName);

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

        ServersTracker serversTracker = Main.getInstance().getServersTracker();

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

        if (serversTracker.getServerOfIsland(islandUUID) != null)
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

    @RequestMapping(value = "/island/{islandUUID}", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity<String> getIslandServer(@RequestHeader Map<String, String> headers,
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

        ServersTracker serversTracker = Main.getInstance().getServersTracker();

        ServerInfo serverInfo = serversTracker.getServerInfo(serverName);

        if (serverInfo == null)
            return Response.INVALID_SERVER.build(headers);

        UUID islandUUID;

        try {
            islandUUID = UUID.fromString(islandUUIDParam);
        } catch (IllegalArgumentException error) {
            return Response.INVALID_ISLAND_UUID.build(headers);
        }

        String server = serversTracker.getServerOfIsland(islandUUID);

        if (server == null)
            return Response.ISLAND_DOES_NOT_EXIST.build(headers);

        return Response.RESULT.newBuilder(headers)
                .set("result", server)
                .build();
    }

    private static boolean checkSecret(@Nullable String secret) {
        return SERVER_SECRET.equals(secret);
    }

}
