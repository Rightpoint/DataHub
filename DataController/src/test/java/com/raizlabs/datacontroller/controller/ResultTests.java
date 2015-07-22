package com.raizlabs.datacontroller.controller;

import com.raizlabs.datacontroller.DCError;
import com.raizlabs.datacontroller.access.DataAccessResult;

import junit.framework.Assert;

import org.junit.Test;

public class ResultTests {

    @Test
    public void testProperties() {
        final int typeId = 290;
        final boolean isFetching = false;
        final Object data = new Object();
        final DataControllerResult<?> result = new DataControllerResult<>(DataAccessResult.fromResult(data), typeId, isFetching);

        Assert.assertEquals(typeId, result.getAccessTypeId());
        Assert.assertEquals(isFetching, result.isFetching());
    }

    @Test
    public void testUnavailable() {
        final DataControllerResult<?> result = new DataControllerResult<>(DataAccessResult.fromUnavailable(), 0, false);
        ControllerAssertions.assertDataUnavailable(result);
    }

    @Test
    public void testResult() {
        final Object data = new Object();
        final DataControllerResult<?> result = new DataControllerResult<>(DataAccessResult.fromResult(data), 0, false);
        ControllerAssertions.assertDataEquals(data, result);
    }

    @Test
    public void testError() {
        final DCError error = new DCError("", DCError.Types.INVALID_STATE);
        final DataControllerResult<?> result = new DataControllerResult<>(DataAccessResult.fromError(error), 0, false);
        ControllerAssertions.assertIsError(result);
    }
}
