package com.raizlabs.datacontroller.access;


import com.raizlabs.datacontroller.DataAccessResult;

public interface SynchronousDataAccess<Data> {
    public DataAccessResult<Data> get();
    public void importData(Data data);

    public void close();

    public int getSourceId();
}
