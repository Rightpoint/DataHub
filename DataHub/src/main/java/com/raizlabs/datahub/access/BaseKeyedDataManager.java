package com.raizlabs.datahub.access;

/**
 * Base class which implements {@link KeyedDataManager} and provides some of the basic functionality.
 *
 * @param <K> {@inheritDoc}
 * @param <V> {@inheritDoc}
 */
public abstract class BaseKeyedDataManager<K, V> implements KeyedDataManager<K, V> {

    @Override
    public <T extends V> KeyedMemoryDataAccess<T> createDataAccess(K key) {
        return new KeyedMemoryDataAccess<>(key, this);
    }

    @Override
    public <T extends V> KeyedMemoryDataAccess<T> createDataAccess(K key, int typeId) {
        return new KeyedMemoryDataAccess<>(key, this, typeId);
    }
}
