package com.raizlabs.datahub.hub;

import android.os.Handler;

import com.raizlabs.datahub.DataHubError;
import com.raizlabs.datahub.access.DataAccess;
import com.raizlabs.datahub.access.DataAccessResult;
import com.raizlabs.datahub.util.Delegate;
import com.raizlabs.datahub.util.MappableSet;
import com.raizlabs.datahub.util.ThreadingUtils;

/**
 * A {@link DataHub} defines a central point to obtain a single piece of data. An individual hub is possibly backed by
 * a set of distinct {@link DataAccess}es as sources of data. It is the hub's job to query all available sources and
 * return the appropriate result or results.
 * <p></p>
 * Each {@link DataAccess} contained in a {@link DataHub} must return unique type IDs via
 * {@link DataAccess#getTypeId()}, but these may be shared by a {@link DataAccess}es in different {@link DataHub}s.
 *
 * @param <Data> The type of data being accessed.
 */
public abstract class DataHub<Data> {

    private static final DataHubError ERROR_CLOSED =
            new DataHubError("Could not access data because the DataHub is closed", DataHubError.Types.INVALID_STATE);

    //region Members
    private MappableSet<DataHubListener<Data>> listeners = new MappableSet<>();
    private boolean isClosed;

    private Handler processingHandler;
    //endregion Members

    //region Accessors

    /**
     * @return True if this {@link DataHub} is closed.
     */
    public boolean isClosed() {
        return isClosed;
    }
    //endregion Accessors

    //region Abstract Methods

    /**
     * Called to obtain the most current result that is immediately available.
     *
     * @return The current immediate result.
     * @see #getCurrent()
     */
    protected abstract DataHubResult<Data> doGetCurrent();

    /**
     * Called to start retrieving an up to date result asynchronously.
     *
     * @see #fetch()
     */
    protected abstract void doFetch();

    /**
     * Called to start retrieving an up to date result asynchronously, up to an upper limit.
     *
     * @param limitId THe upper limit or bound of sources to query.
     * @see #fetch(int)
     */
    protected abstract void doFetch(int limitId);

    /**
     * Called to attempt to import the given data.
     *
     * @param data The data to import.
     * @see #importData(Object)
     */
    protected abstract void doImportData(Data data);

    /**
     * Called when this {@link DataHub} is being closed.
     *
     * @see #close()
     */
    protected abstract void doClose();

    /**
     * Called to determine whether this {@link DataHub} is currently fetching data.
     *
     * @return True if data is currently being fetched.
     */
    public abstract boolean isFetching();
    //endregion Abstract Methods

    //region Instance Methods

    /**
     * Returns the most current result which is immediately available.
     *
     * @return The current immediate result.
     */
    public DataHubResult<Data> getCurrent() {
        if (isClosed()) {
            processClosedError();
            return null;
        } else {
            return doGetCurrent();
        }
    }

    /**
     * Starts retrieving an up to date result asynchronously from all sources.
     */
    public void fetch() {
        synchronized (getStateLock()) {
            if (isClosed()) {
                processClosedError();
            } else if (!isFetching()) {
                onFetchStarted();
                doFetch();
            }
        }
    }

    /**
     * Starts retrieving an up to date result asynchronously. This takes an ID of a type of source which is to be used
     * as an upper limit or bound of sources to query.
     *
     * @param limitId The upper limit or bound of sources to query.
     */
    public void fetch(int limitId) {
        synchronized (getStateLock()) {
            if (isClosed()) {
                processClosedError();
            } else if (!isFetching()) {
                onFetchStarted();
                doFetch(limitId);
            }
        }
    }

    /**
     * Attempts to import the given data into this {@link DataHub} and its contained sources, replacing the current
     * data. Whether this is possible is up to the implementation of the particular {@link DataHub} and possibly its
     * sources as well. Therefore this method is not guaranteed to change the data returned from this {@link DataHub}.
     *
     * @param data The data to import.
     */
    public void importData(Data data) {
        synchronized (getStateLock()) {
            doImportData(data);
        }
    }

    /**
     * Closes this {@link DataHub}, indicating that it will no longer be used and may free associated resources.
     */
    public void close() {
        synchronized (getStateLock()) {
            this.isClosed = true;
            doClose();
        }
    }

    /**
     * Adds a listener to be called when the state changes or new data is received.
     *
     * @param listener The listener to subscribe to updates.
     */
    public void addListener(DataHubListener<Data> listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener from being called when the state changes or new data is received.
     *
     * @param listener The listener to unsubscribe from updates.
     */
    public void removeListener(DataHubListener<Data> listener) {
        listeners.remove(listener);
    }

    /**
     * Sets the {@link Handler} to use to process and dispatch updates when they come in.
     *
     * @param handler The {@link Handler} to use for updates or null to do them immediately on the threads which invoke
     *                them.
     */
    public void setProcessingHandler(Handler handler) {
        synchronized (getStateLock()) {
            this.processingHandler = handler;
        }
    }

    /**
     * @return A lock object which may be synchronized on to prevent state updates.
     */
    protected Object getStateLock() {
        return this;
    }

    /**
     * Called when fetches start in order to process the necessary actions.
     *
     * @see #onProcessFetchStarted() to add additional processing logic.
     */
    protected final void onFetchStarted() {
        synchronized (getStateLock()) {
            process(fetchStartedRunnable);
        }
    }

    /**
     * Called when fetches have been started in order to process the state change and notify listeners.
     */
    protected void onProcessFetchStarted() {
        listeners.map(new Delegate<DataHubListener<Data>>() {
            @Override
            public void execute(DataHubListener<Data> listener) {
                listener.onDataFetchStarted();
            }
        });
    }

    /**
     * Called when fetches are completed in order to process the necessary actions.
     */
    protected final void onFetchFinished() {
        synchronized (getStateLock()) {
            process(fetchFinishedRunnable);
        }
    }

    /**
     * Called when fetches have completed in order to process the state change and notify listeners.
     */
    protected void onProcessFetchFinished() {
        listeners.map(new Delegate<DataHubListener<Data>>() {
            @Override
            public void execute(DataHubListener<Data> listener) {
                listener.onDataFetchFinished();
            }
        });
    }

    /**
     * Called when results are obtained in order to process the necessary actions.
     *
     * @param dataHubResult The result to process.
     */
    protected final void onResult(final DataHubResult<Data> dataHubResult) {
        synchronized (getStateLock()) {
            process(new Runnable() {
                @Override
                public void run() {
                    onProcessResult(dataHubResult);
                }
            });
        }
    }

    /**
     * Called when results are obtained in order to process the update and notify listeners.
     *
     * @param dataHubResult The result to process.
     */
    protected void onProcessResult(final DataHubResult<Data> dataHubResult) {
        onResultFetched(dataHubResult);

        listeners.map(new Delegate<DataHubListener<Data>>() {
            @Override
            public void execute(DataHubListener<Data> listener) {
                listener.onResultReceived(dataHubResult);
            }
        });

        if (!isFetching()) {
            onFetchFinished();
        }
    }

    /**
     * Called to dispatch an error when this {@link DataHub} is accessed after it has been closed.
     */
    private void processClosedError() {
        onProcessResult(new ClosedErrorResult<Data>(isFetching()));
    }

    /**
     * Dispatches the given processing {@link Runnable} to be executed on the proper threads as defined for this
     * {@link DataHub}.
     *
     * @param runnable The runnable containing the logic to execute.
     */
    private void process(Runnable runnable) {
        if (processingHandler != null) {
            ThreadingUtils.runOnHandler(processingHandler, runnable);
        } else {
            runnable.run();
        }
    }

    //endregion Instance Methods

    //region Overridable Events

    /**
     * Convenience method for doing any processing of fetched results.
     *
     * @param result The result which was fetched.
     */
    protected void onResultFetched(DataHubResult<Data> result) {

    }
    //endregion Overridable Events

    //region Anonymous Classes
    private final Runnable fetchStartedRunnable = new Runnable() {
        @Override
        public void run() {
            onProcessFetchStarted();
        }
    };

    private final Runnable fetchFinishedRunnable = new Runnable() {
        @Override
        public void run() {
            onProcessFetchFinished();
        }
    };
    //endregion Anonymous Classes

    //region Inner Classes
    private static class ClosedErrorResult<T> extends DataHubResult<T> {

        //@formatter:off
        public ClosedErrorResult(boolean isFetching) {
            super(new DataAccessResult<T>(ERROR_CLOSED) {},
                    DataAccess.AccessTypeIds.NONE,
                    isFetching);
        }
        //@formatter:on
    }
    //endregion Inner Classes
}
