package com.raizlabs.datahub.hub;

import com.raizlabs.datahub.access.AccessAssertions;

import junit.framework.Assert;

public class HubAssertions {

    public static void assertDataUnavailable(DataHubResult<?> result) {
        AccessAssertions.assertDataUnavailable(result.getAccessResult());
        Assert.assertNull(result.getData());
        Assert.assertFalse(result.hasData());
    }

    public static void assertDataNotNull(DataHubResult<?> result) {
        AccessAssertions.assertDataNotNull(result.getAccessResult());
        Assert.assertNotNull(result.getData());
        Assert.assertNull(result.getError());
        Assert.assertTrue(result.hasData());
    }

    public static void assertDataEquals(Object expectedData, DataHubResult<?> result) {
        AccessAssertions.assertDataEquals(expectedData, result.getAccessResult());
        Assert.assertNotNull(result.getData());
        Assert.assertNull(result.getError());
        Assert.assertTrue(result.hasData());
    }

    public static void assertIsError(DataHubResult<?> result) {
        AccessAssertions.assertIsError(result.getAccessResult());
        Assert.assertNull(result.getData());
        Assert.assertNotNull(result.getError());
        Assert.assertFalse(result.hasData());
    }

}
