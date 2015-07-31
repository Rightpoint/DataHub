package com.raizlabs.datahub.hub;

import com.raizlabs.datahub.access.AccessAssertions;
import com.raizlabs.datahub.access.AsynchronousDataAccess;
import com.raizlabs.datahub.access.DataAccess;
import com.raizlabs.datahub.access.DataAccessResult;
import com.raizlabs.datahub.access.TemporaryMemoryAccess;
import com.raizlabs.datahub.hub.helpers.ImmediateResponseAsyncAccess;
import com.raizlabs.datahub.hub.ordered.FetchStrategies;
import com.raizlabs.datahub.hub.ordered.OrderedDataHub;
import com.raizlabs.datahub.utils.Wrapper;

import junit.framework.Assert;

import org.junit.Test;

public class SerialDataHubTests extends BaseOrderedDataHubTests {

    @Override
    protected OrderedDataHub.Builder<Object> createNewBuilder() {
        return OrderedDataHub.Builder.newSerial(new FetchStrategies.Serial.DataValidator<Object>() {
            @Override
            public boolean isFinal(DataAccessResult<Object> result, DataAccess access) {
                return false;
            }
        });
    }

    @Test
    public void testStopOnFinal() {
        final Object validValue = new Object();
        final Object invalidValue = new Object();

        final DataAccessResult<Object> validResult = DataAccessResult.fromResult(validValue);
        final DataAccessResult<Object> invalidResult = DataAccessResult.fromResult(invalidValue);

        final TemporaryMemoryAccess<Object> memoryAccess = new TemporaryMemoryAccess<>();

        final AsynchronousDataAccess<Object> validAccess = new ImmediateResponseAsyncAccess<>(validResult, 5);
        final AsynchronousDataAccess<Object> invalidAccess = new ImmediateResponseAsyncAccess<>(invalidResult, 6);

        final FetchStrategies.Serial.DataValidator<Object> validator = new FetchStrategies.Serial.DataValidator<Object>() {
            @Override
            public boolean isFinal(DataAccessResult<Object> result, DataAccess access) {
                return validValue.equals(result.getData());
            }
        };

        DataHub<Object> dataHub =
                OrderedDataHub.Builder.newSerial(validator)
                        .setSynchronousAccess(memoryAccess)
                        .addAsynchronousAccess(validAccess)
                        .addAsynchronousAccess(invalidAccess)
                        .build();

        // Assure that our initial state is empty
        AccessAssertions.assertDataUnavailable(memoryAccess);

        // Do a fetch and make sure we only have the valid data
        // Ensuring that we "stop" when we see a final result
        dataHub.fetch();
        AccessAssertions.assertDataEquals(validValue, memoryAccess);

        // Reset...
        memoryAccess.clear();

        // Assure that our initial state is empty
        AccessAssertions.assertDataUnavailable(memoryAccess);

        // Make sure we keep going UNTIL we see a final result
        dataHub =
                OrderedDataHub.Builder.newSerial(validator)
                        .setSynchronousAccess(memoryAccess)
                        .addAsynchronousAccess(invalidAccess)
                        .addAsynchronousAccess(validAccess)
                        .build();

        dataHub.fetch();
        AccessAssertions.assertDataEquals(validValue, memoryAccess);
    }

    @Test
    public void testDispatchReturnable() {
        final Object validValue = new Object();
        final Object invalidValue = new Object();

        final DataAccessResult<Object> validResult = DataAccessResult.fromResult(validValue);
        final DataAccessResult<Object> invalidResult = DataAccessResult.fromResult(invalidValue);

        final TemporaryMemoryAccess<Object> memoryAccess = new TemporaryMemoryAccess<>();

        final AsynchronousDataAccess<Object> validAccess = new ImmediateResponseAsyncAccess<>(validResult, 5);
        final AsynchronousDataAccess<Object> invalidAccess = new ImmediateResponseAsyncAccess<>(invalidResult, 6);

        final FetchStrategies.Serial.DataValidator<Object> validator = new FetchStrategies.Serial.DataValidator<Object>() {
            @Override
            public boolean isFinal(DataAccessResult<Object> result, DataAccess access) {
                return validValue.equals(result.getData());
            }
        };

        final DataHub<Object> dataHub =
                OrderedDataHub.Builder.newSerial(validator)
                        .setSynchronousAccess(memoryAccess)
                        .addAsynchronousAccess(invalidAccess)
                        .addAsynchronousAccess(validAccess)
                        .build();

        final Wrapper<Boolean> receivedValid = new Wrapper<>(false);
        final Wrapper<Boolean> receivedInvalid = new Wrapper<>(false);

        dataHub.addListener(new DataHubListener<Object>() {
            @Override
            public void onDataFetchStarted() {

            }

            @Override
            public void onDataFetchFinished() {

            }

            @Override
            public void onResultReceived(DataHubResult<Object> result) {
                if (result.hasData()) {
                    if (validValue.equals(result.getData())) {
                        receivedValid.set(true);
                    } else if (invalidValue.equals(result.getData())) {
                        receivedInvalid.set(true);
                    }
                }
            }
        });

        // Assure that our initial state is correct
        Assert.assertFalse(receivedInvalid.get());
        Assert.assertFalse(receivedValid.get());

        // Do a fetch and make sure we receive both values
        dataHub.fetch();

        Assert.assertTrue(receivedInvalid.get());
        Assert.assertTrue(receivedValid.get());
    }
}
