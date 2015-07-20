package com.raizlabs.datacontroller.controller.ordered;

import com.raizlabs.datacontroller.DCError;
import com.raizlabs.datacontroller.access.AsynchronousDataAccess;
import com.raizlabs.datacontroller.access.DataAccess;
import com.raizlabs.datacontroller.access.DataAccessResult;
import com.raizlabs.datacontroller.access.SynchronousDataAccess;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseFetchStrategy<T> implements FetchStrategy<T>, ResultProcessor<T> {

    private OrderedDataController<T> dataController;

    private CancelableCallback<T> currentCallback;
    private int lastAccessIndex;
    private int fetchLimitId;
    private List<AsynchronousDataAccess<T>> asyncDataAccesses;

    protected OrderedDataController<T> getDataController() {
        return dataController;
    }

    protected void setLastAccessIndex(int index) {
        this.lastAccessIndex = index;
    }

    protected int getLastAccessIndex() {
        return lastAccessIndex;
    }

    protected int getFetchLimitId() {
        return fetchLimitId;
    }

    protected List<AsynchronousDataAccess<T>> getAsyncDataAccesses() {
        return asyncDataAccesses;
    }

    protected CancelableCallback<T> getCurrentCallback() {
        return currentCallback;
    }

    @Override
    public void setDataController(OrderedDataController<T> controller) {
        this.dataController = controller;
    }

    @Override
    public synchronized void fetch() {
        // Fetch up to the last index
        final List<AsynchronousDataAccess<T>> accesses = dataController.getAsyncDataAccesses();
        final int lastAccessId = accesses.get(accesses.size() - 1).getSourceId();
        fetch(lastAccessId);
    }

    @Override
    public synchronized void fetch(int limitId) {
        // If we're already running, don't start another update.
        if (isPending()) {
            return;
        }

        lastAccessIndex = -1;
        fetchLimitId = limitId;

        // Stop any existing calls to "reset"
        close();

        currentCallback = new CancelableCallback<>(this);
        this.asyncDataAccesses = new ArrayList<>(getDataController().getAsyncDataAccesses());
        final SynchronousDataAccess<T> syncAccess = dataController.getSyncDataAccess();

        if (syncAccess == null || syncAccess.getSourceId() != limitId) {
            boolean limitIdFound = false;
            for (DataAccess access : this.asyncDataAccesses) {
                if (access.getSourceId() == limitId) {
                    limitIdFound = true;
                    break;
                }
            }

            if (!limitIdFound) {
                close();
                String message = "Data Access not found for limit id: " + limitId;
                DCError error = new DCError(message, DCError.Types.DATA_ACCESS_NOT_FOUND);
                DataAccessResult<T> result = DataAccessResult.fromError(error);
                dataController.processResult(result, null);
            }
        }

        if (isPending()) {
            doFetch(limitId);
        }
    }

    @Override
    public synchronized boolean isPending() {
        return (currentCallback != null);
    }

    @Override
    public synchronized void close() {
        if (currentCallback != null) {
            currentCallback.close();
            currentCallback = null;
        }
    }

//    @Override
//    public synchronized void onResult(DataAccessResult<T> result, AsynchronousDataAccess<T> access) {
//        if (isPending() && shouldProcessResult(result, access)) {
//            lastAccessIndex = getAccessIndex(access);
//            getDataController().processResult(result, access);
//        }
//    }

    protected int getAccessIndex(AsynchronousDataAccess<T> access) {
        return asyncDataAccesses.indexOf(access);
    }

    protected void processResult(DataAccessResult<T> result, AsynchronousDataAccess<T> access) {
        getDataController().processResult(result, access);
    }

    protected abstract void doFetch(int limitId);
}
