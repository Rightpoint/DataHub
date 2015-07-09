package com.raizlabs.datacontroller.access;

import android.support.v4.util.LruCache;

public class CachedMemoryDataAccess<Data> implements SynchronousDataAccess<Data> {


    @Override
    public Data get() {
        return null;
    }

    @Override
    public void importData(Data data) {

    }

    @Override
    public void close() {

    }

    @Override
    public int getSourceId() {
        return 0;
    }

}
