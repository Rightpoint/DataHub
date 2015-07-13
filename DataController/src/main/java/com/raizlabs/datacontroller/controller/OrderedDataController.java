package com.raizlabs.datacontroller.controller;

import com.raizlabs.datacontroller.DataAccessResult;
import com.raizlabs.datacontroller.DataResult;
import com.raizlabs.datacontroller.access.AsynchronousDataAccess;
import com.raizlabs.datacontroller.access.SynchronousDataAccess;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class OrderedDataController<Data> extends DataController<Data> {

    private boolean shouldBackport;

    private SynchronousDataAccess<Data> syncDataAccess;
    private List<AsynchronousDataAccess<Data>> asyncDataAccess;
    private FetchHelper<Data> fetchHelper;

    public OrderedDataController(SynchronousDataAccess<Data> synchronous, List<AsynchronousDataAccess<Data>> asynchronous) {
        this(synchronous, asynchronous, true);
    }

    public OrderedDataController(SynchronousDataAccess<Data> synchronous, List<AsynchronousDataAccess<Data>> asynchronous, boolean backport) {
        this.syncDataAccess = synchronous;

        if (asynchronous == null) {
            asynchronous = new ArrayList<>(0);
        }
        this.asyncDataAccess = new LinkedList<>();
        for (AsynchronousDataAccess<Data> access : asynchronous) {
            if (access != null) {
                this.asyncDataAccess.add(access);
            }
        }

        this.fetchHelper = new FetchHelper<>(this);
        setShouldBackport(backport);
    }

    public void setShouldBackport(boolean backport) {
        this.shouldBackport = backport;
    }

    protected boolean shouldBackport() {
        return shouldBackport;
    }

    @Override
    public ControllerResult<Data> doGet() {
        if (syncDataAccess != null) {
            DataAccessResult<Data> accessResult = syncDataAccess.get();
            return new ControllerResult<>(accessResult, syncDataAccess.getSourceId(), isFetching());
        }

        return null;
    }

    @Override
    public void doFetch() {
        fetchHelper.fetch();
    }

    @Override
    public void doImportData(Data data) {
        syncDataAccess.importData(data);

        for (AsynchronousDataAccess<Data> access : asyncDataAccess) {
            access.importData(data);
        }
    }

    @Override
    public void doClose() {
        syncDataAccess.close();

        for (AsynchronousDataAccess<Data> access : asyncDataAccess) {
            access.close();
        }

        fetchHelper.close();
    }

    @Override
    public boolean isFetching() {
        return fetchHelper.isPending();
    }

    @Override
    protected void onDataFetched(DataResult<Data> dataResult) {
        super.onDataFetched(dataResult);

        if (shouldBackport() && shouldBackportResult(dataResult)) {
            Data data = dataResult.getData();
            syncDataAccess.importData(data);

            for (AsynchronousDataAccess<Data> access : asyncDataAccess) {
                access.importData(data);
                // Stop when we hit the same source
                if (access.getSourceId() == dataResult.getDataSourceId()) {
                    break;
                }
            }
        }
    }

    protected boolean shouldBackportResult(DataResult<Data> dataResult) {
        return true;
    }

    protected void processAsyncResult(DataAccessResult<Data> data, AsynchronousDataAccess<Data> access) {
        synchronized (getDataLock()) {
            if (!isClosed()) {
                ControllerResult<Data> result = new ControllerResult<>(data, access.getSourceId(), isFetching());
                processResult(result);
            }
        }
    }

    private static class FetchHelper<T> {

        private OrderedDataController<T> dataController;

        private List<AsynchronousDataAccess<T>> dataAccesses;
        private int lastAccessIndex;

        private CancelableCallback<T> previousCallback;

        public FetchHelper(OrderedDataController<T> dataController) {
            this.dataController = dataController;
        }

        public synchronized void fetch() {
            // If we're already running, don't start another update.
            if (isPending()) {
                return;
            }

            lastAccessIndex = -1;

            // Stop any existing calls to "reset"
            close();

            CancelableCallback<T> callback = new CancelableCallback<>(this);
            previousCallback = callback;

            this.dataAccesses = new ArrayList<>(dataController.asyncDataAccess);

            // Start each access
            // Can't do both operations simultaneously in case accesses return in line.
            for (AsynchronousDataAccess<T> access : dataAccesses) {
                access.get(callback);
            }

        }

        public synchronized boolean isPending() {
            return (previousCallback != null);
        }

        public synchronized void close() {
            if (previousCallback != null) {
                previousCallback.close();
                previousCallback = null;
            }
        }

        private synchronized void processResult(DataAccessResult<T> data, AsynchronousDataAccess<T> access) {
            if (onAccessResult(getAccessIndex(access))) {
                dataController.processAsyncResult(data, access);
            }
        }

        /**
         * @param index
         * @return True if the result of the given index should be processed.
         */
        private synchronized boolean onAccessResult(int index) {
            // Only process the result if the index increased
            if (index > lastAccessIndex && isPending()) {
                lastAccessIndex = index;

                // This was the last one, finish everything
                if (index >= dataAccesses.size() - 1) {
                    close();
                }

                return true;
            } else {
                return false;
            }
        }

        private int getAccessIndex(AsynchronousDataAccess<T> access) {
            return dataAccesses.indexOf(access);
        }

        private static class CancelableCallback<T> implements AsynchronousDataAccess.Callback<T> {

            private WeakReference<FetchHelper<T>> helperReference;

            public CancelableCallback(FetchHelper<T> helper) {
                helperReference = new WeakReference<>(helper);
            }

            public void close() {
                this.helperReference = null;
            }

            @Override
            public void onResult(DataAccessResult<T> result, AsynchronousDataAccess<T> access) {
                FetchHelper<T> helper = getHelper();
                if (helper != null) {
                    helper.processResult(result, access);
                }
            }

            private FetchHelper<T> getHelper() {
                if (helperReference != null) {
                    return helperReference.get();
                } else {
                    return null;
                }
            }
        }
    }
}
