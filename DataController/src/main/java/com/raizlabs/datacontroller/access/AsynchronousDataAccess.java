package com.raizlabs.datacontroller.access;

public interface AsynchronousDataAccess<Data> extends DataAccess {

    public interface Callback<Data> {
        public void onResult(DataAccessResult<Data> result, AsynchronousDataAccess<Data> access);
    }

    public void get(Callback<Data> callback);
    public void importData(Data data);

}
