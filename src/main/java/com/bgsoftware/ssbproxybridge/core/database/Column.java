package com.bgsoftware.ssbproxybridge.core.database;

public class Column {

    private final String name;
    private final Object value;

    public Column(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

}
