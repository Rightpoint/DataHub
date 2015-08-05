package com.raizlabs.datahub.access;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of a {@link KeyedDataManager} that keeps all key/value pairs in memory until they are removed.
 *
 * @param <K> {@inheritDoc}
 * @param <V> {@inheritDoc}
 */
public class MemoryKeyedDataManager<K, V> extends BaseKeyedDataManager<K, V> {

    //region Statics
    private static final MemoryKeyedDataManager<Object, Object> GLOBAL_INSTANCE = new MemoryKeyedDataManager<>();

    /**
     * @return The shared global static instance of a general {@link MemoryKeyedDataManager}.
     */
    public static MemoryKeyedDataManager<Object, Object> getGlobalInstance() {
        return GLOBAL_INSTANCE;
    }
    //endregion Statics

    private Map<K, V> map = new HashMap<>();

    /**
     * Creates a new empty {@link MemoryKeyedDataManager}.
     */
    public MemoryKeyedDataManager() {

    }

    @Override
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    //region Methods
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(K key) {
        try {
            return (T) map.get(key);
        } catch (Exception e) {
            // If anything goes wrong (bad casts, nulls, etc) just return nothing.
            return null;
        }
    }

    @Override
    public void set(K key, V value) {
        map.put(key, value);
    }

    @Override
    public V remove(K key) {
        return map.remove(key);
    }

    @Override
    public void clear() {
        map.clear();
    }
    //endregion Methods
}
