package com.raizlabs.datacontroller.controller;

import android.os.Handler;

import com.raizlabs.datacontroller.DCError;
import com.raizlabs.datacontroller.DataResult;
import com.raizlabs.datacontroller.ErrorInfo;
import com.raizlabs.datacontroller.imported.coreutils.Delegate;
import com.raizlabs.datacontroller.imported.coreutils.MappableSet;
import com.raizlabs.datacontroller.util.ThreadingUtils;

public abstract class DataController<Data> {

    public static class DataSourceIds {
        public static final int MEMORY_DATA = 1000;
        public static final int DISK_DATA = 2000;
        public static final int WEB_DATA = 4000;
    }

    private static final DCError ERROR_CLOSED =
            new DCError("Could not access data because the DataController is closed", DCError.Types.INVALID_STATE);

    //region Members
    private MappableSet<DataControllerListener<Data>> listeners = new MappableSet<>();
    private boolean isClosed;

    private Handler resultHandler;

    public boolean isClosed() {
        synchronized (getDataLock()) {
            return isClosed;
        }
    }
    //endregion Members

    //region Abstract Methods
    protected abstract ControllerResult<Data> doGet();

    protected abstract void doFetch();

    protected abstract void doImportData(Data data);

    protected abstract void doClose();

    protected abstract boolean isFetching();
    //endregion Abstract Methods


    // TODO Should this be `? extends Data` ? More flexible but makes interfaces more complicated

    //region Methods
    public ControllerResult<Data> get() {
        synchronized (getDataLock()) {
            if (isClosed()) {
                processClosedError();
                return null;
            } else {
                return doGet();
            }
        }
    }

    public void fetch() {
        synchronized (getDataLock()) {
            if (isClosed()) {
                processClosedError();
            } else if (!isFetching()) {
                onFetchStarted();
                doFetch();
            }
        }
    }

    public void importData(Data data) {
        synchronized (getDataLock()) {
            doImportData(data);
        }
    }

    public void close() {
        synchronized (getDataLock()) {
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

    public void setResultHandler(Handler handler) {
        synchronized (getDataLock()) {
            this.resultHandler = handler;
        }
    }

    protected Object getDataLock() {
        return this;
    }

    protected void onFetchStarted() {
        synchronized (getDataLock()) {
            dispatchResult(new Runnable() {
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
        synchronized (getDataLock()) {
            dispatchResult(new Runnable() {
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

    protected void processResult(final ControllerResult<Data> controllerResult) {
        synchronized (getDataLock()) {
            dispatchResult(new Runnable() {
                @Override
                public void run() {
                    if (controllerResult.hasValidData()) {
                        final DataResult<Data> dataResult = new DataResult<>(
                                controllerResult.getData(),
                                controllerResult.getSourceId(),
                                controllerResult.isFetching());

                        notifyDataFetched(dataResult);
                    } else if (controllerResult.getError() != null) {
                        final ErrorInfo errorInfo = new ErrorInfo(
                                controllerResult.getError(),
                                controllerResult.getSourceId(),
                                controllerResult.isFetching());

                        notifyError(errorInfo);
                    }

                    if (!controllerResult.isFetching()) {
                        onFetchFinished();
                    }
                }
            });
        }
    }

    private void notifyDataFetched(final DataResult<Data> dataResult) {
        onDataFetched(dataResult);

        listeners.map(new Delegate<DataControllerListener<Data>>() {
            @Override
            public void execute(DataControllerListener<Data> listener) {
                listener.onDataReceived(dataResult);
            }
        });
    }

    private void notifyError(final ErrorInfo errorInfo) {
        onError(errorInfo);

        listeners.map(new Delegate<DataControllerListener<Data>>() {
            @Override
            public void execute(DataControllerListener<Data> listener) {
                listener.onErrorReceived(errorInfo);
            }
        });
    }


    private void processClosedError() {
        synchronized (getDataLock()) {
            dispatchResult(new Runnable() {
                @Override
                public void run() {
                    notifyError(new ErrorInfo(ERROR_CLOSED, ErrorInfo.ACCESS_TYPE_NONE, isFetching()));
                }
            });
        }
    }

    private void dispatchResult(Runnable runnable) {
        if (resultHandler != null) {
            ThreadingUtils.runOnHandler(resultHandler, runnable);
        } else {
            runnable.run();
        }
    }
    //endregion Methods

    //region Overridable Events
    protected void onDataFetched(DataResult<Data> dataResult) {

    }

    protected void onError(ErrorInfo errorInfo) {

    }
    //endregion Overridable Events
}
