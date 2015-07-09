package com.raizlabs.datacontroller;

import com.raizlabs.datacontroller.access.KeyedDataAccess;
import com.raizlabs.datacontroller.access.MemoryDataManager;
import com.raizlabs.datacontroller.model.Employee;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Objects;

public class MemoryDataManagerTests {

    private static MemoryDataManager<String, Object> dataManager;

    @BeforeClass
    public static void setup() {
        dataManager = new MemoryDataManager<>();
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
}
