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

/**
 * A {@link DataObserver} acts as a single view into a {@link DataHub}. This provides a way for any number of listeners
 * to subscribe to updates, while providing a single place for these listeners to be cleaned up and disconnected. This
 * also provides mechanisms for filtering certain results from being passed to the listeners (see
 * {@link #addDispatchResultFilter(ResultFilter)}). In a sense, a {@link DataObserver} can be thought of as a hub that
 * connects an individual {@link DataHub} to many listeners. The hub itself may also filter out incoming results before
 * they are dispatched to its connected listeners.
 * <p></p>
 * For example, if you have a set of views (such as an Activity) which want to subscribe to a single {@link DataHub}'s
 * data, they can all be subscribed to a single {@link DataObserver}. If certain results are not desirable for this
 * screen (such as stale data), they may be filtered out. Once the views are to be destroyed (or the Activity is
 * stopped) all listeners can be cleaned up all at once by calling {@link #close(boolean)} on the {@link DataObserver}
 * instead of removing each listener individually.
 * <p></p>
 * A {@link DataObserver} also provides the ability to dispatch all listener updates on a specific {@link Handler}.
 * Since this class is usually used at the view level to populate views, the default is to dispatch all updates on the
 * UI thread, though this may be changed via an overloaded constructor (see {@link #DataObserver(DataHub, Handler)}).
 *
 * @param <Data> The type of data being accessed.
 */
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
     * @param callbackHandler The Handler to dispatch future callbacks to, or null to dispatch them straight from the
     *                        threads the {@link DataHub} is calling from.
     */
    public DataObserver(DataHub<Data> dataHub, Handler callbackHandler) {
        this.dataHub = dataHub;
        this.listenerHandler = callbackHandler;
        this.listeners = new MappableSet<>();
        this.dispatchResultFilters = new LinkedList<>();

        this.dataHub.addListener(dataHubListener);
    }

    /**
     * @return A lock object which may be synchronized on to prevent state updates.
     */
    protected Object getStateLock() {
        return this;
    }

    /**
     * Add a listener to be notified of state and data updates. If a fetch is already running, the listener will be
     * initialized with calls to {@link DataObserverListener#onDataFetchStarted()} and
     * {@link DataObserverListener#onResultReceived(DataHubResult)} with the {@link DataHub}'s current data.
     *
     * @param listener The listener to add.
     */
    public void addListener(final DataObserverListener<Data> listener) {
        dispatchListenerLogic(new Runnable() {
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

    /**
     * Removes a listener from being notified of state and data updates.
     *
     * @param listener The listener to remove.
     * @return True if the listener was removed, false if it wasn't already subscribed.
     */
    public boolean removeListener(DataObserverListener<Data> listener) {
        return listeners.remove(listener);
    }

    /**
     * Returns the most current result which is immediately available from the {@link DataHub}.
     *
     * @return The current immediate result from the {@link DataHub}.
     * @see DataHub#getCurrent()
     */
    public DataHubResult<Data> getCurrent() {
        if (dataHub != null) {
            return dataHub.getCurrent();
        } else {
            return null;
        }
    }

    /**
     * Sends the most current result from the {@link DataHub} to every subscribed listener. The value passed is the
     * same value returned by {@link #getCurrent()}.
     */
    public void dispatchCurrent() {
        onResultReceived(getCurrent());
    }

    /**
     * Tells the {@link DataHub} to start retrieving an up to date result asynchronously.
     *
     * @see DataHub#fetch()
     */
    public void fetch() {
        if (dataHub != null) {
            dataHub.fetch();
        }
    }

    /**
     * Tells the {@link DataHub} to start retrieving an up to date asynchronously, using the given type id as an upper
     * limit of sources to query.
     *
     * @param limitId The upper limit or bound of sources to query.
     * @see DataHub#fetch(int)
     */
    public void fetch(int limitId) {
        if (dataHub != null) {
            dataHub.fetch(limitId);
        }
    }

    /**
     * Indicates that this {@link DataObserver} will no longer be used and should clean up any resources associated with
     * it.
     *
     * @param completeShutdown True to also close the associated {@link DataHub}, false to leave it as is but still
     *                         disconnect this {@link DataObserver} and all listeners from it.
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

    /**
     * Adds a filter to be run against incoming results. If any filter rejects a result, it will not be dispatched to
     * listeners.
     *
     * @param filter The filter to add.
     */
    public void addDispatchResultFilter(ResultFilter<? super Data> filter) {
        dispatchResultFilters.add(filter);
    }

    /**
     * Removes a filter from being run against incoming results.
     *
     * @param filter The filter to remove.
     * @return True if the filter was removed, false if it was not found.
     */
    public boolean removeDispatchResultFilter(ResultFilter<? super Data> filter) {
        return dispatchResultFilters.remove(filter);
    }

    /**
     * Determines whether the given result should be dispatched to listeners. By default, this checks the result
     * against all the filters defined by {@link #addDispatchResultFilter(ResultFilter)}.
     *
     * @param result The result to check.
     * @return True to dispatch the result, false not to.
     */
    protected boolean shouldDispatchResult(DataHubResult<Data> result) {
        for (ResultFilter<? super Data> filter : dispatchResultFilters) {
            if (filter.shouldFilter(result)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Called to dispatch fetching start indication via the listener.
     */
    protected void onDataFetchStarted() {
        dispatchListenerLogic(new Runnable() {
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
        dispatchListenerLogic(new Runnable() {
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
            dispatchListenerLogic(new Runnable() {
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

    /**
     * Called to initialize a listener. If a fetch is already running, the listener will be initialized with calls to
     * {@link DataObserverListener#onDataFetchStarted()} and
     * {@link DataObserverListener#onResultReceived(DataHubResult)} with the {@link DataHub}'s current data.
     *
     * @param listener The listener to initialize.
     */
    protected void initializeListener(final DataObserverListener<Data> listener) {
        dispatchListenerLogic(new Runnable() {
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

    /**
     * Dispatches the given {@link Runnable} to be executed on the proper thread as defined for this
     * {@link DataObserver}'s listener processing.
     *
     * @param runnable The runnable to execute.
     */
    protected void dispatchListenerLogic(Runnable runnable) {
        synchronized (getStateLock()) {
            if (listenerHandler != null) {
                listenerHandler.post(runnable);
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
