package com.raizlabs.datacontroller.sample.dataacess;

import com.raizlabs.datacontroller.access.m.MemoryDataAccess;

public class BaseMemoryDataAccess<Data> implements MemoryDataAccess<String, Data> {

    @Override
    public void setData(String key, Data data) {
        MemoryCacheManager.getInstance().put(key, data);
    }

    @Override
    public Data getData(String key) {
        return (Data) MemoryCacheManager.getInstance().get(key);
    }

    @Override
    public void clearData(String key) {
        MemoryCacheManager.getInstance().remove(key);
    }
}
