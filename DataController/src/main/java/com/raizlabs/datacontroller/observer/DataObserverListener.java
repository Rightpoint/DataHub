package com.raizlabs.datacontroller.observer;

import com.raizlabs.datacontroller.DataResult;
import com.raizlabs.datacontroller.ErrorInfo;

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
     * Called when data has been fetched.
     * @param dataResult The fetched data.
     */
    public void onDataReceived(DataResult<Data> dataResult);

    /**
     * Called when the data fetch has failed.
     */
    public void onErrorReceived(ErrorInfo errorInfo);
}
