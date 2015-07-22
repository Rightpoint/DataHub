package com.raizlabs.datacontroller.controller;

import com.raizlabs.datacontroller.DCError;
import com.raizlabs.datacontroller.DataResult;
import com.raizlabs.datacontroller.ErrorInfo;
import com.raizlabs.datacontroller.access.DataAccessResult;

public class DataControllerResult<Data> implements DataResult<Data>, ErrorInfo {

    private DataAccessResult<Data> accessResult;
    private int typeId;
    private boolean isFetching;

    public DataControllerResult(DataAccessResult<Data> accessResult, int typeId, boolean isFetching) {
        this.accessResult = accessResult;
        this.typeId = typeId;
        this.isFetching = isFetching;
    }

    @Override
    public Data getData() {
        return accessResult.getData();
    }

    @Override
    public int getAccessTypeId() {
        return typeId;
    }

    @Override
    public boolean isFetching() {
        return isFetching;
    }

    @Override
    public DCError getError() {
        return accessResult.getError();
    }

    public boolean hasData() {
        return accessResult.hasData();
    }

    public DataAccessResult<Data> getAccessResult() {
        return accessResult;
    }
}
