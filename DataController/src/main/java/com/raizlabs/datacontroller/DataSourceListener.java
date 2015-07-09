package com.raizlabs.datacontroller;

public interface DataSourceListener<Data> {

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
     * @param resultInfo The fetched data.
     */
    public void onDataReceived(ResultInfo<Data> resultInfo);

    /**
     * Called when the data fetch has failed.
     */
    public void onErrorReceived(ErrorInfo errorInfo);
}
