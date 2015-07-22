package com.raizlabs.datacontroller;

public interface DataResult<Data> {

    public Data getData();

    public int getAccessTypeId();

    public boolean isFetching();
}
