package com.raizlabs.datacontroller.access;

/**
 * Interface which allows data to be accessed (synchronously) from disk.
 * @param <Data> The type of data to be accessed.
 */
public interface DiskDataAccess<Key, Data> {
    /**
     * Set or update the data in local cache.
     * This will also save/persist the current timestamp to indicate the data was updated
     * @param data The data to set or update.
     */

    public void setData(Key key, Data data, long lastUpdatedTimestamp);
    /**
     * @return The data being accessed.
     */
    public void getData(Key key, DiskDataAccessListener<Data> listener);

    /**
     * Indicates that this data will no longer be accessed and any resources
     * may be cleaned up.
     */
    public void close();
}
