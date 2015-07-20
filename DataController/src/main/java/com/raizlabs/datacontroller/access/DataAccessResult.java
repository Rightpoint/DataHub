package com.raizlabs.datacontroller.access;

import com.raizlabs.datacontroller.DCError;

public class DataAccessResult<Data> {

    private DCError error;
    private Data data;

    private DataAccessResult(DCError error) {
        this.error = error;
    }

    private DataAccessResult(Data data) {
        this.data = data;
    }

    public DCError getError() {
        return error;
    }

    public Data getData() {
        return data;
    }

    public boolean hasValidData() {
        return (getError() == null) && (getData() != null);
    }

    public static <T> DataAccessResult<T> fromResult(T data) {
        return new DataAccessResult<>(data);
    }

    public static <T> DataAccessResult<T> fromUnavailable() {
        return fromResult(null);
    }

    public static <T> DataAccessResult<T> fromError(DCError error) {
        return new DataAccessResult<>(error);
    }
}
