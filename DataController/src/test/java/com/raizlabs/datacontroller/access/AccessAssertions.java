package com.raizlabs.datacontroller.access;

import junit.framework.Assert;

public class AccessAssertions {

    public static void assertDataUnavailable(DataAccessResult<?> result) {
        Assert.assertNull(result.getData());
        Assert.assertNull(result.getError());
        Assert.assertFalse(result.wasDataAvailable());
        Assert.assertFalse(result.hasValidData());
    }

    public static void assertDataUnavailable(SynchronousDataAccess<?> access) {
        assertDataUnavailable(access.get());
    }

    public static void assertDataNotNull(DataAccessResult<?> result) {
        Assert.assertNotNull(result.getData());
        Assert.assertNull(result.getError());
        Assert.assertTrue(result.wasDataAvailable());
        Assert.assertTrue(result.hasValidData());
    }

    public static void assertDataNotNull(SynchronousDataAccess<?> access) {
        assertDataNotNull(access.get());
    }

    public static void assertDataEquals(Object expectedData, DataAccessResult<?> result) {
        Assert.assertEquals(expectedData, result.getData());
        Assert.assertNull(result.getError());
        Assert.assertTrue(result.wasDataAvailable());
        Assert.assertTrue(result.hasValidData());
    }

    public static void assertDataEquals(Object expectedData, SynchronousDataAccess<?> access) {
        assertDataEquals(expectedData, access.get());
    }

    public static void assertIsError(DataAccessResult<?> result) {
        Assert.assertNull(result.getData());
        Assert.assertNotNull(result.getError());
        Assert.assertFalse(result.hasValidData());
    }

    public static void assertIsError(SynchronousDataAccess<?> access) {
        assertIsError(access.get());
    }
}
