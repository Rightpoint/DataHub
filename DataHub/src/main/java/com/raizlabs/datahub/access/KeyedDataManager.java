package com.raizlabs.datahub.access;

public interface KeyedDataManager<K, V> {

    boolean containsKey(K key);

    <T> T get(K key);

    void set(K key, V value);

    void remove(K key);

    void clear();

    <T extends V> KeyedMemoryDataAccess<T> createDataAccess(K key);

    <T extends V> KeyedMemoryDataAccess<T> createDataAccess(K key, int typeId);
}
