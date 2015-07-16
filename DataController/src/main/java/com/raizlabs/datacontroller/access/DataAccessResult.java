package com.raizlabs.datacontroller.access;

import com.raizlabs.datacontroller.DCError;

public class DataAccessResult<Data> {

    private boolean wasDataAvailable;
    private DCError error;
    private Data data;

    private DataAccessResult() {
        wasDataAvailable = false;
    }

    private DataAccessResult(DCError error) {
        this.wasDataAvailable = true;
        this.error = error;
    }

    private DataAccessResult(Data data) {
        this.wasDataAvailable = true;
        this.data = data;
    }

    public boolean wasDataAvailable() {
        return wasDataAvailable;
    }

    public DCError getError() {
        return error;
    }

    public Data getData() {
        return data;
    }

    public boolean hasValidData() {
        return (getError() == null) && wasDataAvailable;
    }

    public static <T> DataAccessResult<T> fromResult(T data) {
        return new DataAccessResult<>(data);
    }

    public static <T> DataAccessResult<T> fromUnavailable() {
        return new DataAccessResult<>();
    }

    public static <T> DataAccessResult<T> fromError(DCError error) {
        return new DataAccessResult<>(error);
    }
}
