package com.raizlabs.datacontroller.controller;

import com.raizlabs.datacontroller.ErrorInfo;
import com.raizlabs.datacontroller.ResultInfo;

/**
 * A listener interface which handles the results from data controller
 * data access.
 * @param <Data> The type of data being accessed.
 */
public interface DataControllerListener<Data> {
    /**
     * Called when the data has been fetched successfully.
     * @param resultInfo The fetched data.
     */
    public void onDataReceived(ResultInfo<Data> resultInfo);

    /**
     * Called when the data fetch has failed.
     */
    public void onErrorReceived(ErrorInfo errorInfo);
}
