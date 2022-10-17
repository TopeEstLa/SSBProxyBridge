package com.bgsoftware.ssbproxybridge.core.connector;

public class EmptyConnectionArguments implements IConnectionArguments {

    private static final EmptyConnectionArguments INSTANCE = new EmptyConnectionArguments();

    public static EmptyConnectionArguments getInstance() {
        return INSTANCE;
    }

}
