package com.raizlabs.datacontroller.access;

import com.raizlabs.datacontroller.ErrorInfo;

/**
 * A listener interface which handles the results from asynchronous
 * data access.
 * @param <Data> The type of data being accessed.
 */
public interface DiskDataAccessListener<Data> {
    /**
     * Called when the data has been fetched successfully.
     * @param data The fetched data.
     */
    public void onDataReceived(Data data, long lastUpdatedTimestamp);

    /**
     * Called when the data fetch has failed.
     */
    public void onErrorReceived(ErrorInfo errorInfo);
}
