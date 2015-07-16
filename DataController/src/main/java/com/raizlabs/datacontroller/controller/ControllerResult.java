package com.raizlabs.datacontroller.controller;

import com.raizlabs.datacontroller.DCError;
import com.raizlabs.datacontroller.access.DataAccessResult;

public class ControllerResult<Data> {

    private DataAccessResult<Data> accessResult;
    private int sourceId;
    private boolean isFetching;

    public ControllerResult(DataAccessResult<Data> accessResult, int sourceId, boolean isFetching) {
        this.accessResult = accessResult;
        this.sourceId = sourceId;
        this.isFetching = isFetching;
    }

    public Data getData() {
        return accessResult.getData();
    }

    public DCError getError() {
        return accessResult.getError();
    }

    public boolean wasDataAvailable() {
        return accessResult.wasDataAvailable();
    }

    public boolean hasValidData() {
        return accessResult.hasValidData();
    }

    public DataAccessResult<Data> getAccessResult() {
        return accessResult;
    }

    public int getSourceId() {
        return sourceId;
    }

    public boolean isFetching() {
        return isFetching;
    }
}
