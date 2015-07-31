package com.raizlabs.datahub.observer;

import com.raizlabs.datahub.DataResult;
import com.raizlabs.datahub.ErrorInfo;
import com.raizlabs.datahub.hub.DataHubResult;

public class SimpleDataObserverListener<Data> extends BaseDataObserverListener<Data> {

    @Override
    public final void onDataFetchStarted() {
        super.onDataFetchStarted();
    }

    @Override
    public final void onDataFetchFinished() {
        super.onDataFetchFinished();
    }

    @Override
    public final void onResultReceived(DataHubResult<Data> result) {
        super.onResultReceived(result);
    }

    @Override
    public void onFetchStarted() {

    }

    @Override
    public void onFetchFinished() {

    }

    @Override
    public void onDataReceived(DataResult<Data> data) {

    }

    @Override
    public void onErrorReceived(ErrorInfo error) {

    }
}
