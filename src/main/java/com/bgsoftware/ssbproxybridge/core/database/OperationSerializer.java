package com.bgsoftware.ssbproxybridge.core.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import reactor.util.annotation.Nullable;

import java.util.Collection;

public class OperationSerializer {

    private OperationSerializer() {

    }

    public static JsonObject serializeOperation(String table, Collection<Filter> filters, Column... columns) {
        JsonObject operation = new JsonObject();
        JsonArray filtersArray = new JsonArray();
        JsonArray columnsData = new JsonArray();

        for (Filter filter : filters) {
            JsonElement valueElement = getJsonFromObject(filter.getValue());
            if (valueElement != null) {
                JsonObject filterObject = new JsonObject();
                filterObject.addProperty("column", filter.getColumn());
                filterObject.add("value", valueElement);
                filtersArray.add(filterObject);
            }
        }

        for (Column column : columns) {
            JsonElement valueElement = getJsonFromObject(column.getValue());
            if (valueElement != null) {
                JsonObject columnDataObject = new JsonObject();
                columnDataObject.addProperty("name", column.getName());
                columnDataObject.add("value", valueElement);
                columnsData.add(columnDataObject);
            }
        }

        operation.addProperty("table", table);
        operation.add("filters", filtersArray);
        operation.add("columns", columnsData);

        return operation;
    }

    @Nullable
    private static JsonElement getJsonFromObject(Object object) {
        if (object instanceof String) {
            return new JsonPrimitive((String) object);
        } else if (object instanceof Number) {
            return new JsonPrimitive((Number) object);
        } else if (object instanceof Boolean) {
            return new JsonPrimitive((Boolean) object);
        } else if (object instanceof Character) {
            return new JsonPrimitive((Character) object);
        } else {
            return null;
        }
    }

}
