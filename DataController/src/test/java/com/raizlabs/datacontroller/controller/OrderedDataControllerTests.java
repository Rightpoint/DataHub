package com.raizlabs.datacontroller.controller;

import com.raizlabs.datacontroller.DataAccessResult;
import com.raizlabs.datacontroller.ErrorInfo;
import com.raizlabs.datacontroller.DataResult;
import com.raizlabs.datacontroller.access.AccessAssertions;
import com.raizlabs.datacontroller.access.AsynchronousDataAccess;
import com.raizlabs.datacontroller.access.KeyedMemoryDataAccess;
import com.raizlabs.datacontroller.access.MemoryDataManager;
import com.raizlabs.datacontroller.utils.OneShotLock;
import com.raizlabs.datacontroller.utils.Wrapper;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderedDataControllerTests {

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

    @Test
    public void testSynchronousAccess() {
        // Tests that modifying the data through the access functions
        final String key = "test";
        final int sourceId = 75;
        final Object value = new Object();
        final KeyedMemoryDataAccess<Object> dataAccess = new KeyedMemoryDataAccess<>(key, sourceId, getDataManager());
        final DataController<Object> dataController = new OrderedDataController<>(dataAccess, null);

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
        final DataController<Object> dataController = new OrderedDataController<>(dataAccess, null);

        // Ensure there initially is no result
        ControllerAssertions.assertDataUnavailable(dataController.get());

        // Import our value and make sure it comes back with the right value and the metadata of the access we created
        dataController.importData(value);

        ControllerResult<Object> result = dataController.get();
        Assert.assertEquals(sourceId, result.getSourceId());
        ControllerAssertions.assertDataEquals(value, result);
    }

    @Test
    public void testAsynchronous() {
        final String key = "test";
        final Object value = new Object();
        final KeyedMemoryDataAccess<Object> dataAccess = new KeyedMemoryDataAccess<>(key, 55, getDataManager());
        final OneShotLock allowResult = new OneShotLock();
        final OneShotLock completed = new OneShotLock();
        final AsynchronousDataAccess<Object> asyncDataAccess = new AsynchronousDataAccess<Object>() {
            @Override
            public void get(final Callback<Object> callback) {
                final AsynchronousDataAccess<Object> access = this;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        allowResult.waitUntilUnlocked();
                        callback.onResult(DataAccessResult.fromResult(value), access);
                    }
                }).start();
            }

            @Override
            public void importData(Object o) {

            }

            @Override
            public void close() {

            }

            @Override
            public int getSourceId() {
                return 66;
            }
        };
        final DataController<Object> dataController = new OrderedDataController<>(dataAccess, Arrays.asList(asyncDataAccess));

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
        Assert.assertEquals(1, listenerReceivedCount.get());

        Assert.assertEquals(value, listenerResult.get());
        ControllerAssertions.assertDataEquals(value, dataController.get());
        // Ensure the data was backported
        AccessAssertions.assertDataEquals(value, dataAccess.get());
    }
}
