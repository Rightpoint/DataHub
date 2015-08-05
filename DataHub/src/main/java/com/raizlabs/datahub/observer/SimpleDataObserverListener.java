package com.raizlabs.datahub.observer;

import com.raizlabs.datahub.DataHubErrorInfo;
import com.raizlabs.datahub.DataResult;
import com.raizlabs.datahub.hub.DataHubResult;

/**
 * Class which greatly simplifies the implementation of {@link DataObserverListener}. Extends
 * {@link BaseDataObserverListener} for some convenience methods for result history, as well as overriding all methods
 * such that subclasses need only implement the ones they want.
 *
 * @param <Data> {@inheritDoc}
 */
public abstract class SimpleDataObserverListener<Data> extends BaseDataObserverListener<Data> {

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
    public void onErrorReceived(DataHubErrorInfo error) {

    }
}
