package com.raizlabs.datahub.observer;

import com.raizlabs.datahub.hub.DataHubResult;

public interface DataObserverListener<Data> {

    /**
     * Called when data fetching has begun, before any other callbacks are called.
     */
    public void onDataFetchStarted();

    /**
     * Called after a data fetch has finished and the other callbacks have been called.
     */
    public void onDataFetchFinished();

    /**
     * Called when a result has been received.
     *
     * @param result The received result.
     */
    public void onResultReceived(DataHubResult<Data> result);
}
