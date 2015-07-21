package com.raizlabs.datacontroller.controller.ordered;

public interface FetchStrategy<T> {

    public void setDataController(OrderedDataController<T> controller);

    public void fetch();

    public void fetch(int limitId);

    public boolean isPending();

    public void close();
}