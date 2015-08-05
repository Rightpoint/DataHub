package com.raizlabs.datahub.observer;

import com.raizlabs.datahub.hub.DataHubResult;

/**
 * A listener interface which responds to updates from a {@link DataObserver}.
 *
 * @param <Data> The type of data being accessed.
 */
public interface DataObserverListener<Data> {

    /**
     * Called when data fetching has begun, before any other callbacks are called.
     */
    void onDataFetchStarted();

    /**
     * Called after a data fetch has finished and the other callbacks have been called.
     */
    void onDataFetchFinished();

    /**
     * Called when a result has been received.
     *
     * @param result The received result.
     */
    void onResultReceived(DataHubResult<Data> result);
}
