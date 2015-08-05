package com.raizlabs.datahub.hub.ordered;

import com.raizlabs.datahub.DataHubError;
import com.raizlabs.datahub.access.AsyncDataAccess;
import com.raizlabs.datahub.access.DataAccess;
import com.raizlabs.datahub.access.DataAccessResult;
import com.raizlabs.datahub.access.SyncDataAccess;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for implementing a {@link FetchStrategy} which does some of the simple housekeeping type work and
 * provides some mechanisms and functionality that may assist in the implementation of the fetching and result
 * processing details.
 *
 * @param <T> The type of data being fetched.
 */
public abstract class BaseFetchStrategy<T> implements FetchStrategy<T>, ResultProcessor<T> {

    private DataHubDelegate<T> dataHubDelegate;

    private CancelableCallback<T> currentCallback;
    private int lastAsyncAccessIndex;
    private int fetchLimitId;
    private List<AsyncDataAccess<T>> asyncDataAccesses;

    /**
     * @return The {@link com.raizlabs.datahub.hub.ordered.FetchStrategy.DataHubDelegate} to use to access the
     * associated {@link OrderedDataHub}.
     */
    protected DataHubDelegate<T> getDataHubDelegate() {
        return dataHubDelegate;
    }

    /**
     * Indicates that the {@link AsyncDataAccess} at the given index was the last one to have provided a result.
     *
     * @param index The index of the last {@link AsyncDataAccess} to have provided a result.
     * @see #getLastAsyncAccessIndex()
     */
    protected void setLastAsyncAccessIndex(int index) {
        this.lastAsyncAccessIndex = index;
    }

    /**
     * @return The index of the {@link AsyncDataAccess} that was the last one to have been indicated that it
     * provided a result. The value must be set manually by the implementing class.
     * @see #setLastAsyncAccessIndex(int)
     */
    protected int getLastAsyncAccessIndex() {
        return lastAsyncAccessIndex;
    }

    /**
     * @return The ID that was passed as the fetch limit, or the ID if the last access in the list if none was passed.
     */
    protected int getFetchLimitId() {
        return fetchLimitId;
    }

    /**
     * Returns a list of {@link AsyncDataAccess} which is a snapshot of the hub's {@link AsyncDataAccess}es at the time
     * the last fetch was started. This will only be valid after {@link #fetch()} has been called. This differs from
     * the delegate's implementation as the delegate will always return the current list, while this method returns
     * whatever was provided at the time of the fetch.
     *
     * @return A snapshot list of {@link AsyncDataAccess} at the time of the last fetch.
     */
    protected List<AsyncDataAccess<T>> getAsyncDataAccesses() {
        return asyncDataAccesses;
    }

    /**
     * Returns a {@link CancelableCallback} for the current fetch. This callback should be passed to all
     * {@link AsyncDataAccess} fetch methods so that the base class can track results and forward to them to the
     * implementations as well as prevent old calls from coming through etc.
     *
     * @return The current {@link CancelableCallback}.
     */
    protected CancelableCallback<T> getCurrentCallback() {
        return currentCallback;
    }

    @Override
    public void setDataHubDelegate(DataHubDelegate<T> delegate) {
        this.dataHubDelegate = delegate;
    }

    @Override
    public synchronized void fetch() {
        // Fetch up to the last index
        final List<AsyncDataAccess<T>> accesses = dataHubDelegate.getAsyncAccesses();
        final int lastAccessId = accesses.get(accesses.size() - 1).getTypeId();
        fetch(lastAccessId);
    }

    @Override
    public synchronized void fetch(int limitId) {
        // If we're already running, don't start another update.
        if (isFetching()) {
            return;
        }

        lastAsyncAccessIndex = -1;
        fetchLimitId = limitId;

        // Stop any existing calls to "reset"
        close();

        currentCallback = new CancelableCallback<>(this);
        this.asyncDataAccesses = new ArrayList<>(getDataHubDelegate().getAsyncAccesses());
        final SyncDataAccess<T> syncAccess = dataHubDelegate.getSyncAccess();

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
                DataHubError error = new DataHubError(message, DataHubError.Types.DATA_ACCESS_NOT_FOUND);
                DataAccessResult<T> result = DataAccessResult.fromError(error);
                dataHubDelegate.processResult(result, null);
            }
        }

        if (isFetching()) {
            doFetch(limitId);
        }
    }

    @Override
    public synchronized boolean isFetching() {
        return (currentCallback != null);
    }

    @Override
    public synchronized void close() {
        if (currentCallback != null) {
            currentCallback.cancel();
            currentCallback = null;
        }
    }

    /**
     * Convenience method to determine the index of the given {@link AsyncDataAccess} in the {@link OrderedDataHub}'s
     * list.
     *
     * @param access The access to obtain the index of.
     * @return The index of the given access, or -1 if it was not found.
     */
    protected int getAccessIndex(AsyncDataAccess<T> access) {
        return asyncDataAccesses.indexOf(access);
    }

    /**
     * Called when a result has been obtained and should be processed. This forwards the result along to the
     * {@link OrderedDataHub}.
     *
     * @param result The result to process.
     * @param access The access that provided the result.
     */
    protected void processResult(DataAccessResult<T> result, AsyncDataAccess<T> access) {
        getDataHubDelegate().processResult(result, access);
    }

    /**
     * Called to start off a fetch of new data.
     *
     * @param limitId The ID that was passed as the fetch limit, or the ID if the last access in the list if none was
     *                passed.
     */
    protected abstract void doFetch(int limitId);
}
