package com.raizlabs.datahub.access;

import junit.framework.Assert;

public class AccessAssertions {

    public static void assertDataUnavailable(DataAccessResult<?> result) {
        Assert.assertNull(result.getData());
        Assert.assertNull(result.getError());
        Assert.assertFalse(result.hasData());
    }

    public static void assertDataUnavailable(SyncDataAccess<?> access) {
        assertDataUnavailable(access.get());
    }

    public static void assertDataNotNull(DataAccessResult<?> result) {
        Assert.assertNotNull(result.getData());
        Assert.assertNull(result.getError());
        Assert.assertTrue(result.hasData());
    }

    public static void assertDataNotNull(SyncDataAccess<?> access) {
        assertDataNotNull(access.get());
    }

    public static void assertDataEquals(Object expectedData, DataAccessResult<?> result) {
        Assert.assertEquals(expectedData, result.getData());
        Assert.assertNull(result.getError());
        Assert.assertTrue(result.hasData());
    }

    public static void assertDataEquals(Object expectedData, SyncDataAccess<?> access) {
        assertDataEquals(expectedData, access.get());
    }

    public static void assertIsError(DataAccessResult<?> result) {
        Assert.assertNull(result.getData());
        Assert.assertNotNull(result.getError());
        Assert.assertFalse(result.hasData());
    }

    public static void assertIsError(SyncDataAccess<?> access) {
        assertIsError(access.get());
    }
}
