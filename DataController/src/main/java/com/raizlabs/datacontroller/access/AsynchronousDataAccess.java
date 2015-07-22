package com.raizlabs.datacontroller.access;

public interface AsynchronousDataAccess<Data> extends DataAccess {

    interface Callback<Data> {
        void onResult(DataAccessResult<Data> result, AsynchronousDataAccess<Data> access);
    }

    void get(Callback<Data> callback);
    void importData(Data data);

}
