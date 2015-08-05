package com.raizlabs.datahub.access;

import android.support.v4.util.LruCache;

/**
 * Implementation of a {@link KeyedDataManager} that keeps all key/value pairs in a memory cache which is trimmed when
 * too many items are added. This class uses a concept of "size" for each object and is set to allow a maximum total
 * size. By default, all items are assumed to have a size of 1. When the size is exceeded, the least recently accessed
 * objects will be trimmed.
 *
 * @param <K> {@inheritDoc}
 * @param <V> {@inheritDoc}
 * @see #sizeOf(Object, Object) - Override to change the sizing of individual objects.
 */
public class CachedKeyedDataManager<K, V> extends BaseKeyedDataManager<K, V> {

    private final LruCache<K, V> cache;

    /**
     * Creates a new {@link CachedKeyedDataManager} which permits the given maximum total size.
     *
     * @param size The maximum total size of all items allowed. Items will be trimmed when this is exceeded.
     */
    public CachedKeyedDataManager(int size) {
        this.cache = new LruCache<K, V>(size) {
            @Override
            protected int sizeOf(K key, V value) {
                return CachedKeyedDataManager.this.sizeOf(key, value);
            }
        };
    }

    @Override
    public boolean containsKey(K key) {
        return (cache.get(key) != null);
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
    public V remove(K key) {
        return cache.remove(key);
    }

    @Override
    public void clear() {
        cache.evictAll();
    }

    /**
     * Called to obtain the size of the value in the given key/value pair.
     *
     * @param key   The key of the item being queried for size.
     * @param value The value to return the size of.
     * @return The size of the given value.
     */
    protected int sizeOf(K key, V value) {
        return 1;
    }
}
