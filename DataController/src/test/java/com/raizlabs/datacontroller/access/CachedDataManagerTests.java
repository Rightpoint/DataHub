package com.raizlabs.datacontroller.access;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class CachedDataManagerTests extends BaseKeyedDataManagerTests {

    private static final int SIZE = 5;

    private static CachedDataManager<String, Object> dataManager;

    @BeforeClass
    public static void setup() {
        dataManager = new CachedDataManager<>(SIZE);
    }

    @Override
    public CachedDataManager<String, Object> getDataManager() {
        return dataManager;
    }

    @Test
    public void testSizeAndOverflow() {
        List<KeyedMemoryDataAccess<Object>> accesses = new LinkedList<>();
        for (int i = 0; i < SIZE; i++) {
            KeyedMemoryDataAccess<Object> access = dataManager.createDataAccess(Integer.toString(i));
            accesses.add(access);

            access.importData(new Object());
        }

        // Verify all values exist
        // This accesses in the same order as they were added such that the first item is the LRU in the cache
        for (KeyedMemoryDataAccess<Object> access : accesses) {
            AccessAssertions.assertDataNotNull(access);
        }

        // Overflow once and expect the first item to be missing.
        KeyedMemoryDataAccess<Object> overflowAccess = dataManager.createDataAccess("overflow");
        accesses.add(overflowAccess);
        overflowAccess.importData(new Object());

        boolean first = true;
        for (KeyedMemoryDataAccess<Object> access : accesses) {
            if (first) {
                AccessAssertions.assertDataUnavailable(access);
            } else {
                AccessAssertions.assertDataNotNull(access);
            }

            first = false;
        }
    }
}
