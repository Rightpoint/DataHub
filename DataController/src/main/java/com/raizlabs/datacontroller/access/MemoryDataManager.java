package com.raizlabs.datacontroller.access;

import java.util.HashMap;
import java.util.Map;

public class MemoryDataManager<K, V> extends BaseKeyedDataManager<K, V> {

    //region Statics
    private static final MemoryDataManager GLOBAL_INSTANCE = new MemoryDataManager();

    public static MemoryDataManager getGlobalInstance() {
        return GLOBAL_INSTANCE;
    }
    //endregion Statics

    private Map<K, V> map = new HashMap<>();

    public MemoryDataManager() {

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
    public void remove(K key) {
        map.remove(key);
    }

    @Override
    public void clear() {
        map.clear();
    }
    //endregion Methods
}
