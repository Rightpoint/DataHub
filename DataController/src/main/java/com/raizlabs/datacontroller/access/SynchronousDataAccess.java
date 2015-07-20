package com.raizlabs.datacontroller.access;


public interface SynchronousDataAccess<Data> extends DataAccess {
    public DataAccessResult<Data> get();
    public void importData(Data data);
}
