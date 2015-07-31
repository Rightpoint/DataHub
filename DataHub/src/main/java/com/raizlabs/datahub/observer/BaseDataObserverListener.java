package com.raizlabs.datahub.observer;

import com.raizlabs.datahub.DataResult;
import com.raizlabs.datahub.ErrorInfo;
import com.raizlabs.datahub.hub.DataHub;
import com.raizlabs.datahub.hub.DataHubResult;

import java.util.LinkedHashMap;

public abstract class BaseDataObserverListener<Data> implements DataObserverListener<Data> {

    private LinkedHashMap<Integer, DataHubResult<Data>> resultList;

    private int lastTypeId;
    private int lastDataTypeId;

    public BaseDataObserverListener() {
        resultList = new LinkedHashMap<>();
        lastTypeId = DataHub.AccessTypeIds.NONE;
        lastDataTypeId = DataHub.AccessTypeIds.NONE;
    }

    @Override
    public void onDataFetchStarted() {
        resultList.clear();
        lastTypeId = DataHub.AccessTypeIds.NONE;
        lastDataTypeId = DataHub.AccessTypeIds.NONE;
        onFetchStarted();
    }

    @Override
    public void onDataFetchFinished() {
        onFetchFinished();
        onFinalDataReceived(getResult(lastDataTypeId));
    }

    @Override
    public void onResultReceived(DataHubResult<Data> result) {
        if (result.hasError()) {
            onErrorReceived(result);
        } else {
            lastDataTypeId = result.getAccessTypeId();
            onDataReceived(result);
        }
    }

    public boolean hasResult() {
        return !resultList.isEmpty();
    }

    public abstract void onFetchStarted();
    public abstract void onFetchFinished();
    public abstract void onDataReceived(DataResult<Data> data);
    public abstract void onErrorReceived(ErrorInfo error);

    /**
     * Called with the last {@link DataResult} which was stored and dispatched. This is called when the request for
     * data has been finished.
     *
     * @param dataResult The last stored and dispatched {@link DataResult}, potentially null.
     */
    protected void onFinalDataReceived(DataResult<Data> dataResult) {

    }

    protected DataHubResult<Data> getResult(int accessTypeId) {
        return resultList.get(accessTypeId);
    }

    protected DataHubResult<Data> getMostRecentResult() {
        return getResult(lastTypeId);
    }

    protected DataHubResult<Data> getMostRecentDataResult() {
        return getResult(lastDataTypeId);
    }
}
