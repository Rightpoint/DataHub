package com.raizlabs.datahub.hub.ordered;

import com.raizlabs.datahub.access.AsyncDataAccess;
import com.raizlabs.datahub.access.DataAccess;
import com.raizlabs.datahub.access.DataAccessResult;
import com.raizlabs.datahub.access.SyncDataAccess;
import com.raizlabs.datahub.hub.DataHub;
import com.raizlabs.datahub.hub.DataHubResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@link DataHub} which contains a single {@link SyncDataAccess} and a set of ordered {@link AsyncDataAccess}. What
 * the order means is up to the implementation of the associated {@link FetchStrategy}, but accesses closer to the
 * beginning of the list are thought to be "less accurate, but faster" whereas accesses closer to the end of the list
 * are thought to be "more accurate and truthful, but slower". See {@link FetchStrategies} for some common examples of
 * how this order may be treated. While this class is responsible for the management and housekeeping of the data
 * accesses, listeners, and other {@link DataHub} necessities, the {@link FetchStrategy} is responsible for the
 * fetching mechanism and result processing. The {@link FetchStrategy} works as the "brains" while the
 * {@link OrderedDataHub} works as the "organizer".
 * <p></p>
 * Though constructors are available, it is generally more convenient to use the
 * {@link com.raizlabs.datahub.hub.ordered.OrderedDataHub.Builder} instead.
 *
 * @param <Data> {@inheritDoc}
 * @see FetchStrategies
 */
public class OrderedDataHub<Data> extends DataHub<Data> {

    /**
     * Builder class which assists with setting up an {@link OrderedDataHub}. Note that a {@link FetchStrategy} must
     * be set through {@link #setStrategy(FetchStrategy)} or an appropriate constructor, otherwise {@link #build()} will
     * throw an exception.
     *
     * @param <T> The type of data being accessed.
     */
    public static class Builder<T> {

        /**
         * Convenience method which constructs a new builder which leverages the
         * {@link com.raizlabs.datahub.hub.ordered.FetchStrategies.Serial} strategy.
         *
         * @param <T> The type of data being accessed.
         * @return A builder to continue setting up the {@link OrderedDataHub}.
         */
        public static <T> Builder<T> newParallel() {
            return new Builder<T>().setStrategy(new FetchStrategies.Parallel<T>());
        }

        /**
         * Convenience method which constructs a new builder which leverages the
         * {@link com.raizlabs.datahub.hub.ordered.FetchStrategies.Parallel} strategy.
         *
         * @param finalizer A {@link com.raizlabs.datahub.hub.ordered.FetchStrategies.Serial.DataFinalizer} to use for
         *                  determining the final data. See {@link FetchStrategies.Serial.Finalizers} for some provided
         *                  implementations.
         * @param <T>       The type of data being accessed.
         * @return A builder to continue setting up the {@link OrderedDataHub}.
         */
        public static <T> Builder<T> newSerial(FetchStrategies.Serial.DataFinalizer<T> finalizer) {
            return new Builder<T>().setStrategy(new FetchStrategies.Serial<>(finalizer));
        }

        private FetchStrategy<T> strategy;
        private SyncDataAccess<T> synchronous;
        private List<AsyncDataAccess<T>> asynchronous;
        private boolean shouldBackport = true;

        /**
         * Sets the strategy that the {@link OrderedDataHub} will use to fetch and process data.
         *
         * @param strategy The strategy to use.
         * @return This builder for chaining method calls.
         * @see FetchStrategies
         */
        public Builder<T> setStrategy(FetchStrategy<T> strategy) {
            this.strategy = strategy;
            return this;
        }

        /**
         * Sets the {@link SyncDataAccess} that the {@link OrderedDataHub} will use for immediate data access.
         *
         * @param synchronous The {@link SyncDataAccess} to use.
         * @return This builder for chaining method calls.
         */
        public Builder<T> setSynchronousAccess(SyncDataAccess<T> synchronous) {
            this.synchronous = synchronous;
            return this;
        }

        /**
         * Sets a list of {@link AsyncDataAccess} for the {@link OrderedDataHub} to use for asynchronous data access.
         *
         * @param asynchronous The list of {@link AsyncDataAccess}.
         * @return This builder for chaining method calls.
         */
        public Builder<T> setAsynchronousAccesses(List<AsyncDataAccess<T>> asynchronous) {
            this.asynchronous = asynchronous;
            return this;
        }

        /**
         * Appends the given {@link AsyncDataAccess} to the list of accesses for the {@link OrderedDataHub} to use for
         * asynchronous data access.
         *
         * @param asynchronous THe {@link AsyncDataAccess} to append.
         * @return This builder for chaining method calls.
         */
        public Builder<T> addAsynchronousAccess(AsyncDataAccess<T> asynchronous) {
            if (this.asynchronous == null) {
                this.asynchronous = new LinkedList<>();
            }
            this.asynchronous.add(asynchronous);
            return this;
        }

        /**
         * Sets whether the {@link OrderedDataHub} should backport data.
         *
         * @param shouldBackport True to backport data.
         * @return This builder for chaining method calls.
         * @see OrderedDataHub#setShouldBackport(boolean)
         */
        public Builder<T> setShouldBackport(boolean shouldBackport) {
            this.shouldBackport = shouldBackport;
            return this;
        }

        /**
         * Builds and returns an {@link OrderedDataHub} according to the current configuration.
         *
         * @return The configured {@link OrderedDataHub}.
         * @throws IllegalStateException if no {@link FetchStrategy} has been set.
         */
        public OrderedDataHub<T> build() {
            if (strategy == null) {
                throw new IllegalStateException("Cannot build " + OrderedDataHub.class.getSimpleName() + " with no " + FetchStrategy.class.getSimpleName());
            }

            return new OrderedDataHub<>(strategy, synchronous, asynchronous, shouldBackport);
        }
    }

    private boolean shouldBackport;

    private SyncDataAccess<Data> syncDataAccess;
    private List<AsyncDataAccess<Data>> asyncDataAccesses;
    private List<AsyncDataAccess<Data>> publicDataAccesses;
    private FetchStrategy<Data> fetchStrategy;

    /**
     * Constructs an {@link OrderedDataHub} with the given parameters.
     *
     * @param strategy     The {@link FetchStrategy} to use for fetching and processing data.
     * @param synchronous  The {@link SyncDataAccess} to use for immediate data access.
     * @param asynchronous The list of {@link AsyncDataAccess} to use for asynchronous data access.
     */
    public OrderedDataHub(FetchStrategy<Data> strategy, SyncDataAccess<Data> synchronous, List<AsyncDataAccess<Data>> asynchronous) {
        this(strategy, synchronous, asynchronous, true);
    }

    /**
     * Constructs an {@link OrderedDataHub} with the given parameters.
     *
     * @param strategy     The {@link FetchStrategy} to use for fetching and processing data.
     * @param synchronous  The {@link SyncDataAccess} to use for immediate data access.
     * @param asynchronous The list of {@link AsyncDataAccess} to use for asynchronous data access.
     * @param backport     Whether data should be backported to previous async accesses. See
     *                     {@link #setShouldBackport(boolean)}.
     */
    public OrderedDataHub(FetchStrategy<Data> strategy, SyncDataAccess<Data> synchronous, List<AsyncDataAccess<Data>> asynchronous, boolean backport) {
        this.syncDataAccess = synchronous;
        this.fetchStrategy = strategy;
        this.fetchStrategy.setDataHubDelegate(fetchStrategyDelegate);

        if (asynchronous == null) {
            asynchronous = new ArrayList<>(0);
        }
        this.asyncDataAccesses = new LinkedList<>();
        for (AsyncDataAccess<Data> access : asynchronous) {
            if (access != null) {
                this.asyncDataAccesses.add(access);
            }
        }
        this.publicDataAccesses = Collections.unmodifiableList(this.asyncDataAccesses);

        setShouldBackport(backport);
    }

    /**
     * Sets whether updates from {@link AsyncDataAccess}es should be imported back into the {@link SyncDataAccess} and
     * any {@link AsyncDataAccess}es closer to the front of the list.
     *
     * @param backport True to enable backporting, false to disable.
     * @see #shouldBackportResult(DataHubResult)
     */
    public void setShouldBackport(boolean backport) {
        this.shouldBackport = backport;
    }

    /**
     * @return True if backporting is enabled, false if it is disabled.
     * @see #setShouldBackport(boolean)
     * @see #shouldBackportResult(DataHubResult)
     */
    protected boolean shouldBackport() {
        return shouldBackport;
    }

    /**
     * @return The {@link SyncDataAccess} used for immediate data access.
     */
    SyncDataAccess<Data> getSyncDataAccess() {
        return syncDataAccess;
    }

    /**
     * @return The list of {@link AsyncDataAccess} used for asynchronous access.
     */
    List<AsyncDataAccess<Data>> getAsyncDataAccesses() {
        return publicDataAccesses;
    }

    @Override
    public DataHubResult<Data> doGetCurrent() {
        if (syncDataAccess != null) {
            DataAccessResult<Data> accessResult = syncDataAccess.get();
            return new DataHubResult<>(accessResult, syncDataAccess.getTypeId(), isFetching());
        }

        return null;
    }

    @Override
    protected void doFetch() {
        fetchStrategy.fetch();
    }

    @Override
    protected void doFetch(int limitId) {
        fetchStrategy.fetch(limitId);
    }

    @Override
    public void doImportData(Data data) {
        if (syncDataAccess != null) {
            syncDataAccess.importData(data);
        }

        for (AsyncDataAccess<Data> access : asyncDataAccesses) {
            access.importData(data);
        }
    }

    @Override
    public void doClose() {
        if (syncDataAccess != null) {
            syncDataAccess.close();
        }

        for (AsyncDataAccess<Data> access : asyncDataAccesses) {
            access.close();
        }

        fetchStrategy.close();
    }

    @Override
    public boolean isFetching() {
        return fetchStrategy.isFetching();
    }

    @Override
    protected void onResultFetched(DataHubResult<Data> dataResult) {
        super.onResultFetched(dataResult);

        if (shouldBackport() && shouldBackportResult(dataResult)) {
            Data data = dataResult.getData();

            if (syncDataAccess != null) {
                syncDataAccess.importData(data);
            }

            for (AsyncDataAccess<Data> access : asyncDataAccesses) {
                // Stop when we hit the same access type
                if (access.getTypeId() == dataResult.getAccessTypeId()) {
                    break;
                }
                access.importData(data);
            }
        }
    }

    /**
     * Called to check if this specific result should be backported. Only called if {@link #shouldBackport()} is true.
     *
     * @param dataResult The result to be backported.
     * @return True to backport the result, false not to.
     * @see #shouldBackport()
     * @see #setShouldBackport(boolean)
     */
    protected boolean shouldBackportResult(DataHubResult<Data> dataResult) {
        return (dataResult.getData() != null) && (dataResult.getError() == null);
    }

    /**
     * Notifies this {@link OrderedDataHub} that a result has come back and should be processed and sent to listeners.
     *
     * @param data   The result.
     * @param access The access that produced the result.
     */
    protected void processResult(DataAccessResult<Data> data, DataAccess access) {
        synchronized (getStateLock()) {
            if (!isClosed()) {
                int accessId = (access != null) ? access.getTypeId() : DataAccess.AccessTypeIds.NONE;
                DataHubResult<Data> result = new DataHubResult<>(data, accessId, isFetching());
                onResult(result);
            }
        }
    }

    private final FetchStrategy.DataHubDelegate<Data> fetchStrategyDelegate = new FetchStrategy.DataHubDelegate<Data>() {
        @Override
        public OrderedDataHub<Data> getDataHub() {
            return OrderedDataHub.this;
        }

        @Override
        public SyncDataAccess<Data> getSyncAccess() {
            return getSyncDataAccess();
        }

        @Override
        public List<AsyncDataAccess<Data>> getAsyncAccesses() {
            return getAsyncDataAccesses();
        }

        @Override
        public void processResult(DataAccessResult<Data> data, DataAccess access) {
            OrderedDataHub.this.processResult(data, access);
        }
    };
}
