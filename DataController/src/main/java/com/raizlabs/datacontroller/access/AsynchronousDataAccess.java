package com.raizlabs.datacontroller.access;

import com.raizlabs.datacontroller.*;

public interface AsynchronousDataAccess<Data> {

    public interface Callback<Data> {
        public void onDataObtained(Data data, AsynchronousDataAccess<Data> access);
        public void onError(DCError error, AsynchronousDataAccess<Data> access);
    }

    public void get(Callback<Data> callback);
    public void importData(Data data);

    public void close();

    public int getSourceId();
}
