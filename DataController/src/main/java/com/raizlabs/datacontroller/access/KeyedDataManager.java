package com.raizlabs.datacontroller.access;

public interface KeyedDataManager<K, V> {

    public boolean containsKey(K key);

    public <T> T get(K key);

    public void set(K key, V value);

    public void remove(K key);

    public void clear();

    public <T extends V> KeyedMemoryDataAccess<T> createDataAccess(K key);

    public <T extends V> KeyedMemoryDataAccess<T> createDataAccess(K key, int sourceId);
}
