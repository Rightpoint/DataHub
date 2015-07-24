package com.raizlabs.datahub.access;

public abstract class BaseKeyedDataManager<K, V> implements KeyedDataManager<K, V> {

    @Override
    public <T extends V> KeyedMemoryDataAccess<T> createDataAccess(K key) {
        return new KeyedMemoryDataAccess<>(key, this);
    }

    @Override
    public <T extends V> KeyedMemoryDataAccess<T> createDataAccess(K key, int typeId) {
        return new KeyedMemoryDataAccess<>(key, typeId, this);
    }
}