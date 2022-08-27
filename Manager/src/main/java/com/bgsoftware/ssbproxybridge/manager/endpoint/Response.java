package com.bgsoftware.ssbproxybridge.manager.endpoint;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.function.Consumer;

public enum Response {

    UNAUTHORIZED(responseBuilder -> responseBuilder
            .httpStatus(HttpStatus.UNAUTHORIZED)
            .set("error", "UNAUTHORIZED")),

    INVALID_ISLAND_UUID(responseBuilder -> responseBuilder
            .httpStatus(HttpStatus.BAD_REQUEST)
            .set("error", "INVALID_ISLAND_UUID")),

    INVALID_SERVER(responseBuilder -> responseBuilder
            .httpStatus(HttpStatus.BAD_REQUEST)
            .set("error", "INVALID_SERVER")),

    ISLAND_ALREADY_EXISTS(responseBuilder -> responseBuilder
            .httpStatus(HttpStatus.BAD_REQUEST)
            .set("error", "ISLAND_ALREADY_EXISTS")),

    ISLAND_DOES_NOT_EXIST(responseBuilder -> responseBuilder
            .httpStatus(HttpStatus.BAD_REQUEST)
            .set("error", "ISLAND_DOES_NOT_EXIST")),

    NO_VALID_SERVERS(responseBuilder -> responseBuilder
            .httpStatus(HttpStatus.BAD_REQUEST)
            .set("error", "NO_VALID_SERVERS")),

    BAD_REQUEST(responseBuilder -> responseBuilder
            .httpStatus(HttpStatus.BAD_REQUEST)),

    INVALID_REQUEST(responseBuilder -> responseBuilder
            .httpStatus(HttpStatus.NOT_FOUND)
            .set("error", "INVALID_REQUEST")),

    RESULT(responseBuilder -> responseBuilder
            .httpStatus(HttpStatus.OK));

    @Nullable
    private final Consumer<ResponseBuilder> responseBuilderCreator;

    Response() {
        this(null);
    }

    Response(@Nullable Consumer<ResponseBuilder> responseBuilderCreator) {
        this.responseBuilderCreator = responseBuilderCreator;
    }

    public ResponseBuilder newBuilder(Map<String, String> headers) {
        ResponseBuilder responseBuilder = new ResponseBuilder();

        String id = headers.get(Headers.REQUEST_ID);

        if (id != null)
            responseBuilder.set("id", Integer.parseInt(id));

        if (this.responseBuilderCreator != null)
            this.responseBuilderCreator.accept(responseBuilder);

        return responseBuilder;
    }

    public ResponseEntity<String> build(Map<String, String> headers) {
        return newBuilder(headers).build();
    }

}
