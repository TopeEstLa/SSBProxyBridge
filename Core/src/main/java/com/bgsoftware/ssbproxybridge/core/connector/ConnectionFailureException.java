package com.bgsoftware.ssbproxybridge.core.connector;

public class ConnectionFailureException extends Exception {

    public ConnectionFailureException(String message) {
        super(message);
    }

    public ConnectionFailureException(Throwable error) {
        super(error);
    }

}
