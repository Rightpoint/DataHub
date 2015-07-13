package com.raizlabs.datacontroller.controller;

import com.raizlabs.datacontroller.DCError;
import com.raizlabs.datacontroller.DataAccessResult;

import junit.framework.Assert;

import org.junit.Test;

public class ResultTests {

    @Test
    public void testProperties() {
        final int sourceId = 290;
        final boolean isFetching = false;
        final Object data = new Object();
        final ControllerResult<?> result = new ControllerResult<>(DataAccessResult.fromResult(data), sourceId, isFetching);

        Assert.assertEquals(sourceId, result.getSourceId());
        Assert.assertEquals(isFetching, result.isFetching());
    }

    @Test
    public void testUnavailable() {
        final ControllerResult<?> result = new ControllerResult<>(DataAccessResult.fromUnavailable(), 0, false);
        ControllerAssertions.assertDataUnavailable(result);
    }

    @Test
    public void testResult() {
        final Object data = new Object();
        final ControllerResult<?> result = new ControllerResult<>(DataAccessResult.fromResult(data), 0, false);
        ControllerAssertions.assertDataEquals(data, result);
    }

    @Test
    public void testError() {
        final DCError error = new DCError("", DCError.Types.INVALID_STATE);
        final ControllerResult<?> result = new ControllerResult<>(DataAccessResult.fromError(error), 0, false);
        ControllerAssertions.assertIsError(result);
    }
}
