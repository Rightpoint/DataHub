package com.raizlabs.datacontroller.access;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public abstract class BaseKeyedDataManagerTests {


    protected abstract KeyedDataManager<String, Object> getDataManager();

    @Before
    public void preTest() {
        getDataManager().clear();
    }

    @Test
    public void testManagerClear() {
        final String key = "key";

        getDataManager().set(key, new Object());
        Assert.assertNotNull(getDataManager().get(key));

        getDataManager().clear();
        Assert.assertNull(getDataManager().get(key));
    }

    @Test
    public void testAccessSetup() {
        final String key = "key";
        final int sourceId = 50;
        KeyedMemoryDataAccess<Object> access = getDataManager().createDataAccess(key, sourceId);

        Assert.assertEquals(key, access.getKey());
        Assert.assertEquals(sourceId, access.getSourceId());
    }

    @Test
    public void testPut() {
        final String key = "testKey";
        final Object value = new Object();
        final KeyedMemoryDataAccess<Object> access = getDataManager().createDataAccess(key);

        AccessAssertions.assertDataUnavailable(access);

        getDataManager().set(key, value);
        AccessAssertions.assertDataEquals(value, access);
    }

    @Test
    public void testMultipleAccesses() {
        // Test that two access bound to the same key obtain the same value
        final String key = "testKey";
        final Object value = new Object();
        final KeyedMemoryDataAccess<Object> access1 = getDataManager().createDataAccess(key);
        final KeyedMemoryDataAccess<Object> access2 = getDataManager().createDataAccess(key);

        AccessAssertions.assertDataUnavailable(access1);
        AccessAssertions.assertDataUnavailable(access2);

        getDataManager().set(key, value);
        AccessAssertions.assertDataEquals(value, access1);
        AccessAssertions.assertDataEquals(value, access2);
    }

    @Test
    public void testImportAndClear() {
        final String key = "importKey";
        final Object value = new Object();
        final KeyedMemoryDataAccess<Object> access = getDataManager().createDataAccess(key);

        AccessAssertions.assertDataUnavailable(access);

        access.importData(value);
        AccessAssertions.assertDataEquals(value, access);

        access.clear();
        AccessAssertions.assertDataUnavailable(access);
    }
}
