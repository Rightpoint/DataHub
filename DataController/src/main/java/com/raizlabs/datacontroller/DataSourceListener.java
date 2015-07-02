package com.raizlabs.datacontroller;

public interface DataSourceListener<Data> {

    /**
     * Called when data fetching has begun.
     */
    public void onDataFetching();

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
