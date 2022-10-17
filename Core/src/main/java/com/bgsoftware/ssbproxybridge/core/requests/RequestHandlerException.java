package com.bgsoftware.ssbproxybridge.core.requests;

public class RequestHandlerException extends Exception {

    public RequestHandlerException(String message) {
        super(message);
    }

    public RequestHandlerException(Throwable error) {
        super(error);
    }

}
