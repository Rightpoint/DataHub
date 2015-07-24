package com.raizlabs.datahub.sample.dataacess;

import android.util.LruCache;

public class MemoryCacheManager extends LruCache<String, Object> {
    private static MemoryCacheManager instance;

    /**
     * @param maxSize This is the maximum number of entries in the cache.
     */
    public static void init(int maxSize) {
        if(instance == null) {
            instance = new MemoryCacheManager(maxSize);
        }
    }

    public static MemoryCacheManager getInstance() {
        if(instance == null) {
            throw new IllegalStateException(MemoryCacheManager.class.getName() + " is not initialized.");
        }
        return instance;
    }

    private MemoryCacheManager(int maxSize) {
        super(maxSize);
    }
}
