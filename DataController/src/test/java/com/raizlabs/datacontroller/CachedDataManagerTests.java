package com.raizlabs.datacontroller;

import com.raizlabs.datacontroller.access.CachedDataManager;
import com.raizlabs.datacontroller.access.KeyedDataAccess;
import com.raizlabs.datacontroller.model.Employee;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class CachedDataManagerTests {

    private static final int SIZE = 5;

    private static CachedDataManager<String, Object> dataManager;

    @BeforeClass
    public static void setup() {
        dataManager = new CachedDataManager<>(SIZE);
    }

    @Before
    public void preTest() {
        dataManager.clear();
    }

    @Test
    public void testManagerClear() {
        final String key = "key";

        dataManager.set(key, new Object());
        Assert.assertNotNull(dataManager.get(key));

        dataManager.clear();
        Assert.assertNull(dataManager.get(key));
    }

    @Test
    public void testAccessSetup() {
        final String key = "key";
        final int sourceId = 50;
        KeyedDataAccess<Employee> access = dataManager.createDataAccess(key, sourceId);

        Assert.assertEquals(key, access.getKey());
        Assert.assertEquals(sourceId, access.getSourceId());
    }

    @Test
    public void testPut() {
        final String key = "testKey";
        final Object value = new Object();
        final KeyedDataAccess<Object> access = dataManager.createDataAccess(key);

        Assert.assertNull(access.get());

        dataManager.set(key, value);
        Assert.assertEquals(value, access.get());

    }

    @Test
    public void testImportAndClear() {
        final String key = "importKey";
        final Object value = new Object();
        final KeyedDataAccess<Object> access = dataManager.createDataAccess(key);

        Assert.assertNull(access.get());

        access.importData(value);
        Assert.assertEquals(value, access.get());

        access.clear();
        Assert.assertNull(access.get());
    }

    @Test
    public void testSizeAndOverflow() {
        List<KeyedDataAccess<Object>> accesses = new LinkedList<>();
        for (int i = 0; i < SIZE; i++) {
            KeyedDataAccess<Object> access = dataManager.createDataAccess(Integer.toString(i));
            accesses.add(access);

            access.importData(new Object());
        }

        // Verify all values exist
        // This accesses in the same order as they were added such that the first item is the LRU in the cache
        for (KeyedDataAccess<Object> access : accesses) {
            Assert.assertNotNull(access.get());
        }

        // Overflow once and expect the first item to be missing.
        KeyedDataAccess<Object> overflowAccess = dataManager.createDataAccess("overflow");
        accesses.add(overflowAccess);
        overflowAccess.importData(new Object());

        boolean first = true;
        for (KeyedDataAccess<Object> access : accesses) {
            if (first) {
                Assert.assertNull(access.get());
            } else {
                Assert.assertNotNull(access.get());
            }

            first = false;
        }
    }
}
