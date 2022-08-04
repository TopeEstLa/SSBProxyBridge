package com.bgsoftware.ssbproxybridge.bukkit.database.requests;

public class RequestHandlerException extends Exception {

    public RequestHandlerException(String message) {
        super(message);
    }

    public RequestHandlerException(Throwable error) {
        super(error);
    }

}
