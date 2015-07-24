package com.raizlabs.datahub.hub;

import com.raizlabs.datahub.DCError;
import com.raizlabs.datahub.access.DataAccessResult;

import junit.framework.Assert;

import org.junit.Test;

public class ResultTests {

    @Test
    public void testProperties() {
        final int typeId = 290;
        final boolean isFetching = false;
        final Object data = new Object();
        final DataHubResult<?> result = new DataHubResult<>(DataAccessResult.fromResult(data), typeId, isFetching);

        Assert.assertEquals(typeId, result.getAccessTypeId());
        Assert.assertEquals(isFetching, result.isFetching());
    }

    @Test
    public void testUnavailable() {
        final DataHubResult<?> result = new DataHubResult<>(DataAccessResult.fromUnavailable(), 0, false);
        HubAssertions.assertDataUnavailable(result);
    }

    @Test
    public void testResult() {
        final Object data = new Object();
        final DataHubResult<?> result = new DataHubResult<>(DataAccessResult.fromResult(data), 0, false);
        HubAssertions.assertDataEquals(data, result);
    }

    @Test
    public void testError() {
        final DCError error = new DCError("", DCError.Types.INVALID_STATE);
        final DataHubResult<?> result = new DataHubResult<>(DataAccessResult.fromError(error), 0, false);
        HubAssertions.assertIsError(result);
    }
}
