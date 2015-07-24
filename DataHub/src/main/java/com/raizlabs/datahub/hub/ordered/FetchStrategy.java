package com.raizlabs.datahub.hub.ordered;

public interface FetchStrategy<T> {

    public void setDataHub(OrderedDataHub<T> hub);

    public void fetch();

    public void fetch(int limitId);

    public boolean isPending();

    public void close();
}
