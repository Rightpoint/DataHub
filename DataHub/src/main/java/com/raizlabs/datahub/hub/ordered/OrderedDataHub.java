package com.raizlabs.datahub.hub.ordered;

import com.raizlabs.datahub.access.AsynchronousDataAccess;
import com.raizlabs.datahub.access.DataAccess;
import com.raizlabs.datahub.access.DataAccessResult;
import com.raizlabs.datahub.access.SynchronousDataAccess;
import com.raizlabs.datahub.hub.DataHub;
import com.raizlabs.datahub.hub.DataHubResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class OrderedDataHub<Data> extends DataHub<Data> {

    public static class Builder<T> {

        public static <T> Builder<T> newParallel() {
            return new Builder<T>().setStrategy(new FetchStrategies.Parallel<T>());
        }

        public static <T> Builder<T> newSerial(FetchStrategies.Serial.DataValidator<T> validator) {
            return new Builder<T>().setStrategy(new FetchStrategies.Serial<>(validator));
        }

        private FetchStrategy<T> strategy;
        private SynchronousDataAccess<T> synchronous;
        private List<AsynchronousDataAccess<T>> asynchronous;
        private boolean shouldBackport = true;

        public Builder<T> setStrategy(FetchStrategy<T> strategy) {
            this.strategy = strategy;
            return this;
        }

        public Builder<T> setSynchronousAccess(SynchronousDataAccess<T> synchronous) {
            this.synchronous = synchronous;
            return this;
        }

        public Builder<T> setAsynchronousAccesses(List<AsynchronousDataAccess<T>> asynchronous) {
            this.asynchronous = asynchronous;
            return this;
        }

        public Builder<T> addAsynchronousAccess(AsynchronousDataAccess<T> asynchronous) {
            if (this.asynchronous == null) {
                this.asynchronous = new LinkedList<>();
            }
            this.asynchronous.add(asynchronous);
            return this;
        }

        public Builder<T> setShouldBackport(boolean shouldBackport) {
            this.shouldBackport = shouldBackport;
            return this;
        }

        public OrderedDataHub<T> build() {
            if (strategy == null) {
                throw new IllegalStateException("Cannot build " + OrderedDataHub.class.getSimpleName() + " with no " + FetchStrategy.class.getSimpleName());
            }

            return new OrderedDataHub<>(strategy, synchronous, asynchronous, shouldBackport);
        }
    }

    private boolean shouldBackport;

    private SynchronousDataAccess<Data> syncDataAccess;
    private List<AsynchronousDataAccess<Data>> asyncDataAccesses;
    private List<AsynchronousDataAccess<Data>> publicDataAccesses;
    private FetchStrategy<Data> fetchStrategy;

    public OrderedDataHub(FetchStrategy<Data> strategy, SynchronousDataAccess<Data> synchronous, List<AsynchronousDataAccess<Data>> asynchronous) {
        this(strategy, synchronous, asynchronous, true);
    }

    public OrderedDataHub(FetchStrategy<Data> strategy, SynchronousDataAccess<Data> synchronous, List<AsynchronousDataAccess<Data>> asynchronous, boolean backport) {
        this.syncDataAccess = synchronous;
        this.fetchStrategy = strategy;
        this.fetchStrategy.setDataHub(this);

        if (asynchronous == null) {
            asynchronous = new ArrayList<>(0);
        }
        this.asyncDataAccesses = new LinkedList<>();
        for (AsynchronousDataAccess<Data> access : asynchronous) {
            if (access != null) {
                this.asyncDataAccesses.add(access);
            }
        }
        this.publicDataAccesses = Collections.unmodifiableList(this.asyncDataAccesses);

        setShouldBackport(backport);
    }

    public void setShouldBackport(boolean backport) {
        this.shouldBackport = backport;
    }

    protected boolean shouldBackport() {
        return shouldBackport;
    }

    public SynchronousDataAccess<Data> getSyncDataAccess() {
        return syncDataAccess;
    }

    public List<AsynchronousDataAccess<Data>> getAsyncDataAccesses() {
        return publicDataAccesses;
    }

    @Override
    public DataHubResult<Data> doGet() {
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

        for (AsynchronousDataAccess<Data> access : asyncDataAccesses) {
            access.importData(data);
        }
    }

    @Override
    public void doClose() {
        if (syncDataAccess != null) {
            syncDataAccess.close();
        }

        for (AsynchronousDataAccess<Data> access : asyncDataAccesses) {
            access.close();
        }

        fetchStrategy.close();
    }

    @Override
    public boolean isFetching() {
        return fetchStrategy.isPending();
    }

    @Override
    protected void onResultFetched(DataHubResult<Data> dataResult) {
        super.onResultFetched(dataResult);

        if (shouldBackport() && shouldBackportResult(dataResult)) {
            Data data = dataResult.getData();

            if (syncDataAccess != null) {
                syncDataAccess.importData(data);
            }

            for (AsynchronousDataAccess<Data> access : asyncDataAccesses) {
                // Stop when we hit the same access type
                if (access.getTypeId() == dataResult.getAccessTypeId()) {
                    break;
                }
                access.importData(data);
            }
        }
    }


    protected boolean shouldBackportResult(DataHubResult<Data> dataResult) {
        return (dataResult.getData() != null) && (dataResult.getError() == null);
    }

    protected void processResult(DataAccessResult<Data> data, DataAccess access) {
        synchronized (getStateLock()) {
            if (!isClosed()) {
                int accessId = (access != null) ? access.getTypeId() : AccessTypeIds.NONE;
                DataHubResult<Data> result = new DataHubResult<>(data, accessId, isFetching());
                processResult(result);
            }
        }
    }
}
