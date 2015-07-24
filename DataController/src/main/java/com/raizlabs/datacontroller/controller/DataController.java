package com.raizlabs.datacontroller.controller;

import android.os.Handler;

import com.raizlabs.datacontroller.DCError;
import com.raizlabs.datacontroller.DataResult;
import com.raizlabs.datacontroller.ErrorInfo;
import com.raizlabs.datacontroller.access.DataAccessResult;
import com.raizlabs.datacontroller.util.Delegate;
import com.raizlabs.datacontroller.util.MappableSet;
import com.raizlabs.datacontroller.util.ThreadingUtils;

public abstract class DataController<Data> {

    public static class AccessTypeIds {
        public static final int NONE = 0;
        public static final int MEMORY_DATA = 1000;
        public static final int DISK_DATA = 2000;
        public static final int WEB_DATA = 4000;
    }

    private static final DCError ERROR_CLOSED =
            new DCError("Could not access data because the DataController is closed", DCError.Types.INVALID_STATE);

    //region Members
    private MappableSet<DataControllerListener<Data>> listeners = new MappableSet<>();
    private boolean isClosed;

    private Handler processingHandler;

    public boolean isClosed() {
        synchronized (getStateLock()) {
            return isClosed;
        }
    }
    //endregion Members

    //region Abstract Methods
    protected abstract DataControllerResult<Data> doGet();

    protected abstract void doFetch();
    protected abstract void doFetch(int limitId);

    protected abstract void doImportData(Data data);

    protected abstract void doClose();

    public abstract boolean isFetching();
    //endregion Abstract Methods

    //region Methods
    public DataControllerResult<Data> get() {
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

    public void addListener(DataControllerListener<Data> listener) {
        listeners.add(listener);
    }

    public void removeListener(DataControllerListener<Data> listener) {
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
                    listeners.map(new Delegate<DataControllerListener<Data>>() {
                        @Override
                        public void execute(DataControllerListener<Data> listener) {
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
                    listeners.map(new Delegate<DataControllerListener<Data>>() {
                        @Override
                        public void execute(DataControllerListener<Data> listener) {
                            listener.onDataFetchFinished();
                        }
                    });
                }
            });
        }
    }

    protected void processResult(final DataControllerResult<Data> dataControllerResult) {
        synchronized (getStateLock()) {
            process(new Runnable() {
                @Override
                public void run() {
                    notifyResult(dataControllerResult);

                    if (!dataControllerResult.isFetching()) {
                        onFetchFinished();
                    }
                }
            });
        }
    }

    private void notifyResult(final DataControllerResult<Data> result) {
        onResultFetched(result);

        listeners.map(new Delegate<DataControllerListener<Data>>() {
            @Override
            public void execute(DataControllerListener<Data> listener) {
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

    private static class ClosedErrorResult<T> extends DataControllerResult<T> {

        public ClosedErrorResult(boolean isFetching) {
            super(new DataAccessResult<T>(ERROR_CLOSED){}, AccessTypeIds.NONE, isFetching);
        }
    }

    //endregion Methods

    //region Overridable Events
    protected void onResultFetched(DataControllerResult<Data> result) {

    }
    //endregion Overridable Events
}
