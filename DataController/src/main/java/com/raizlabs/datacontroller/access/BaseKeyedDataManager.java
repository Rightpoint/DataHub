package com.raizlabs.datacontroller.access;

public abstract class BaseKeyedDataManager<K, V> implements KeyedDataManager<K, V> {

    @Override
    public <T extends V> KeyedDataAccess<T> createDataAccess(K key) {
        return new KeyedDataAccess<>(key, this);
    }

    @Override
    public <T extends V> KeyedDataAccess<T> createDataAccess(K key, int sourceId) {
        return new KeyedDataAccess<>(key, sourceId, this);
    }
}
