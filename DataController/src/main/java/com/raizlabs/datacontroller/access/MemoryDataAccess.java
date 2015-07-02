package com.raizlabs.datacontroller.access;

public interface MemoryDataAccess<Key, Data> {
    /**
     * Set or update the data in local cache.
     * This will also save/persist the current timestamp to indicate the data was updated
     * @param data data to set or update.
     */
    public void  setData(Key key, Data data);

    /**
     * @return The data being accessed.
     */
    public Data getData(Key key);

    public void clearData(Key key);
}
