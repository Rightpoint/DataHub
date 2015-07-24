package com.raizlabs.datahub.hub.ordered;

import com.raizlabs.datahub.DCError;
import com.raizlabs.datahub.access.AsynchronousDataAccess;
import com.raizlabs.datahub.access.DataAccess;
import com.raizlabs.datahub.access.DataAccessResult;
import com.raizlabs.datahub.access.SynchronousDataAccess;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseFetchStrategy<T> implements FetchStrategy<T>, ResultProcessor<T> {

    private OrderedDataHub<T> dataHub;

    private CancelableCallback<T> currentCallback;
    private int lastAccessIndex;
    private int fetchLimitId;
    private List<AsynchronousDataAccess<T>> asyncDataAccesses;

    protected OrderedDataHub<T> getDataHub() {
        return dataHub;
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
    public void setDataHub(OrderedDataHub<T> hub) {
        this.dataHub = hub;
    }

    @Override
    public synchronized void fetch() {
        // Fetch up to the last index
        final List<AsynchronousDataAccess<T>> accesses = dataHub.getAsyncDataAccesses();
        final int lastAccessId = accesses.get(accesses.size() - 1).getTypeId();
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
        this.asyncDataAccesses = new ArrayList<>(getDataHub().getAsyncDataAccesses());
        final SynchronousDataAccess<T> syncAccess = dataHub.getSyncDataAccess();

        if (syncAccess == null || syncAccess.getTypeId() != limitId) {
            boolean limitIdFound = false;
            for (DataAccess access : this.asyncDataAccesses) {
                if (access.getTypeId() == limitId) {
                    limitIdFound = true;
                    break;
                }
            }

            if (!limitIdFound) {
                close();
                String message = "Data Access not found for limit id: " + limitId;
                DCError error = new DCError(message, DCError.Types.DATA_ACCESS_NOT_FOUND);
                DataAccessResult<T> result = DataAccessResult.fromError(error);
                dataHub.processResult(result, null);
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

    protected int getAccessIndex(AsynchronousDataAccess<T> access) {
        return asyncDataAccesses.indexOf(access);
    }

    protected void processResult(DataAccessResult<T> result, AsynchronousDataAccess<T> access) {
        getDataHub().processResult(result, access);
    }

    protected abstract void doFetch(int limitId);
}
