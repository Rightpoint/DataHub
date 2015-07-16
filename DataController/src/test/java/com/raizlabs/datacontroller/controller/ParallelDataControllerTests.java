package com.raizlabs.datacontroller.controller;

import com.raizlabs.datacontroller.DataAccessResult;
import com.raizlabs.datacontroller.DataResult;
import com.raizlabs.datacontroller.ErrorInfo;
import com.raizlabs.datacontroller.access.AccessAssertions;
import com.raizlabs.datacontroller.access.AsynchronousDataAccess;
import com.raizlabs.datacontroller.access.SynchronousDataAccess;
import com.raizlabs.datacontroller.access.TemporaryMemoryAccess;
import com.raizlabs.datacontroller.controller.helpers.ImmediateResponseAsyncAccess;
import com.raizlabs.datacontroller.controller.helpers.WaitForLockAsyncAccess;
import com.raizlabs.datacontroller.controller.ordered.OrderedDataController;
import com.raizlabs.datacontroller.utils.OneShotLock;
import com.raizlabs.datacontroller.utils.Wrapper;

import junit.framework.Assert;

import org.junit.Test;

public class ParallelDataControllerTests extends BaseOrderedDataControllerTests {

    @Override
    protected OrderedDataController.Builder<Object> createNewBuilder() {
        return OrderedDataController.Builder.newParallel();
    }

    @Test
    /**
     * Tests that a result sent by a previous access is ignored after the final access returns
     */
    public void testIgnorePostCompletionResponse() {
        final Object validValue = new Object();
        final Object invalidValue = new Object();

        final DataAccessResult<Object> validResult = DataAccessResult.fromResult(validValue);
        final DataAccessResult<Object> invalidResult = DataAccessResult.fromResult(invalidValue);

        final OneShotLock allowInvalidResponseLock = new OneShotLock();
        final OneShotLock fetchFinishedLock = new OneShotLock();
        final Wrapper<Object> receivedData = new Wrapper<>();

        final SynchronousDataAccess<Object> memoryAccess = new TemporaryMemoryAccess<>();
        final WaitForLockAsyncAccess<Object> lateInvalidAccess =
                new WaitForLockAsyncAccess<>(invalidResult, allowInvalidResponseLock, 88);
        final AsynchronousDataAccess<Object> validAccess =
                new ImmediateResponseAsyncAccess<>(validResult, 55);

        final DataController<Object> dataController =
                createNewBuilder()
                        .setSynchronousAccess(memoryAccess)
                        .addAsynchronousAccess(lateInvalidAccess)
                        .addAsynchronousAccess(validAccess)
                        .build();

        dataController.addListener(new DataControllerListener<Object>() {
            @Override
            public void onDataFetchStarted() {

            }

            @Override
            public void onDataFetchFinished() {
                fetchFinishedLock.unlock();
            }

            @Override
            public void onDataReceived(DataResult<Object> dataResult) {
                receivedData.set(dataResult.getData());
            }

            @Override
            public void onErrorReceived(ErrorInfo errorInfo) {

            }
        });

        // Test post-completion results don't show up

        // Make sure we have no data
        Assert.assertNull(receivedData.get());
        AccessAssertions.assertDataUnavailable(memoryAccess.get());

        // Start a fetch and wait for the response
        dataController.fetch();
        fetchFinishedLock.waitUntilUnlocked();

        // Make sure the data went all the way through
        Assert.assertEquals(validValue, receivedData.get());
        Assert.assertEquals(validValue, memoryAccess.get().getData());

        // Reset...
        receivedData.set(null);

        // Allow the "invalid" data through
        allowInvalidResponseLock.unlock();
        lateInvalidAccess.getCompletionLock().waitUntilUnlocked();

        // Make sure we still have the old response and no updates
        Assert.assertNull(receivedData.get());
        Assert.assertEquals(validValue, memoryAccess.get().getData());
    }

    @Test
    /**
     * Tests that a result sent by a previous access is ignored, even before the final access returns
     */
    public void testIgnorePreCompletionResponse() {
        final Object validValue = new Object();
        final Object invalidValue = new Object();

        final DataAccessResult<Object> validResult = DataAccessResult.fromResult(validValue);
        final DataAccessResult<Object> invalidResult = DataAccessResult.fromResult(invalidValue);
        final DataAccessResult<Object> finalResult = DataAccessResult.fromUnavailable();

        final OneShotLock allowInvalidResponseLock = new OneShotLock();
        final OneShotLock allowFinalResponseLock = new OneShotLock();
        final OneShotLock fetchFinishedLock = new OneShotLock();
        final Wrapper<Object> receivedData = new Wrapper<>();

        final SynchronousDataAccess<Object> memoryAccess = new TemporaryMemoryAccess<>();
        final WaitForLockAsyncAccess<Object> lateInvalidAccess =
                new WaitForLockAsyncAccess<>(invalidResult, allowInvalidResponseLock, 88);
        final AsynchronousDataAccess<Object> validAccess =
                new ImmediateResponseAsyncAccess<>(validResult, 55);
        final WaitForLockAsyncAccess<Object> finalAccess =
                new WaitForLockAsyncAccess<>(finalResult, allowFinalResponseLock, 77);

        final DataController<Object> dataController =
                createNewBuilder()
                        .setSynchronousAccess(memoryAccess)
                        .addAsynchronousAccess(lateInvalidAccess)
                        .addAsynchronousAccess(validAccess)
                        .addAsynchronousAccess(finalAccess)
                        .build();

        dataController.addListener(new DataControllerListener<Object>() {
            @Override
            public void onDataFetchStarted() {

            }

            @Override
            public void onDataFetchFinished() {
                fetchFinishedLock.unlock();
            }

            @Override
            public void onDataReceived(DataResult<Object> dataResult) {
                receivedData.set(dataResult.getData());
            }

            @Override
            public void onErrorReceived(ErrorInfo errorInfo) {

            }
        });

        // Test post-completion results don't show up

        // Make sure we have no data
        Assert.assertNull(receivedData.get());
        AccessAssertions.assertDataUnavailable(memoryAccess.get());

        // Start a fetch and wait for the response
        dataController.fetch();

        // Make sure the data went all the way through
        Assert.assertEquals(validValue, receivedData.get());
        Assert.assertEquals(validValue, memoryAccess.get().getData());

        // Reset...
        receivedData.set(null);

        // Allow the "invalid" data through
        allowInvalidResponseLock.unlock();
        lateInvalidAccess.getCompletionLock().waitUntilUnlocked();

        // Make sure we still have the old response and no updates
        Assert.assertNull(receivedData.get());
        Assert.assertEquals(validValue, memoryAccess.get().getData());

        // Reset...
        receivedData.set(null);

        // Let the final data through
        allowFinalResponseLock.unlock();
        finalAccess.getCompletionLock().waitUntilUnlocked();

        // Make sure we still have the old response and no updates
        Assert.assertNull(receivedData.get());
        Assert.assertEquals(validValue, memoryAccess.get().getData());
    }
}
