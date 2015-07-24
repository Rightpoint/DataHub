package com.raizlabs.datahub.hub;

import com.raizlabs.datahub.DCError;
import com.raizlabs.datahub.DataResult;
import com.raizlabs.datahub.ErrorInfo;
import com.raizlabs.datahub.access.DataAccessResult;

public class DataHubResult<Data> implements DataResult<Data>, ErrorInfo {

    private DataAccessResult<Data> accessResult;
    private int typeId;
    private boolean isFetching;

    public DataHubResult(DataAccessResult<Data> accessResult, int typeId, boolean isFetching) {
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

    public boolean hasError() {
        return (getError() != null);
    }

    public boolean hasData() {
        return accessResult.hasData();
    }

    public DataAccessResult<Data> getAccessResult() {
        return accessResult;
    }
}
