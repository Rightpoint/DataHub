package com.raizlabs.datacontroller.observer;

import com.raizlabs.datacontroller.DataResult;
import com.raizlabs.datacontroller.ErrorInfo;
import com.raizlabs.datacontroller.controller.DataControllerResult;

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
    public void onResultReceived(DataControllerResult<Data> result);
}
