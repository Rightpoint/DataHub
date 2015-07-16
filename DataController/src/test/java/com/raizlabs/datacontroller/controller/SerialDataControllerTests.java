package com.raizlabs.datacontroller.controller;

import com.raizlabs.datacontroller.DataAccessResult;
import com.raizlabs.datacontroller.DataResult;
import com.raizlabs.datacontroller.ErrorInfo;
import com.raizlabs.datacontroller.access.AccessAssertions;
import com.raizlabs.datacontroller.access.AsynchronousDataAccess;
import com.raizlabs.datacontroller.access.TemporaryMemoryAccess;
import com.raizlabs.datacontroller.controller.helpers.ImmediateResponseAsyncAccess;
import com.raizlabs.datacontroller.controller.ordered.FetchStrategies;
import com.raizlabs.datacontroller.controller.ordered.OrderedDataController;
import com.raizlabs.datacontroller.utils.Wrapper;

import junit.framework.Assert;

import org.junit.Test;

public class SerialDataControllerTests extends BaseOrderedDataControllerTests {

    @Override
    protected OrderedDataController.Builder<Object> createNewBuilder() {
        return OrderedDataController.Builder.newSerial(FetchStrategies.Serial.Validators.newValidOnly());
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
            public boolean isReturnable(DataAccessResult<Object> result, AsynchronousDataAccess<Object> access) {
                return true;
            }

            @Override
            public boolean isFinal(DataAccessResult<Object> result, AsynchronousDataAccess<Object> access) {
                return validValue.equals(result.getData());
            }
        };

        DataController<Object> dataController =
                OrderedDataController.Builder.newSerial(validator)
                        .setSynchronousAccess(memoryAccess)
                        .addAsynchronousAccess(validAccess)
                        .addAsynchronousAccess(invalidAccess)
                        .build();

        // Assure that our initial state is empty
        AccessAssertions.assertDataUnavailable(memoryAccess);

        // Do a fetch and make sure we only have the valid data
        // Ensuring that we "stop" when we see a final result
        dataController.fetch();
        AccessAssertions.assertDataEquals(validValue, memoryAccess);

        // Reset...
        memoryAccess.clear();

        // Assure that our initial state is empty
        AccessAssertions.assertDataUnavailable(memoryAccess);

        // Make sure we keep going UNTIL we see a final result
        dataController =
                OrderedDataController.Builder.newSerial(validator)
                        .setSynchronousAccess(memoryAccess)
                        .addAsynchronousAccess(invalidAccess)
                        .addAsynchronousAccess(validAccess)
                        .build();

        dataController.fetch();
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

        final Wrapper<Boolean> allowInvalid = new Wrapper<>(false);

        final FetchStrategies.Serial.DataValidator<Object> validator = new FetchStrategies.Serial.DataValidator<Object>() {
            @Override
            public boolean isReturnable(DataAccessResult<Object> result, AsynchronousDataAccess<Object> access) {
                return allowInvalid.get() || validValue.equals(result.getData());
            }

            @Override
            public boolean isFinal(DataAccessResult<Object> result, AsynchronousDataAccess<Object> access) {
                return validValue.equals(result.getData());
            }
        };

        final DataController<Object> dataController =
                OrderedDataController.Builder.newSerial(validator)
                        .setSynchronousAccess(memoryAccess)
                        .addAsynchronousAccess(invalidAccess)
                        .addAsynchronousAccess(validAccess)
                        .build();

        final Wrapper<Boolean> receivedValid = new Wrapper<>(false);
        final Wrapper<Boolean> receivedInvalid = new Wrapper<>(false);

        dataController.addListener(new DataControllerListener<Object>() {
            @Override
            public void onDataFetchStarted() {

            }

            @Override
            public void onDataFetchFinished() {

            }

            @Override
            public void onDataReceived(DataResult<Object> dataResult) {
                if (validValue.equals(dataResult.getData())) {
                    receivedValid.set(true);
                } else if (invalidValue.equals(dataResult.getData())) {
                    receivedInvalid.set(true);
                }
            }

            @Override
            public void onErrorReceived(ErrorInfo errorInfo) {

            }
        });

        // Assure that our initial state is correct
        Assert.assertFalse(receivedInvalid.get());
        Assert.assertFalse(receivedValid.get());

        // Do a fetch and make sure we only receive the valid data
        dataController.fetch();

        Assert.assertFalse(receivedInvalid.get());
        Assert.assertTrue(receivedValid.get());

        // Reset...
        receivedInvalid.set(false);
        receivedValid.set(false);

        // Allow invalids through and ensure we receive both responses
        allowInvalid.set(true);
        dataController.fetch();

        Assert.assertTrue(receivedInvalid.get());
        Assert.assertTrue(receivedValid.get());
    }
}
