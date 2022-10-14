package com.bgsoftware.ssbproxybridge.core.database;

import com.bgsoftware.ssbproxybridge.core.bundle.Bundle;

import java.util.Collection;

public class OperationSerializer {

    private OperationSerializer() {

    }

    public static Bundle serializeOperation(String operationType, Collection<Filter> filters, Column... columns) {
        Bundle operation = new Bundle();
        Bundle filtersBundle = new Bundle();
        Bundle columnsBundle = new Bundle();

        for (Filter filter : filters)
            filtersBundle.setObject(filter.getColumn(), filter.getValue());

        for (Column column : columns)
            columnsBundle.setObject(column.getName(), column.getValue());

        operation.setString("type", operationType);
        operation.setExtra("filters", filtersBundle);
        operation.setExtra("columns", columnsBundle);

        return operation;
    }

}
