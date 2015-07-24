package com.raizlabs.datahub.observer;

import com.raizlabs.datahub.access.DataAccessResult;
import com.raizlabs.datahub.access.TemporaryMemoryAccess;
import com.raizlabs.datahub.hub.DataHub;
import com.raizlabs.datahub.hub.DataHubResult;
import com.raizlabs.datahub.hub.helpers.ImmediateResponseAsyncAccess;
import com.raizlabs.datahub.hub.helpers.WaitForLockAsyncAccess;
import com.raizlabs.datahub.hub.ordered.FetchStrategies;
import com.raizlabs.datahub.hub.ordered.OrderedDataHub;
import com.raizlabs.datahub.utils.OneShotLock;
import com.raizlabs.datahub.utils.Wrapper;

import junit.framework.Assert;

import org.junit.Test;

public class DataObserverTests {

    @Test
    public void testFullCycle() {
        final DataAccessResult<Object> firstResult = DataAccessResult.fromUnavailable();
        final DataAccessResult<Object> secondResult = DataAccessResult.fromResult(new Object());

        final DataHub<Object> hub =
                OrderedDataHub.Builder.newSerial(FetchStrategies.Serial.Validators.newValidOnly())
                        .setSynchronousAccess(new TemporaryMemoryAccess<>())
                        .addAsynchronousAccess(new ImmediateResponseAsyncAccess<>(firstResult, 10))
                        .addAsynchronousAccess(new ImmediateResponseAsyncAccess<>(secondResult, 20))
                        .build();

        final DataObserver<Object> observer = new DataObserver<>(hub, null);

        final Wrapper<Boolean> fetchStartedCalled = new Wrapper<>(false);
        final Wrapper<Boolean> dataReceived = new Wrapper<>(false);
        final Wrapper<Boolean> errorReceived = new Wrapper<>(false);
        final Wrapper<Boolean> fetchFinishedCalled = new Wrapper<>(false);

        final OneShotLock fetchFinishedLock = new OneShotLock();

        observer.addListener(new DataObserverListener<Object>() {
            @Override
            public void onDataFetchStarted() {
                fetchStartedCalled.set(true);
            }

            @Override
            public void onDataFetchFinished() {
                fetchFinishedCalled.set(true);
                fetchFinishedLock.unlock();
            }

            @Override
            public void onResultReceived(DataHubResult<Object> result) {
                if (result.hasError()) {
                    errorReceived.set(true);
                } else if (result.hasData()){
                    dataReceived.set(true);
                }
            }
        });

        Assert.assertFalse(fetchStartedCalled.get());
        Assert.assertFalse(dataReceived.get());
        Assert.assertFalse(fetchFinishedCalled.get());
        Assert.assertFalse(errorReceived.get());

        observer.fetch();
        fetchFinishedLock.waitUntilUnlocked();

        Assert.assertTrue(fetchStartedCalled.get());
        Assert.assertTrue(dataReceived.get());
        Assert.assertTrue(fetchFinishedCalled.get());
        Assert.assertFalse(errorReceived.get());
    }

    @Test
    public void testListenerInitialization() {
        final DataAccessResult<Object> firstResult = DataAccessResult.fromResult(new Object());
        final DataAccessResult<Object> secondResult = DataAccessResult.fromResult(new Object());

        final OneShotLock finalResultLock = new OneShotLock();

        final ImmediateResponseAsyncAccess<Object> firstAccess = new ImmediateResponseAsyncAccess<>(firstResult, 9);
        final WaitForLockAsyncAccess<Object> secondAccess = new WaitForLockAsyncAccess<>(secondResult, finalResultLock, 20);

        final DataHub<Object> hub =
                OrderedDataHub.Builder.newParallel()
                        .setSynchronousAccess(new TemporaryMemoryAccess<>())
                        .addAsynchronousAccess(firstAccess)
                        .addAsynchronousAccess(secondAccess)
                        .build();

        hub.isClosed();

        final DataObserver<Object> observer = new DataObserver<>(hub, null);

        final Wrapper<Boolean> fetchStartedCalled = new Wrapper<>(false);
        final Wrapper<Boolean> dataReceived = new Wrapper<>(false);

        DataObserverListener<Object> listener = new DataObserverListener<Object>() {
            @Override
            public void onDataFetchStarted() {
                fetchStartedCalled.set(true);
            }

            @Override
            public void onDataFetchFinished() {

            }

            @Override
            public void onResultReceived(DataHubResult<Object> result) {
                if (result.hasData()) {
                    dataReceived.set(true);
                }
            }
        };

        observer.fetch();
        firstAccess.getCompletionLock().waitUntilUnlocked();

        Assert.assertFalse(fetchStartedCalled.get());
        Assert.assertFalse(dataReceived.get());

        observer.addListener(listener);

        Assert.assertTrue(fetchStartedCalled.get());
        Assert.assertTrue(dataReceived.get());
    }

    @Test
    public void testClose() {
        DataHub<Object> dataHub = OrderedDataHub.Builder.newParallel().build();
        DataObserver<Object> dataObserver = new DataObserver<>(dataHub, null);

        final Wrapper<Boolean> fetchStartedCalled = new Wrapper<>(false);
        final Wrapper<Boolean> dataReceived = new Wrapper<>(false);
        final DataObserverListener<Object> listener = new DataObserverListener<Object>() {
            @Override
            public void onDataFetchStarted() {
                fetchStartedCalled.set(true);
            }

            @Override
            public void onDataFetchFinished() {

            }

            @Override
            public void onResultReceived(DataHubResult<Object> result) {
                if (result.hasData()) {
                    dataReceived.set(true);
                }
            }
        };

        Assert.assertFalse(dataHub.isClosed());

        // Shallow-close the data observer and make sure the data hub is unaffected
        dataObserver.close(false);

        Assert.assertFalse(dataHub.isClosed());

        // Make sure our listener isn't triggered
        dataObserver.addListener(listener);

        Assert.assertFalse(fetchStartedCalled.get());
        Assert.assertFalse(dataReceived.get());

        dataObserver.fetch();

        Assert.assertFalse(fetchStartedCalled.get());
        Assert.assertFalse(dataReceived.get());


        // Do a full close and make sure the hub closes
        dataObserver = new DataObserver<>(dataHub, null);
        dataObserver.close(true);

        Assert.assertTrue(dataHub.isClosed());

    }
}