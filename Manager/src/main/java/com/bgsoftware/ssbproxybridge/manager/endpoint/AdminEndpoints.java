package com.bgsoftware.ssbproxybridge.manager.endpoint;

import com.bgsoftware.ssbproxybridge.manager.ManagerServer;
import com.bgsoftware.ssbproxybridge.manager.tracker.IslandInfo;
import com.bgsoftware.ssbproxybridge.manager.tracker.ServerInfo;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@RestController
public class AdminEndpoints {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private static final ManagerServer managerServer = ManagerServer.getInstance();

    @RequestMapping(value = "/info", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity<String> getInfo(@RequestHeader Map<String, String> headers) {
        ResponseBuilder responseBuilder = Response.RESULT.newBuilder(headers);

        Map<String, ServerInfo> servers = managerServer.getServersTracker().getServers();
        servers.forEach((serverName, serverInfo) -> {
            ObjectNode serverNode = new ObjectNode(JsonNodeFactory.instance);
            serverNode.set("islands_count", IntNode.valueOf(serverInfo.getIslandsCount()));
            serverNode.set("last_ping", TextNode.valueOf(DATE_FORMAT.format(new Date(serverInfo.getLastPingTime()))));
            ArrayNode islands = new ArrayNode(JsonNodeFactory.instance);
            for (IslandInfo islandInfo : serverInfo.getServerIslands()) {
                ObjectNode islandNode = new ObjectNode(JsonNodeFactory.instance);
                islandNode.set("server", TextNode.valueOf(islandInfo.getServerName()));
                islandNode.set("uuid", TextNode.valueOf(islandInfo.getUniqueId().toString()));
                islandNode.set("last_update_time", TextNode.valueOf(DATE_FORMAT.format(new Date(islandInfo.getLastUpdateTime()))));
                islands.add(islandNode);
            }
            serverNode.set("islands", islands);
            responseBuilder.set(serverName, serverNode);
        });

        return responseBuilder.build();
    }

    @RequestMapping(value = "/clear", method = RequestMethod.GET, produces = "application/json")
    public RedirectView clear() {
        managerServer.getServersTracker().clear();
        return new RedirectView("/info");
    }

}
