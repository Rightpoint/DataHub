package com.raizlabs.datacontroller.access;

import android.support.v4.util.LruCache;

public class CachedDataManager<K, V> extends BaseKeyedDataManager<K, V> {

    private final LruCache<K, V> cache;

    public CachedDataManager(int size) {
        this.cache = new LruCache<K, V>(size) {
            @Override
            protected int sizeOf(K key, V value) {
                return CachedDataManager.this.sizeOf(key, value);
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(K key) {
        try {
            return (T) cache.get(key);
        } catch (Exception e) {
            // If anything goes wrong (bad casts, nulls, etc) just return nothing.
            return null;
        }
    }

    @Override
    public void set(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public void remove(K key) {
        cache.remove(key);
    }

    @Override
    public void clear() {
        cache.evictAll();
    }

    protected int sizeOf(K key, V value) {
        return 1;
    }
}
