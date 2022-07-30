package com.bgsoftware.ssbproxybridge.core.database;

public class Filter {

    private final String column;
    private final Object value;

    public Filter(String column, Object value) {
        this.column = column;
        this.value = value;
    }

    public String getColumn() {
        return column;
    }

    public Object getValue() {
        return value;
    }

}
