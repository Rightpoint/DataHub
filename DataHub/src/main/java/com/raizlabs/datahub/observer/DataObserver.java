package com.raizlabs.datahub.observer;

import android.os.Handler;

import com.raizlabs.datahub.hub.DataHub;
import com.raizlabs.datahub.hub.DataHubListener;
import com.raizlabs.datahub.hub.DataHubResult;
import com.raizlabs.datahub.util.Delegate;
import com.raizlabs.datahub.util.MappableSet;
import com.raizlabs.datahub.util.ThreadingUtils;

import java.util.LinkedList;
import java.util.List;

public class DataObserver<Data> {

    private DataHub<Data> dataHub;

    private MappableSet<DataObserverListener<Data>> listeners;
    private List<ResultFilter<? super Data>> dispatchResultFilters;

    private Handler listenerHandler;

    /**
     * Constructs a {@link DataObserver} which accesses data from the given {@link DataHub}. All update callbacks will be
     * dispatched on the UI thread.
     *
     * @param dataHub The {@link DataHub} to access data from.
     */
    public DataObserver(DataHub<Data> dataHub) {
        this(dataHub, ThreadingUtils.getUIHandler());
    }

    /**
     * Constructs a {@link DataObserver} which accesses data from the given {@link DataHub} and dispatches all update callbacks
     * via the given {@link Handler}.
     *
     * @param dataHub         The {@link DataHub} to access data from.
     * @param listenerHandler The Handler to dispatch future callbacks to, or null to dispatch them straight from the
     *                        threads the {@link DataHub} is calling from.
     */
    public DataObserver(DataHub<Data> dataHub, Handler listenerHandler) {
        this.dataHub = dataHub;
        this.listenerHandler = listenerHandler;
        this.listeners = new MappableSet<>();
        this.dispatchResultFilters = new LinkedList<>();

        this.dataHub.addListener(dataHubListener);
    }

    public void addListener(final DataObserverListener<Data> listener) {
        dispatch(new Runnable() {
            @Override
            public void run() {
                synchronized (getStateLock()) {
                    if (!listeners.contains(listener)) {
                        listeners.add(listener);
                        initializeListener(listener);
                    }
                }
            }
        });
    }

    public boolean removeListener(DataObserverListener<Data> listener) {
        return listeners.remove(listener);
    }

    public DataHubResult<Data> getCurrent() {
        if (dataHub != null) {
            return dataHub.getCurrent();
        } else {
            return null;
        }
    }

    public void dispatchCurrent() {
        onResultReceived(getCurrent());
    }

    public void fetch() {
        if (dataHub != null) {
            dataHub.fetch();
        }
    }

    public void fetch(int limitId) {
        if (dataHub != null) {
            dataHub.fetch(limitId);
        }
    }

    /**
     * Indicates that this {@link DataObserver} will no longer be used and should clean up any resources associated with
     * it.
     *
     * @param completeShutdown If set to <code>true</code> all the attached listeners to the {@link DataHub} will
     *                         be cleared and the async requests, if any, will be interrupted/cancelled. Else if set to
     *                         <code>false</code> only this listener will be detached from the associated {@link
     *                         DataHub} and the existing async data requests, if any, will continue to stay
     *                         alive.
     */
    public void close(boolean completeShutdown) {

        listeners.clear();

        if (dataHub != null) {
            dataHub.removeListener(dataHubListener);

            if (completeShutdown) {
                dataHub.close();
            }

            dataHub = null;
        }
    }

    public void addDispatchResultFilter(ResultFilter<? super Data> filter) {
        dispatchResultFilters.add(filter);
    }

    public void removeDispatchResultFilter(ResultFilter<? super Data> filter) {
        dispatchResultFilters.remove(filter);
    }

    protected boolean shouldDispatchResult(DataHubResult<Data> result) {
        for (ResultFilter<? super Data> filter : dispatchResultFilters) {
            if (filter.shouldFilter(result)) {
                return false;
            }
        }

        return true;
    }

    protected Object getStateLock() {
        return this;
    }

    /**
     * Called to dispatch fetching start indication via the listener.
     */
    protected void onDataFetchStarted() {
        dispatch(new Runnable() {
            @Override
            public void run() {
                synchronized (getStateLock()) {
                    listeners.map(new Delegate<DataObserverListener<Data>>() {
                        @Override
                        public void execute(DataObserverListener<Data> dataDataObserverListener) {
                            dataDataObserverListener.onDataFetchStarted();
                        }
                    });
                }
            }
        });
    }

    /**
     * Called to dispatch fetching finished indication via the listener.
     */
    protected void onDataFetchFinished() {
        dispatch(new Runnable() {
            @Override
            public void run() {
                synchronized (getStateLock()) {
                    listeners.map(new Delegate<DataObserverListener<Data>>() {
                        @Override
                        public void execute(DataObserverListener<Data> dataDataObserverListener) {
                            dataDataObserverListener.onDataFetchFinished();
                        }
                    });
                }
            }
        });
    }

    /**
     * Called to dispatch results via the listener.
     */
    protected void onResultReceived(final DataHubResult<Data> dataResult) {
        if (shouldDispatchResult(dataResult)) {
            dispatch(new Runnable() {
                @Override
                public void run() {
                    synchronized (getStateLock()) {
                        listeners.map(new Delegate<DataObserverListener<Data>>() {
                            @Override
                            public void execute(DataObserverListener<Data> dataDataObserverListener) {
                                dataDataObserverListener.onResultReceived(dataResult);
                            }
                        });
                    }
                }
            });
        }
    }

    protected void initializeListener(final DataObserverListener<Data> listener) {
        dispatch(new Runnable() {
            @Override
            public void run() {
                doInitializeListener(listener);
            }
        });
    }

    private void doInitializeListener(DataObserverListener<Data> listener) {
        synchronized (getStateLock()) {
            if ((dataHub != null) && dataHub.isFetching()) {
                listener.onDataFetchStarted();

                listener.onResultReceived(dataHub.getCurrent());
            }
        }
    }

    protected void dispatch(Runnable runnable) {
        synchronized (getStateLock()) {
            if (listenerHandler != null) {
                ThreadingUtils.runOnHandler(listenerHandler, runnable);
            } else {
                runnable.run();
            }
        }
    }

    private DataHubListener<Data> dataHubListener = new DataHubListener<Data>() {
        @Override
        public void onDataFetchStarted() {
            DataObserver.this.onDataFetchStarted();
        }

        @Override
        public void onDataFetchFinished() {
            DataObserver.this.onDataFetchFinished();
        }

        @Override
        public void onResultReceived(DataHubResult<Data> result) {
            DataObserver.this.onResultReceived(result);
        }
    };
}
