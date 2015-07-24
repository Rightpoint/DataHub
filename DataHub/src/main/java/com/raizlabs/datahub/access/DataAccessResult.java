package com.raizlabs.datahub.access;

import com.raizlabs.datahub.DCError;

public class DataAccessResult<Data> {

    public static <T> DataAccessResult<T> fromResult(T data) {
        return new DataAccessResult<>(data);
    }

    public static <T> DataAccessResult<T> fromUnavailable() {
        return fromResult(null);
    }

    public static <T> DataAccessResult<T> fromError(DCError error) {
        return new DataAccessResult<>(error);
    }

    private DCError error;
    private Data data;

    protected DataAccessResult(DCError error) {
        this.error = error;
    }

    protected DataAccessResult(Data data) {
        this.data = data;
    }

    public DCError getError() {
        return error;
    }

    public Data getData() {
        return data;
    }

    public boolean hasData() {
        return (getError() == null) && (getData() != null);
    }
}
