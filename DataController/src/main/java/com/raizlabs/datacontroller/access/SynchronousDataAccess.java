package com.raizlabs.datacontroller.access;


public interface SynchronousDataAccess<Data> {
    public Data get();
    public void importData(Data data);

    public void close();

    public int getSourceId();
}
