package com.raizlabs.datahub.access;

import com.raizlabs.datahub.DataHubError;

import org.junit.Test;

public class ResultTests {

    @Test
    public void testUnavailable() {
        final DataAccessResult<?> result = DataAccessResult.fromUnavailable();
        AccessAssertions.assertDataUnavailable(result);
    }

    @Test
    public void testResult() {
        final Object data = new Object();
        final DataAccessResult<Object> result = DataAccessResult.fromResult(data);
        AccessAssertions.assertDataEquals(data, result);
    }

    @Test
    public void testError() {
        final DataHubError error = new DataHubError("", DataHubError.Types.INVALID_STATE);
        final DataAccessResult<?> result = DataAccessResult.fromError(error);
        AccessAssertions.assertIsError(result);
    }
}
