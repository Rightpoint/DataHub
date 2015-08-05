package com.raizlabs.datahub.hub.ordered;

import com.raizlabs.datahub.access.AsyncDataAccess;
import com.raizlabs.datahub.access.DataAccess;
import com.raizlabs.datahub.access.DataAccessResult;
import com.raizlabs.datahub.access.SyncDataAccess;

import java.util.List;

/**
 * A {@link FetchStrategy} defines a method for an {@link OrderedDataHub} to retrieve data from its async sources.
 *
 * @param <T> The type of data being accessed.
 */
public interface FetchStrategy<T> {

    /**
     * Delegate interface that must be provided to a {@link FetchStrategy} to allow it to access the information that
     * it needs.
     *
     * @param <U> The type of data being accessed.
     */
    interface DataHubDelegate<U> {
        /**
         * @return The {@link OrderedDataHub} to fetch through.
         */
        OrderedDataHub<U> getDataHub();

        /**
         * @return The {@link SyncDataAccess} that the hub is using.
         */
        SyncDataAccess<U> getSyncAccess();

        /**
         * @return The ordered list of all {@link AsyncDataAccess} that the hub is using.
         */
        List<AsyncDataAccess<U>> getAsyncAccesses();

        /**
         * Called to notify the {@link OrderedDataHub} that a result has been retrieved.
         *
         * @param data   The result.
         * @param access The access that produced the result.
         */
        void processResult(DataAccessResult<U> data, DataAccess access);
    }

    /**
     * Sets the delegate to use to access {@link OrderedDataHub} information. This should be set initially before other
     * methods are used.
     *
     * @param delegate The delegate to use.
     */
    void setDataHubDelegate(DataHubDelegate<T> delegate);

    /**
     * Called to start retrieving an up to date result asynchronously from all sources.
     */
    void fetch();

    /**
     * Called to start retrieving an up to date result asynchronously. This takes an ID of a type of source which is
     * to be used as an upper limit or bound of sources to query.
     *
     * @param limitId The upper limit or bound of sources to query.
     */
    void fetch(int limitId);

    /**
     * Called to determine whether this {@link FetchStrategy} is currently fetching data.
     *
     * @return True if data is currently being fetched.
     */
    boolean isFetching();

    /**
     * Closes this {@link FetchStrategy}, indicating that it will no longer be used and may free associated resources.
     */
    void close();
}
