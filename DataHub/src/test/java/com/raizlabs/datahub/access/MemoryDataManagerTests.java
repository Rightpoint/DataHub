package com.raizlabs.datahub.access;

import org.junit.BeforeClass;

public class MemoryDataManagerTests extends BaseKeyedDataManagerTests {
    private static MemoryKeyedDataManager<String, Object> dataManager;

    @BeforeClass
    public static void setup() {
        dataManager = new MemoryKeyedDataManager<>();
    }

    @Override
    protected KeyedDataManager<String, Object> getDataManager() {
        return dataManager;
    }
}
