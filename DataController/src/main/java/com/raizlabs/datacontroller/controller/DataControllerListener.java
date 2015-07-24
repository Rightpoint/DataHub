package com.raizlabs.datacontroller.controller;

import com.raizlabs.datacontroller.ErrorInfo;
import com.raizlabs.datacontroller.DataResult;

/**
 * A listener interface which handles the results from data controller
 * data access.
 *
 * @param <Data> The type of data being accessed.
 */
public interface DataControllerListener<Data> {
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
    public void onResultReceived(DataControllerResult<Data> result);
}
