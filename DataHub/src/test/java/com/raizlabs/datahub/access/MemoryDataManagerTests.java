package com.raizlabs.datahub.access;

import org.junit.BeforeClass;

public class MemoryDataManagerTests extends BaseKeyedDataManagerTests {
    private static MemoryDataManager<String, Object> dataManager;

    @BeforeClass
    public static void setup() {
        dataManager = new MemoryDataManager<>();
    }

    @Override
    protected KeyedDataManager<String, Object> getDataManager() {
        return dataManager;
    }
}
