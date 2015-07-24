package com.raizlabs.datahub.hub;

import android.os.Handler;

import com.raizlabs.datahub.DCError;
import com.raizlabs.datahub.access.DataAccessResult;
import com.raizlabs.datahub.util.Delegate;
import com.raizlabs.datahub.util.MappableSet;
import com.raizlabs.datahub.util.ThreadingUtils;

public abstract class DataHub<Data> {

    public static class AccessTypeIds {
        public static final int NONE = 0;
        public static final int MEMORY_DATA = 1000;
        public static final int DISK_DATA = 2000;
        public static final int WEB_DATA = 4000;
    }

    private static final DCError ERROR_CLOSED =
            new DCError("Could not access data because the DataHub is closed", DCError.Types.INVALID_STATE);

    //region Members
    private MappableSet<DataHubListener<Data>> listeners = new MappableSet<>();
    private boolean isClosed;

    private Handler processingHandler;

    public boolean isClosed() {
        synchronized (getStateLock()) {
            return isClosed;
        }
    }
    //endregion Members

    //region Abstract Methods
    protected abstract DataHubResult<Data> doGet();

    protected abstract void doFetch();
    protected abstract void doFetch(int limitId);

    protected abstract void doImportData(Data data);

    protected abstract void doClose();

    public abstract boolean isFetching();
    //endregion Abstract Methods

    //region Methods
    public DataHubResult<Data> get() {
        synchronized (getStateLock()) {
            if (isClosed()) {
                processClosedError();
                return null;
            } else {
                return doGet();
            }
        }
    }

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

    public void importData(Data data) {
        synchronized (getStateLock()) {
            doImportData(data);
        }
    }

    public void close() {
        synchronized (getStateLock()) {
            this.isClosed = true;
            doClose();
        }
    }

    public void addListener(DataHubListener<Data> listener) {
        listeners.add(listener);
    }

    public void removeListener(DataHubListener<Data> listener) {
        listeners.remove(listener);
    }

    public void setProcessingHandler(Handler handler) {
        synchronized (getStateLock()) {
            this.processingHandler = handler;
        }
    }

    protected Object getStateLock() {
        return this;
    }

    protected void onFetchStarted() {
        synchronized (getStateLock()) {
            process(new Runnable() {
                @Override
                public void run() {
                    listeners.map(new Delegate<DataHubListener<Data>>() {
                        @Override
                        public void execute(DataHubListener<Data> listener) {
                            listener.onDataFetchStarted();
                        }
                    });
                }
            });
        }
    }

    protected void onFetchFinished() {
        synchronized (getStateLock()) {
            process(new Runnable() {
                @Override
                public void run() {
                    listeners.map(new Delegate<DataHubListener<Data>>() {
                        @Override
                        public void execute(DataHubListener<Data> listener) {
                            listener.onDataFetchFinished();
                        }
                    });
                }
            });
        }
    }

    protected void processResult(final DataHubResult<Data> dataHubResult) {
        synchronized (getStateLock()) {
            process(new Runnable() {
                @Override
                public void run() {
                    notifyResult(dataHubResult);

                    if (!dataHubResult.isFetching()) {
                        onFetchFinished();
                    }
                }
            });
        }
    }

    private void notifyResult(final DataHubResult<Data> result) {
        onResultFetched(result);

        listeners.map(new Delegate<DataHubListener<Data>>() {
            @Override
            public void execute(DataHubListener<Data> listener) {
                listener.onResultReceived(result);
            }
        });
    }


    private void processClosedError() {
        synchronized (getStateLock()) {
            process(new Runnable() {
                @Override
                public void run() {
                    notifyResult(new ClosedErrorResult<Data>(isFetching()));
                }
            });
        }
    }

    private void process(Runnable runnable) {
        if (processingHandler != null) {
            ThreadingUtils.runOnHandler(processingHandler, runnable);
        } else {
            runnable.run();
        }
    }

    private static class ClosedErrorResult<T> extends DataHubResult<T> {

        public ClosedErrorResult(boolean isFetching) {
            super(new DataAccessResult<T>(ERROR_CLOSED){}, AccessTypeIds.NONE, isFetching);
        }
    }

    //endregion Methods

    //region Overridable Events
    protected void onResultFetched(DataHubResult<Data> result) {

    }
    //endregion Overridable Events
}
