package com.raizlabs.datahub.hub;

import com.raizlabs.datahub.DataHubError;
import com.raizlabs.datahub.DataHubErrorInfo;
import com.raizlabs.datahub.access.AccessAssertions;
import com.raizlabs.datahub.access.AsyncDataAccess;
import com.raizlabs.datahub.access.DataAccessResult;
import com.raizlabs.datahub.access.KeyedMemoryDataAccess;
import com.raizlabs.datahub.access.MemoryKeyedDataManager;
import com.raizlabs.datahub.hub.helpers.ImmediateResponseAsyncAccess;
import com.raizlabs.datahub.hub.helpers.TrackedTemporaryMemoryAccess;
import com.raizlabs.datahub.hub.helpers.WaitForLockAsyncAccess;
import com.raizlabs.datahub.hub.ordered.OrderedDataHub;
import com.raizlabs.datahub.utils.OneShotLock;
import com.raizlabs.datahub.utils.Wrapper;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseOrderedDataHubTests {

    private static MemoryKeyedDataManager<String, Object> dataManager;

    private static MemoryKeyedDataManager<String, Object> getDataManager() {
        return dataManager;
    }

    @BeforeClass
    public static void setup() {
        dataManager = new MemoryKeyedDataManager<>();
    }

    @Before
    public void preTest() {
        dataManager.clear();
    }

    @After
    public void postTest() {
        dataManager.clear();
    }

    protected abstract OrderedDataHub.Builder<Object> createNewBuilder();

    @Test
    public void testSynchronousAccess() {
        // Tests that modifying the data through the access functions
        final String key = "test";
        final int typeId = 75;
        final Object value = new Object();
        final KeyedMemoryDataAccess<Object> dataAccess = new KeyedMemoryDataAccess<>(key, getDataManager(), typeId);
        final DataHub<Object> dataHub =
                createNewBuilder()
                        .setSynchronousAccess(dataAccess)
                        .build();

        // Ensure there initially is no result
        HubAssertions.assertDataUnavailable(dataHub.getCurrent());

        // Import our value and make sure it comes back with the right value and the metadata of the access we created
        dataAccess.importData(value);

        DataHubResult<Object> result = dataHub.getCurrent();
        Assert.assertEquals(typeId, result.getAccessTypeId());
        HubAssertions.assertDataEquals(value, result);

        // Ensure clearing functions properly
        dataAccess.clear();
        HubAssertions.assertDataUnavailable(dataHub.getCurrent());
    }

    @Test
    public void testSynchronousHub() {
        // Tests that modifying the data through the hub functions
        final String key = "test";
        final int typeId = 75;
        final Object value = new Object();
        final KeyedMemoryDataAccess<Object> dataAccess = new KeyedMemoryDataAccess<>(key, getDataManager(), typeId);
        final DataHub<Object> dataHub =
                createNewBuilder()
                        .setSynchronousAccess(dataAccess)
                        .build();

        // Ensure there initially is no result
        HubAssertions.assertDataUnavailable(dataHub.getCurrent());

        // Import our value and make sure it comes back with the right value and the metadata of the access we created
        dataHub.importData(value);

        DataHubResult<Object> result = dataHub.getCurrent();
        Assert.assertEquals(typeId, result.getAccessTypeId());
        HubAssertions.assertDataEquals(value, result);
    }

    @Test
    public void testSingleAsynchronous() {
        final String key = "test";
        final Object value = new Object();
        final KeyedMemoryDataAccess<Object> dataAccess = new KeyedMemoryDataAccess<>(key, getDataManager(), 55);
        final OneShotLock allowResult = new OneShotLock();
        final OneShotLock completed = new OneShotLock();
        final AsyncDataAccess<Object> asyncDataAccess =
                new WaitForLockAsyncAccess<>(DataAccessResult.fromResult(value), allowResult, 66);

        final DataHub<Object> dataHub =
                createNewBuilder()
                        .setSynchronousAccess(dataAccess)
                        .addAsynchronousAccess(asyncDataAccess)
                        .build();


        // Ensure there initially is no result
        HubAssertions.assertDataUnavailable(dataHub.getCurrent());

        final AtomicInteger listenerReceivedCount = new AtomicInteger(0);
        final Wrapper<Boolean> listenerFinished = new Wrapper<>(false);
        final Wrapper<Object> listenerResult = new Wrapper<>(null);

        dataHub.addListener(new DataHubListener<Object>() {
            @Override
            public void onDataFetchStarted() {

            }

            @Override
            public void onDataFetchFinished() {
                listenerFinished.set(true);
                completed.unlock();
            }

            @Override
            public void onResultReceived(DataHubResult<Object> result) {
                if (!result.hasError()) {
                    listenerReceivedCount.incrementAndGet();
                    listenerResult.set(result.getData());
                }
            }
        });

        Assert.assertFalse(dataHub.isFetching());
        dataHub.fetch();
        Assert.assertTrue(dataHub.isFetching());
        allowResult.unlock();
        completed.waitUntilUnlocked();
        Assert.assertFalse(dataHub.isFetching());

        Assert.assertTrue(listenerFinished.get());
        Assert.assertEquals(2, listenerReceivedCount.get());

        Assert.assertEquals(value, listenerResult.get());
        HubAssertions.assertDataEquals(value, dataHub.getCurrent());
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

        final AsyncDataAccess<Object> firstAccess = new ImmediateResponseAsyncAccess<Object>(DataAccessResult.fromUnavailable(), 99) {
            @Override
            public void importData(Object o) {
                super.importData(o);
                wasImported.get(0).set(true);
            }
        };

        final AsyncDataAccess<Object> secondAccess = new ImmediateResponseAsyncAccess<Object>(DataAccessResult.fromResult(value), 52) {
            @Override
            public void importData(Object o) {
                super.importData(o);
                wasImported.get(1).set(true);
            }
        };

        final AsyncDataAccess<Object> thirdAccess = new ImmediateResponseAsyncAccess<Object>(DataAccessResult.fromUnavailable(), 88) {
            @Override
            public void importData(Object o) {
                super.importData(o);
                wasImported.get(2).set(true);
            }
        };

        final DataHub<Object> dataHub =
                createNewBuilder()
                        .addAsynchronousAccess(firstAccess)
                        .addAsynchronousAccess(secondAccess)
                        .addAsynchronousAccess(thirdAccess)
                        .build();

        dataHub.addListener(new DataHubListener<Object>() {
            @Override
            public void onDataFetchStarted() {

            }

            @Override
            public void onDataFetchFinished() {
                finishedLock.unlock();
            }

            @Override
            public void onResultReceived(DataHubResult<Object> result) {

            }
        });

        // Assure that we initially have nothing imported
        Assert.assertFalse(wasImported.get(0).get());
        Assert.assertFalse(wasImported.get(1).get());
        Assert.assertFalse(wasImported.get(2).get());

        // Start a fetch and wait for completion
        dataHub.fetch();
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

        final DataHub<Object> dataHub =
                createNewBuilder()
                        .setSynchronousAccess(memoryAccess)
                        .addAsynchronousAccess(firstAccess)
                        .addAsynchronousAccess(secondAccess)
                        .addAsynchronousAccess(thirdAccess)
                        .build();

        final Wrapper<DataHubErrorInfo> receivedError = new Wrapper<>();

        dataHub.addListener(new DataHubListener<Object>() {
            @Override
            public void onDataFetchStarted() {

            }

            @Override
            public void onDataFetchFinished() {

            }

            @Override
            public void onResultReceived(DataHubResult<Object> result) {
                if (result.hasError()) {
                    receivedError.set(result);
                }
            }
        });

        dataHub.fetch(memoryAccess.getTypeId());
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

        dataHub.fetch(firstAccess.getTypeId());
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

        dataHub.fetch(secondAccess.getTypeId());
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

        dataHub.fetch(thirdAccess.getTypeId());
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

        dataHub.fetch(564);
        Assert.assertFalse(memoryAccess.wasQueried());
        Assert.assertFalse(firstAccess.getCompletionLock().isUnlocked());
        Assert.assertFalse(secondAccess.getCompletionLock().isUnlocked());
        Assert.assertFalse(thirdAccess.getCompletionLock().isUnlocked());
        Assert.assertNotNull(receivedError.get());
        Assert.assertEquals(DataHubError.Types.DATA_ACCESS_NOT_FOUND, receivedError.get().getError().getErrorType());
    }
}
