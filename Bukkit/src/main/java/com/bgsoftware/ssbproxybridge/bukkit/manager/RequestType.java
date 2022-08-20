package com.bgsoftware.ssbproxybridge.bukkit.manager;

public enum RequestType {

    HELLO("GET", ""),
    CHECK_ISLAND("GET", "island/"),
    CREATE_ISLAND("POST", "island/");

    private final String method;
    private final String route;

    RequestType(String method, String route) {
        this.method = method;
        this.route = route;
    }

    public String getMethod() {
        return method;
    }

    public String getRoute() {
        return route;
    }

}
