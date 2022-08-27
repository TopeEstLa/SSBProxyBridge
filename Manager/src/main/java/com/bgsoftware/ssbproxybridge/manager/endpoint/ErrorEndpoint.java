package com.bgsoftware.ssbproxybridge.manager.endpoint;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class ErrorEndpoint implements ErrorController {

    @RequestMapping(value = "/error", method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json")
    public @ResponseBody ResponseEntity<String> fallback(@RequestHeader Map<String, String> headers) {
        return Response.INVALID_REQUEST.build(headers);
    }

    @Override
    public String getErrorPath() {
        return "error";
    }

}
