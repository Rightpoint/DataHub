package com.raizlabs.datahub.observer;

import com.raizlabs.datahub.DataResult;
import com.raizlabs.datahub.ErrorInfo;
import com.raizlabs.datahub.hub.DataHubResult;

import java.util.LinkedHashMap;

public class BaseDataObserverListener<Data> implements DataObserverListener<Data> {

    private LinkedHashMap<Integer, DataHubResult<Data>> resultList;

    public BaseDataObserverListener() {
        resultList = new LinkedHashMap<>();
    }

    @Override
    public void onDataFetchStarted() {
        resultList.clear();
    }

    @Override
    public void onDataFetchFinished() {

    }

    @Override
    public void onResultReceived(DataHubResult<Data> result) {
        resultList.put(result.getAccessTypeId(), result);

        if (result.hasError()) {
            onErrorReceived(result);
        } else {
            onDataReceived(result);
        }
    }

    protected void onDataReceived(DataResult<Data> dataResult) {

    }

    protected void onErrorReceived(ErrorInfo errorInfo) {

    }

    public boolean hasResult() {
        return !resultList.isEmpty();
    }
}
