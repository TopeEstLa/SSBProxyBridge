package com.bgsoftware.ssbproxybridge.core.database;

import com.bgsoftware.ssbproxybridge.core.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collection;

public class OperationSerializer {

    private OperationSerializer() {

    }

    public static JsonObject serializeOperation(String operationType, Collection<Filter> filters, Column... columns) {
        JsonObject operation = new JsonObject();
        JsonArray filtersArray = new JsonArray();
        JsonArray columnsData = new JsonArray();

        for (Filter filter : filters) {
            JsonElement valueElement = JsonUtil.getJsonFromObject(filter.getValue());
            if (valueElement != null) {
                JsonObject filterObject = new JsonObject();
                filterObject.addProperty("column", filter.getColumn());
                filterObject.add("value", valueElement);
                filtersArray.add(filterObject);
            }
        }

        for (Column column : columns) {
            JsonElement valueElement = JsonUtil.getJsonFromObject(column.getValue());
            if (valueElement != null) {
                JsonObject columnDataObject = new JsonObject();
                columnDataObject.addProperty("name", column.getName());
                columnDataObject.add("value", valueElement);
                columnsData.add(columnDataObject);
            }
        }

        operation.addProperty("type", operationType);
        operation.add("filters", filtersArray);
        operation.add("columns", columnsData);

        return operation;
    }

}
