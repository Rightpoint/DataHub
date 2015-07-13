package com.raizlabs.datacontroller;

public class DataResult<Data> {

    private final Data data;
    private final int dataSourceId;
    private final boolean isUpdatePending;

    public DataResult(Data data, int dataSourceId, boolean isUpdatePending) {
        this.data = data;
        this.dataSourceId = dataSourceId;
        this.isUpdatePending = isUpdatePending;
    }

    public Data getData() {
        return data;
    }

    public int getDataSourceId() {
        return dataSourceId;
    }

    public boolean isUpdatePending() {
        return isUpdatePending;
    }
}
