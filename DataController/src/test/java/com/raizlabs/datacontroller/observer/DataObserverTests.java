package com.raizlabs.datacontroller.observer;

import com.raizlabs.datacontroller.DataResult;
import com.raizlabs.datacontroller.ErrorInfo;
import com.raizlabs.datacontroller.access.DataAccessResult;
import com.raizlabs.datacontroller.access.TemporaryMemoryAccess;
import com.raizlabs.datacontroller.controller.DataController;
import com.raizlabs.datacontroller.controller.DataControllerResult;
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
            public void onResultReceived(DataControllerResult<Object> result) {
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

        final DataController<Object> controller =
                OrderedDataController.Builder.newParallel()
                        .setSynchronousAccess(new TemporaryMemoryAccess<>())
                        .addAsynchronousAccess(firstAccess)
                        .addAsynchronousAccess(secondAccess)
                        .build();

        controller.isClosed();

        final DataObserver<Object> observer = new DataObserver<>(controller, null);

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
            public void onResultReceived(DataControllerResult<Object> result) {
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
        DataController<Object> dataController = OrderedDataController.Builder.newParallel().build();
        DataObserver<Object> dataObserver = new DataObserver<>(dataController, null);

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
            public void onResultReceived(DataControllerResult<Object> result) {
                if (result.hasData()) {
                    dataReceived.set(true);
                }
            }
        };

        Assert.assertFalse(dataController.isClosed());

        // Shallow-close the data observer and make sure the data controller is unaffected
        dataObserver.close(false);

        Assert.assertFalse(dataController.isClosed());

        // Make sure our listener isn't triggered
        dataObserver.addListener(listener);

        Assert.assertFalse(fetchStartedCalled.get());
        Assert.assertFalse(dataReceived.get());

        dataObserver.fetch();

        Assert.assertFalse(fetchStartedCalled.get());
        Assert.assertFalse(dataReceived.get());


        // Do a full close and make sure the controller closes
        dataObserver = new DataObserver<>(dataController, null);
        dataObserver.close(true);

        Assert.assertTrue(dataController.isClosed());

    }
}
