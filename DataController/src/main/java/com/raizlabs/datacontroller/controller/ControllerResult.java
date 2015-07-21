package com.raizlabs.datacontroller.controller;

import com.raizlabs.datacontroller.DCError;
import com.raizlabs.datacontroller.DataResult;
import com.raizlabs.datacontroller.ErrorInfo;
import com.raizlabs.datacontroller.access.DataAccessResult;

// TODO - Casting this to DataResult hides errors and looks like "unavailable"... Is it OK to accept this risk?
public class ControllerResult<Data> implements DataResult<Data>, ErrorInfo {

    private DataAccessResult<Data> accessResult;
    private int sourceId;
    private boolean isFetching;

    public ControllerResult(DataAccessResult<Data> accessResult, int sourceId, boolean isFetching) {
        this.accessResult = accessResult;
        this.sourceId = sourceId;
        this.isFetching = isFetching;
    }

    @Override
    public Data getData() {
        return accessResult.getData();
    }

    @Override
    public int getDataSourceId() {
        return sourceId;
    }

    @Override
    public boolean isUpdatePending() {
        return isFetching;
    }

    @Override
    public DCError getError() {
        return accessResult.getError();
    }

    public boolean hasValidData() {
        return accessResult.hasValidData();
    }

    public DataAccessResult<Data> getAccessResult() {
        return accessResult;
    }
}
