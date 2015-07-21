package com.raizlabs.datacontroller;

public interface DataResult<Data> {

    public Data getData();

    public int getDataSourceId();

    public boolean isUpdatePending();
}
