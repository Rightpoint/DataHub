package com.raizlabs.datacontroller.access;

public interface WebDataAccess<Data> {
    /**
     * Attempts to fetch the data, calling the given listener with a result
     * when it completes.
     * @param listener A listener to call when the fetch completes or fails.
     *                 If this method is recalled with a different
     */
    public void getData(WebDataAccessListener<Data> listener);

    /**
     * Indicates that this data will no longer be accessed and any resources
     * may be cleaned up. It also cancels a request, if it is already placed.
     */
    public void close();
}
