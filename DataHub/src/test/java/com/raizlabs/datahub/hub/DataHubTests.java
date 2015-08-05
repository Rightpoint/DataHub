package com.raizlabs.datahub.hub;

import com.raizlabs.datahub.DataHubError;
import com.raizlabs.datahub.access.DataAccessResult;
import com.raizlabs.datahub.utils.Wrapper;

import junit.framework.Assert;

import org.junit.Test;

public class DataHubTests {

    @Test
    public void testEventMethods() {
        final Object value = new Object();
        final Wrapper<Boolean> getCalled = new Wrapper<>(false);
        final Wrapper<Boolean> hadData = new Wrapper<>(false);
        final Wrapper<Boolean> importCalled = new Wrapper<>(false);
        final Wrapper<Boolean> closeCalled = new Wrapper<>(false);
        final Wrapper<Boolean> hadError = new Wrapper<>(false);
        final ExposedDataHub<Object> hub = new ExposedDataHub<Object>() {
            @Override
            protected DataHubResult<Object> doGetCurrent() {
                getCalled.set(true);
                return new DataHubResult<>(DataAccessResult.fromResult(value), 0, isFetching());
            }

            @Override
            protected void doFetch() {
                hadData.set(true);
                onProcessResult(new DataHubResult<>(DataAccessResult.fromResult(value), 0, false));
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
            protected void onResultFetched(DataHubResult<Object> result) {
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
        final DataHubListener<Object> listener = new DataHubListener<Object>() {
            @Override
            public void onDataFetchStarted() {
                listenerFetchStarted.set(true);
            }

            @Override
            public void onDataFetchFinished() {
                listenerFetchFinished.set(true);
            }

            @Override
            public void onResultReceived(DataHubResult<Object> result) {
                if (result.hasError()) {
                    listenerError.set(true);
                }

                if (result.hasData()) {
                    listenerData.set(true);
                }
            }
        };

        hub.addListener(listener);

        hub.fetch();
        Assert.assertTrue(hadData.get());
        Assert.assertTrue(listenerFetchStarted.get());
        Assert.assertTrue(listenerData.get());
        Assert.assertTrue(listenerFetchFinished.get());

        hub.importData(new Object());
        Assert.assertTrue(importCalled.get());

        DataAccessResult<Object> errorResult = DataAccessResult.fromError(new DataHubError("", DataHubError.Types.UNDEFINED));
        hub.onProcessResult(new DataHubResult<>(errorResult, 0, false));
        Assert.assertTrue(hadError.get());
        Assert.assertTrue(listenerError.get());

        hub.close();
        Assert.assertTrue(closeCalled.get());
    }

    private static abstract class ExposedDataHub<T> extends DataHub<T> {
        @Override
        protected void onProcessResult(DataHubResult<T> dataHubResult) {
            super.onProcessResult(dataHubResult);
        }
    }
}
