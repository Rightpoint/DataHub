package com.raizlabs.datacontroller.observer;

import com.raizlabs.datacontroller.DataResult;
import com.raizlabs.datacontroller.ErrorInfo;
import com.raizlabs.datacontroller.access.DataAccessResult;
import com.raizlabs.datacontroller.access.TemporaryMemoryAccess;
import com.raizlabs.datacontroller.controller.DataController;
import com.raizlabs.datacontroller.controller.helpers.ImmediateResponseAsyncAccess;
import com.raizlabs.datacontroller.controller.helpers.WaitForLockAsyncAccess;
import com.raizlabs.datacontroller.controller.ordered.FetchStrategies;
import com.raizlabs.datacontroller.controller.ordered.OrderedDataController;
import com.raizlabs.datacontroller.utils.OneShotLock;
import com.raizlabs.datacontroller.utils.Wrapper;

import junit.framework.Assert;

import org.junit.Test;

public class DataObserverTests {

    @Test
    public void testFullCycle() {
        final DataAccessResult<Object> firstResult = DataAccessResult.fromUnavailable();
        final DataAccessResult<Object> secondResult = DataAccessResult.fromResult(new Object());

        final DataController<Object> controller =
                OrderedDataController.Builder.newSerial(FetchStrategies.Serial.Validators.newValidOnly())
                        .setSynchronousAccess(new TemporaryMemoryAccess<>())
                        .addAsynchronousAccess(new ImmediateResponseAsyncAccess<>(firstResult, 10))
                        .addAsynchronousAccess(new ImmediateResponseAsyncAccess<>(secondResult, 20))
                        .build();

        final DataObserver<Object> observer = new DataObserver<>(controller, null);

        final Wrapper<Boolean> fetchStartedCalled = new Wrapper<>(false);
        final Wrapper<Boolean> dataReceivedCalled = new Wrapper<>(false);
        final Wrapper<Boolean> errorReceivedCalled = new Wrapper<>(false);
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
            public void onDataReceived(DataResult<Object> dataResult) {
                dataReceivedCalled.set(true);
            }

            @Override
            public void onErrorReceived(ErrorInfo errorInfo) {
                errorReceivedCalled.set(true);
            }
        });

        Assert.assertFalse(fetchStartedCalled.get());
        Assert.assertFalse(dataReceivedCalled.get());
        Assert.assertFalse(fetchFinishedCalled.get());
        Assert.assertFalse(errorReceivedCalled.get());

        observer.fetch();
        fetchFinishedLock.waitUntilUnlocked();

        Assert.assertTrue(fetchStartedCalled.get());
        Assert.assertTrue(dataReceivedCalled.get());
        Assert.assertTrue(fetchFinishedCalled.get());
        Assert.assertFalse(errorReceivedCalled.get());
    }

    @Test
    public void testListenerInitialization() {
        final DataAccessResult<Object> firstResult = DataAccessResult.fromResult(new Object());
        final DataAccessResult<Object> secondResult = DataAccessResult.fromResult(new Object());

        final OneShotLock finalResultLock = new OneShotLock();

        final ImmediateResponseAsyncAccess<Object> firstAccess = new ImmediateResponseAsyncAccess<>(firstResult, 9);
        final WaitForLockAsyncAccess<Object> secondAccess = new WaitForLockAsyncAccess<>(secondResult, finalResultLock, 20);

        final DataController<Object> controller =
                OrderedDataController.Builder.newParallel()
                        .setSynchronousAccess(new TemporaryMemoryAccess<>())
                        .addAsynchronousAccess(firstAccess)
                        .addAsynchronousAccess(secondAccess)
                        .build();

        controller.isClosed();

        final DataObserver<Object> observer = new DataObserver<>(controller, null);

        final Wrapper<Boolean> fetchStartedCalled = new Wrapper<>(false);
        final Wrapper<Boolean> dataReceivedCalled = new Wrapper<>(false);

        DataObserverListener<Object> listener = new DataObserverListener<Object>() {
            @Override
            public void onDataFetchStarted() {
                fetchStartedCalled.set(true);
            }

            @Override
            public void onDataFetchFinished() {

            }

            @Override
            public void onDataReceived(DataResult<Object> dataResult) {
                dataReceivedCalled.set(true);
            }

            @Override
            public void onErrorReceived(ErrorInfo errorInfo) {

            }
        };

        observer.fetch();
        firstAccess.getCompletionLock().waitUntilUnlocked();

        Assert.assertFalse(fetchStartedCalled.get());
        Assert.assertFalse(dataReceivedCalled.get());

        observer.addListener(listener);

        Assert.assertTrue(fetchStartedCalled.get());
        Assert.assertTrue(dataReceivedCalled.get());
    }

    @Test
    public void testClose() {
        DataController<Object> dataController = OrderedDataController.Builder.newParallel().build();
        DataObserver<Object> dataObserver = new DataObserver<>(dataController, null);

        final Wrapper<Boolean> fetchStartedCalled = new Wrapper<>(false);
        final Wrapper<Boolean> dataReceivedCalled = new Wrapper<>(false);
        final DataObserverListener<Object> listener = new DataObserverListener<Object>() {
            @Override
            public void onDataFetchStarted() {
                fetchStartedCalled.set(true);
            }

            @Override
            public void onDataFetchFinished() {

            }

            @Override
            public void onDataReceived(DataResult<Object> dataResult) {
                dataReceivedCalled.set(true);
            }

            @Override
            public void onErrorReceived(ErrorInfo errorInfo) {

            }
        };

        Assert.assertFalse(dataController.isClosed());

        // Shallow-close the data observer and make sure the data controller is unaffected
        dataObserver.close(false);

        Assert.assertFalse(dataController.isClosed());

        // Make sure our listener isn't triggered
        dataObserver.addListener(listener);

        Assert.assertFalse(fetchStartedCalled.get());
        Assert.assertFalse(dataReceivedCalled.get());

        dataObserver.fetch();

        Assert.assertFalse(fetchStartedCalled.get());
        Assert.assertFalse(dataReceivedCalled.get());


        // Do a full close and make sure the controller closes
        dataObserver = new DataObserver<>(dataController, null);
        dataObserver.close(true);

        Assert.assertTrue(dataController.isClosed());

    }
}
