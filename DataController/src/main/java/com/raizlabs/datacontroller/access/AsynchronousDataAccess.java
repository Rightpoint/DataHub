package com.raizlabs.datacontroller.access;

public interface AsynchronousDataAccess<Data> {

    public interface Callback<Data> {
        public void onResult(DataAccessResult<Data> result, AsynchronousDataAccess<Data> access);
    }

    public void get(Callback<Data> callback);
    public void importData(Data data);

    public void close();

    public int getSourceId();
}
