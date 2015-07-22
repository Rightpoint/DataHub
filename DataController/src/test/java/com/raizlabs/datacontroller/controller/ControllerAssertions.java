package com.raizlabs.datacontroller.controller;

import com.raizlabs.datacontroller.access.AccessAssertions;

import junit.framework.Assert;

public class ControllerAssertions {

    public static void assertDataUnavailable(DataControllerResult<?> result) {
        AccessAssertions.assertDataUnavailable(result.getAccessResult());
        Assert.assertNull(result.getData());
        Assert.assertFalse(result.hasData());
    }

    public static void assertDataNotNull(DataControllerResult<?> result) {
        AccessAssertions.assertDataNotNull(result.getAccessResult());
        Assert.assertNotNull(result.getData());
        Assert.assertNull(result.getError());
        Assert.assertTrue(result.hasData());
    }

    public static void assertDataEquals(Object expectedData, DataControllerResult<?> result) {
        AccessAssertions.assertDataEquals(expectedData, result.getAccessResult());
        Assert.assertNotNull(result.getData());
        Assert.assertNull(result.getError());
        Assert.assertTrue(result.hasData());
    }

    public static void assertIsError(DataControllerResult<?> result) {
        AccessAssertions.assertIsError(result.getAccessResult());
        Assert.assertNull(result.getData());
        Assert.assertNotNull(result.getError());
        Assert.assertFalse(result.hasData());
    }

}
