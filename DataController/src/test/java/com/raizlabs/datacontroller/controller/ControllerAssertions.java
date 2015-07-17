package com.raizlabs.datacontroller.controller;

import com.raizlabs.datacontroller.access.AccessAssertions;

import junit.framework.Assert;

public class ControllerAssertions {

    public static void assertDataUnavailable(ControllerResult<?> result) {
        AccessAssertions.assertDataUnavailable(result.getAccessResult());
        Assert.assertNull(result.getData());
        Assert.assertFalse(result.hasValidData());
    }

    public static void assertDataNotNull(ControllerResult<?> result) {
        AccessAssertions.assertDataNotNull(result.getAccessResult());
        Assert.assertNotNull(result.getData());
        Assert.assertNull(result.getError());
        Assert.assertTrue(result.hasValidData());
    }

    public static void assertDataEquals(Object expectedData, ControllerResult<?> result) {
        AccessAssertions.assertDataEquals(expectedData, result.getAccessResult());
        Assert.assertNotNull(result.getData());
        Assert.assertNull(result.getError());
        Assert.assertTrue(result.hasValidData());
    }

    public static void assertIsError(ControllerResult<?> result) {
        AccessAssertions.assertIsError(result.getAccessResult());
        Assert.assertNull(result.getData());
        Assert.assertNotNull(result.getError());
        Assert.assertFalse(result.hasValidData());
    }

}
