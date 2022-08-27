package com.bgsoftware.ssbproxybridge.manager.endpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class ResponseBuilder {

    private final ObjectNode responseNode = new ObjectNode(JsonNodeFactory.instance);
    private HttpStatus httpStatus = HttpStatus.OK;

    public ResponseBuilder() {
    }

    public ResponseBuilder httpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public ResponseBuilder set(String field, String value) {
        this.responseNode.set(field, TextNode.valueOf(value));
        return this;
    }

    public ResponseBuilder set(String field, int value) {
        this.responseNode.set(field, IntNode.valueOf(value));
        return this;
    }

    public ResponseBuilder set(String field, long value) {
        this.responseNode.set(field, LongNode.valueOf(value));
        return this;
    }

    public ResponseBuilder set(String field, List<JsonNode> value) {
        this.responseNode.set(field, new ArrayNode(JsonNodeFactory.instance, value));
        return this;
    }

    public ResponseEntity<String> build() {
        return ResponseEntity.status(this.httpStatus).body(this.responseNode.toPrettyString());
    }


}
