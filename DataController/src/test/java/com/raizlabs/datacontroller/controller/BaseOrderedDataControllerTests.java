package com.raizlabs.datacontroller.controller;

import com.raizlabs.datacontroller.DCError;
import com.raizlabs.datacontroller.DataResult;
import com.raizlabs.datacontroller.ErrorInfo;
import com.raizlabs.datacontroller.access.AccessAssertions;
import com.raizlabs.datacontroller.access.AsynchronousDataAccess;
import com.raizlabs.datacontroller.access.DataAccessResult;
import com.raizlabs.datacontroller.access.KeyedMemoryDataAccess;
import com.raizlabs.datacontroller.access.MemoryDataManager;
import com.raizlabs.datacontroller.controller.helpers.ImmediateResponseAsyncAccess;
import com.raizlabs.datacontroller.controller.helpers.TrackedTemporaryMemoryAccess;
import com.raizlabs.datacontroller.controller.helpers.WaitForLockAsyncAccess;
import com.raizlabs.datacontroller.controller.ordered.OrderedDataController;
import com.raizlabs.datacontroller.utils.OneShotLock;
import com.raizlabs.datacontroller.utils.Wrapper;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseOrderedDataControllerTests {

    private static MemoryDataManager<String, Object> dataManager;

    private static MemoryDataManager<String, Object> getDataManager() {
        return dataManager;
    }

    @BeforeClass
    public static void setup() {
        dataManager = new MemoryDataManager<>();
    }

    @Before
    public void preTest() {
        dataManager.clear();
    }

    @After
    public void postTest() {
        dataManager.clear();
    }

    protected abstract OrderedDataController.Builder<Object> createNewBuilder();

    @Test
    public void testSynchronousAccess() {
        // Tests that modifying the data through the access functions
        final String key = "test";
        final int sourceId = 75;
        final Object value = new Object();
        final KeyedMemoryDataAccess<Object> dataAccess = new KeyedMemoryDataAccess<>(key, sourceId, getDataManager());
        final DataController<Object> dataController =
                createNewBuilder()
                        .setSynchronousAccess(dataAccess)
                        .build();

        // Ensure there initially is no result
        ControllerAssertions.assertDataUnavailable(dataController.get());

        // Import our value and make sure it comes back with the right value and the metadata of the access we created
        dataAccess.importData(value);

        ControllerResult<Object> result = dataController.get();
        Assert.assertEquals(sourceId, result.getSourceId());
        ControllerAssertions.assertDataEquals(value, result);

        // Ensure clearing functions properly
        dataAccess.clear();
        ControllerAssertions.assertDataUnavailable(dataController.get());
    }

    @Test
    public void testSynchronousController() {
        // Tests that modifying the data through the controller functions
        final String key = "test";
        final int sourceId = 75;
        final Object value = new Object();
        final KeyedMemoryDataAccess<Object> dataAccess = new KeyedMemoryDataAccess<>(key, sourceId, getDataManager());
        final DataController<Object> dataController =
                createNewBuilder()
                        .setSynchronousAccess(dataAccess)
                        .build();

        // Ensure there initially is no result
        ControllerAssertions.assertDataUnavailable(dataController.get());

        // Import our value and make sure it comes back with the right value and the metadata of the access we created
        dataController.importData(value);

        ControllerResult<Object> result = dataController.get();
        Assert.assertEquals(sourceId, result.getSourceId());
        ControllerAssertions.assertDataEquals(value, result);
    }

    @Test
    public void testSingleAsynchronous() {
        final String key = "test";
        final Object value = new Object();
        final KeyedMemoryDataAccess<Object> dataAccess = new KeyedMemoryDataAccess<>(key, 55, getDataManager());
        final OneShotLock allowResult = new OneShotLock();
        final OneShotLock completed = new OneShotLock();
        final AsynchronousDataAccess<Object> asyncDataAccess =
                new WaitForLockAsyncAccess<>(DataAccessResult.fromResult(value), allowResult, 66);

        final DataController<Object> dataController =
                createNewBuilder()
                        .setSynchronousAccess(dataAccess)
                        .addAsynchronousAccess(asyncDataAccess)
                        .build();


        // Ensure there initially is no result
        ControllerAssertions.assertDataUnavailable(dataController.get());

        final AtomicInteger listenerReceivedCount = new AtomicInteger(0);
        final Wrapper<Boolean> listenerFinished = new Wrapper<>(false);
        final Wrapper<Object> listenerResult = new Wrapper<>(null);

        dataController.addListener(new DataControllerListener<Object>() {
            @Override
            public void onDataFetchStarted() {

            }

            @Override
            public void onDataFetchFinished() {
                listenerFinished.set(true);
                completed.unlock();
            }

            @Override
            public void onDataReceived(DataResult<Object> dataResult) {
                listenerReceivedCount.incrementAndGet();
                listenerResult.set(dataResult.getData());
            }

            @Override
            public void onErrorReceived(ErrorInfo errorInfo) {

            }
        });

        Assert.assertFalse(dataController.isFetching());
        dataController.fetch();
        Assert.assertTrue(dataController.isFetching());
        allowResult.unlock();
        completed.waitUntilUnlocked();
        Assert.assertFalse(dataController.isFetching());

        Assert.assertTrue(listenerFinished.get());
        Assert.assertEquals(2, listenerReceivedCount.get());

        Assert.assertEquals(value, listenerResult.get());
        ControllerAssertions.assertDataEquals(value, dataController.get());
        // Ensure the data was backported
        AccessAssertions.assertDataEquals(value, dataAccess);
    }

    @Test
    public void testMultipleAsyncBackport() {
        final Object value = new Object();
        final OneShotLock finishedLock = new OneShotLock();
        final List<Wrapper<Boolean>> wasImported = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            wasImported.add(new Wrapper<>(false));
        }

        final AsynchronousDataAccess<Object> firstAccess = new ImmediateResponseAsyncAccess<Object>(DataAccessResult.fromUnavailable(), 99) {
            @Override
            public void importData(Object o) {
                super.importData(o);
                wasImported.get(0).set(true);
            }
        };

        final AsynchronousDataAccess<Object> secondAccess = new ImmediateResponseAsyncAccess<Object>(DataAccessResult.fromResult(value), 52) {
            @Override
            public void importData(Object o) {
                super.importData(o);
                wasImported.get(1).set(true);
            }
        };

        final AsynchronousDataAccess<Object> thirdAccess = new ImmediateResponseAsyncAccess<Object>(DataAccessResult.fromUnavailable(), 88) {
            @Override
            public void importData(Object o) {
                super.importData(o);
                wasImported.get(2).set(true);
            }
        };

        final DataController<Object> dataController =
                createNewBuilder()
                        .addAsynchronousAccess(firstAccess)
                        .addAsynchronousAccess(secondAccess)
                        .addAsynchronousAccess(thirdAccess)
                        .build();

        dataController.addListener(new DataControllerListener<Object>() {
            @Override
            public void onDataFetchStarted() {

            }

            @Override
            public void onDataFetchFinished() {
                finishedLock.unlock();
            }

            @Override
            public void onDataReceived(DataResult<Object> dataResult) {

            }

            @Override
            public void onErrorReceived(ErrorInfo errorInfo) {

            }
        });

        // Assure that we initially have nothing imported
        Assert.assertFalse(wasImported.get(0).get());
        Assert.assertFalse(wasImported.get(1).get());
        Assert.assertFalse(wasImported.get(2).get());

        // Start a fetch and wait for completion
        dataController.fetch();
        finishedLock.waitUntilUnlocked();

        // Verify that we imported into the first access, but not the second or third
        Assert.assertTrue(wasImported.get(0).get());
        Assert.assertFalse(wasImported.get(1).get());
        Assert.assertFalse(wasImported.get(2).get());
    }

    @Test
    public void testLimits() {
        final Object firstValue = new Object();
        final Object secondValue = new Object();

        final TrackedTemporaryMemoryAccess<Object> memoryAccess = new TrackedTemporaryMemoryAccess<>(99);
        final ImmediateResponseAsyncAccess<Object> firstAccess = new ImmediateResponseAsyncAccess<>(DataAccessResult.fromUnavailable(), 88);
        final ImmediateResponseAsyncAccess<Object> secondAccess = new ImmediateResponseAsyncAccess<>(DataAccessResult.fromResult(firstValue), 77);
        final ImmediateResponseAsyncAccess<Object> thirdAccess = new ImmediateResponseAsyncAccess<>(DataAccessResult.fromResult(secondValue), 66);

        final DataController<Object> dataController =
                createNewBuilder()
                        .setSynchronousAccess(memoryAccess)
                        .addAsynchronousAccess(firstAccess)
                        .addAsynchronousAccess(secondAccess)
                        .addAsynchronousAccess(thirdAccess)
                        .build();

        final Wrapper<ErrorInfo> receivedError = new Wrapper<>();

        dataController.addListener(new DataControllerListener<Object>() {
            @Override
            public void onDataFetchStarted() {

            }

            @Override
            public void onDataFetchFinished() {

            }

            @Override
            public void onDataReceived(DataResult<Object> dataResult) {

            }

            @Override
            public void onErrorReceived(ErrorInfo errorInfo) {
                receivedError.set(errorInfo);
            }
        });

        dataController.fetch(memoryAccess.getSourceId());
        Assert.assertTrue(memoryAccess.wasQueried());
        Assert.assertFalse(firstAccess.getCompletionLock().isUnlocked());
        Assert.assertFalse(secondAccess.getCompletionLock().isUnlocked());
        Assert.assertFalse(thirdAccess.getCompletionLock().isUnlocked());
        Assert.assertNull(receivedError.get());

        memoryAccess.clear();
        memoryAccess.reset();
        firstAccess.reset();
        secondAccess.reset();
        thirdAccess.reset();

        dataController.fetch(firstAccess.getSourceId());
        Assert.assertTrue(memoryAccess.wasQueried());
        Assert.assertTrue(firstAccess.getCompletionLock().isUnlocked());
        Assert.assertFalse(secondAccess.getCompletionLock().isUnlocked());
        Assert.assertFalse(thirdAccess.getCompletionLock().isUnlocked());
        Assert.assertNull(receivedError.get());

        memoryAccess.clear();
        memoryAccess.reset();
        firstAccess.reset();
        secondAccess.reset();
        thirdAccess.reset();

        dataController.fetch(secondAccess.getSourceId());
        Assert.assertTrue(memoryAccess.wasQueried());
        Assert.assertTrue(firstAccess.getCompletionLock().isUnlocked());
        Assert.assertTrue(secondAccess.getCompletionLock().isUnlocked());
        Assert.assertFalse(thirdAccess.getCompletionLock().isUnlocked());
        Assert.assertNull(receivedError.get());

        memoryAccess.clear();
        memoryAccess.reset();
        firstAccess.reset();
        secondAccess.reset();
        thirdAccess.reset();

        dataController.fetch(thirdAccess.getSourceId());
        Assert.assertTrue(memoryAccess.wasQueried());
        Assert.assertTrue(firstAccess.getCompletionLock().isUnlocked());
        Assert.assertTrue(secondAccess.getCompletionLock().isUnlocked());
        Assert.assertTrue(thirdAccess.getCompletionLock().isUnlocked());
        Assert.assertNull(receivedError.get());

        memoryAccess.clear();
        memoryAccess.reset();
        firstAccess.reset();
        secondAccess.reset();
        thirdAccess.reset();

        dataController.fetch(564);
        Assert.assertFalse(memoryAccess.wasQueried());
        Assert.assertFalse(firstAccess.getCompletionLock().isUnlocked());
        Assert.assertFalse(secondAccess.getCompletionLock().isUnlocked());
        Assert.assertFalse(thirdAccess.getCompletionLock().isUnlocked());
        Assert.assertNotNull(receivedError.get());
        Assert.assertEquals(DCError.Types.DATA_ACCESS_NOT_FOUND, receivedError.get().getError().getErrorType());
    }
}
