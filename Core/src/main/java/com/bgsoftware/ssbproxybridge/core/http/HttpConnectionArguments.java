package com.bgsoftware.ssbproxybridge.core.http;

import com.bgsoftware.ssbproxybridge.core.connector.IConnectionArguments;

public class HttpConnectionArguments implements IConnectionArguments {

    private final String url;
    private final String secret;

    public HttpConnectionArguments(String url, String secret) {
        this.url = url;
        this.secret = secret;
    }

    public String getUrl() {
        return url;
    }

    public String getSecret() {
        return secret;
    }

}
