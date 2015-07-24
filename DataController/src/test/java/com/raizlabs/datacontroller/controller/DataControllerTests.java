package com.raizlabs.datacontroller.controller;

import com.raizlabs.datacontroller.DCError;
import com.raizlabs.datacontroller.access.DataAccessResult;
import com.raizlabs.datacontroller.ErrorInfo;
import com.raizlabs.datacontroller.DataResult;
import com.raizlabs.datacontroller.utils.Wrapper;

import junit.framework.Assert;

import org.junit.Test;

public class DataControllerTests {

    @Test
    public void testEventMethods() {
        final Object value = new Object();
        final Wrapper<Boolean> getCalled = new Wrapper<>(false);
        final Wrapper<Boolean> hadData = new Wrapper<>(false);
        final Wrapper<Boolean> importCalled = new Wrapper<>(false);
        final Wrapper<Boolean> closeCalled = new Wrapper<>(false);
        final Wrapper<Boolean> hadError = new Wrapper<>(false);
        final ExposedDataController<Object> controller = new ExposedDataController<Object>() {
            @Override
            protected DataControllerResult<Object> doGet() {
                getCalled.set(true);
                return new DataControllerResult<>(DataAccessResult.fromResult(value), 0, isFetching());
            }

            @Override
            protected void doFetch() {
                hadData.set(true);
                processResult(new DataControllerResult<>(DataAccessResult.fromResult(value), 0, false));
            }

            @Override
            protected void doFetch(int limitId) {
                // Unused / stub
                doFetch();
            }

            @Override
            protected void doImportData(Object o) {
                importCalled.set(true);
            }

            @Override
            protected void doClose() {
                closeCalled.set(true);
            }

            @Override
            public boolean isFetching() {
                return false;
            }

            @Override
            protected void onResultFetched(DataControllerResult<Object> result) {
                super.onResultFetched(result);

                if (result.hasError()) {
                    hadError.set(true);
                } else if (result.hasData()) {
                    hadData.set(true);
                }
            }
        };

        final Wrapper<Boolean> listenerFetchStarted = new Wrapper<>(false);
        final Wrapper<Boolean> listenerData = new Wrapper<>(false);
        final Wrapper<Boolean> listenerFetchFinished = new Wrapper<>(false);
        final Wrapper<Boolean> listenerError = new Wrapper<>(false);
        final DataControllerListener<Object> listener = new DataControllerListener<Object>() {
            @Override
            public void onDataFetchStarted() {
                listenerFetchStarted.set(true);
            }

            @Override
            public void onDataFetchFinished() {
                listenerFetchFinished.set(true);
            }

            @Override
            public void onResultReceived(DataControllerResult<Object> result) {
                if (result.hasError()) {
                    listenerError.set(true);
                }

                if (result.hasData()) {
                    listenerData.set(true);
                }
            }
        };

        controller.addListener(listener);

        controller.fetch();
        Assert.assertTrue(hadData.get());
        Assert.assertTrue(listenerFetchStarted.get());
        Assert.assertTrue(listenerData.get());
        Assert.assertTrue(listenerFetchFinished.get());

        controller.importData(new Object());
        Assert.assertTrue(importCalled.get());

        DataAccessResult<Object> errorResult = DataAccessResult.fromError(new DCError("", DCError.Types.UNDEFINED));
        controller.processResult(new DataControllerResult<>(errorResult, 0, false));
        Assert.assertTrue(hadError.get());
        Assert.assertTrue(listenerError.get());

        controller.close();
        Assert.assertTrue(closeCalled.get());
    }

    private static abstract class ExposedDataController<T> extends DataController<T> {
        @Override
        public void processResult(DataControllerResult<T> dataControllerResult) {
            super.processResult(dataControllerResult);
        }
    }
}
