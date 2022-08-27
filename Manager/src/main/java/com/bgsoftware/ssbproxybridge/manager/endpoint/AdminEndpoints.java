package com.bgsoftware.ssbproxybridge.manager.endpoint;

import com.bgsoftware.ssbproxybridge.manager.Main;
import com.bgsoftware.ssbproxybridge.manager.tracker.ServerInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@RestController
public class AdminEndpoints {

    @RequestMapping(value = "/info", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody ResponseEntity<String> getInfo(@RequestHeader Map<String, String> headers) {
        ResponseBuilder responseBuilder = Response.RESULT.newBuilder(headers);

        Map<String, ServerInfo> servers = Main.getInstance().getServersTracker().getServers();
        servers.forEach((serverName, islandsCount) -> responseBuilder.set(serverName, islandsCount.getIslandsCount()));

        return responseBuilder.build();
    }

    @RequestMapping(value = "/clear", method = RequestMethod.GET, produces = "application/json")
    public RedirectView clear() {
        Main.getInstance().getServersTracker().clear();
        return new RedirectView("/info");
    }

}
