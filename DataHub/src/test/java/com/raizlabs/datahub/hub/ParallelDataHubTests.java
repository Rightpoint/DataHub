package com.raizlabs.datahub.hub;

import com.raizlabs.datahub.access.AccessAssertions;
import com.raizlabs.datahub.access.AsynchronousDataAccess;
import com.raizlabs.datahub.access.DataAccessResult;
import com.raizlabs.datahub.access.SynchronousDataAccess;
import com.raizlabs.datahub.access.TemporaryMemoryAccess;
import com.raizlabs.datahub.hub.helpers.ImmediateResponseAsyncAccess;
import com.raizlabs.datahub.hub.helpers.WaitForLockAsyncAccess;
import com.raizlabs.datahub.hub.ordered.OrderedDataHub;
import com.raizlabs.datahub.utils.OneShotLock;
import com.raizlabs.datahub.utils.Wrapper;

import junit.framework.Assert;

import org.junit.Test;

public class ParallelDataHubTests extends BaseOrderedDataHubTests {

    @Override
    protected OrderedDataHub.Builder<Object> createNewBuilder() {
        return OrderedDataHub.Builder.newParallel();
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

        final DataHub<Object> dataHub =
                createNewBuilder()
                        .setSynchronousAccess(memoryAccess)
                        .addAsynchronousAccess(lateInvalidAccess)
                        .addAsynchronousAccess(validAccess)
                        .build();

        dataHub.addListener(new DataHubListener<Object>() {
            @Override
            public void onDataFetchStarted() {

            }

            @Override
            public void onDataFetchFinished() {
                fetchFinishedLock.unlock();
            }

            @Override
            public void onResultReceived(DataHubResult<Object> result) {
                if (result.hasData()) {
                    receivedData.set(result.getData());
                }
            }
        });

        // Test post-completion results don't show up

        // Make sure we have no data
        Assert.assertNull(receivedData.get());
        AccessAssertions.assertDataUnavailable(memoryAccess.get());

        // Start a fetch and wait for the response
        dataHub.fetch();
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

        final DataHub<Object> dataHub =
                createNewBuilder()
                        .setSynchronousAccess(memoryAccess)
                        .addAsynchronousAccess(lateInvalidAccess)
                        .addAsynchronousAccess(validAccess)
                        .addAsynchronousAccess(finalAccess)
                        .build();

        dataHub.addListener(new DataHubListener<Object>() {
            @Override
            public void onDataFetchStarted() {

            }

            @Override
            public void onDataFetchFinished() {
                fetchFinishedLock.unlock();
            }

            @Override
            public void onResultReceived(DataHubResult<Object> result) {
                if (result.hasData()) {
                    receivedData.set(result.getData());
                }
            }
        });

        // Test post-completion results don't show up

        // Make sure we have no data
        Assert.assertNull(receivedData.get());
        AccessAssertions.assertDataUnavailable(memoryAccess.get());

        // Start a fetch and wait for the response
        dataHub.fetch();

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
