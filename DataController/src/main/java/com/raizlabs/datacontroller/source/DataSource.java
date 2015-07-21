package com.raizlabs.datacontroller.source;

import android.os.Handler;

import com.raizlabs.datacontroller.DataResult;
import com.raizlabs.datacontroller.ErrorInfo;
import com.raizlabs.datacontroller.controller.ControllerResult;
import com.raizlabs.datacontroller.controller.DataController;
import com.raizlabs.datacontroller.controller.DataControllerListener;
import com.raizlabs.datacontroller.imported.coreutils.Delegate;
import com.raizlabs.datacontroller.imported.coreutils.MappableSet;
import com.raizlabs.datacontroller.util.ThreadingUtils;

public class DataSource<Data> {

    private DataController<Data> dataController;

    private MappableSet<DataSourceListener<Data>> listeners;

    private Handler handler;

    /**
     * Constructs a {@link DataSource} which accesses data from the given controller. All update callbacks will be
     * dispatched on the UI thread.
     *
     * @param dataController The controller to access data from.
     */
    public DataSource(DataController<Data> dataController) {
        this(dataController, ThreadingUtils.getUIHandler());
    }

    /**
     * Constructs a {@link DataSource} which accesses data from the given controller and dispatches all update callbacks
     * via the given {@link Handler}.
     *
     * @param dataController The controller to access data from.
     * @param handler        The handler to dispatch future callbacks to, or null to dispatch them straight from the
     *                       threads the {@link com.raizlabs.datacontroller.controller.DataController} is calling from.
     */
    public DataSource(DataController<Data> dataController, Handler handler) {
        this.dataController = dataController;
        this.handler = handler;
        this.listeners = new MappableSet<>();

        this.dataController.addListener(dataControllerListener);
    }

    public void addListener(final DataSourceListener<Data> listener) {
        dispatchResult(new Runnable() {
            @Override
            public void run() {
                synchronized (getStatusLock()) {
                    if (!listeners.contains(listener)) {
                        listeners.add(listener);
                        initializeListener(listener);
                    }
                }
            }
        });

    }

    public boolean removeListener(DataSourceListener<Data> listener) {
        return listeners.remove(listener);
    }

    public ControllerResult<Data> get() {
        if (dataController != null) {
            return dataController.get();
        } else {
            return null;
        }
    }

    public void fetch() {
        if (dataController != null) {
            dataController.fetch();
        }
    }

    public void fetch(int limitId) {
        if (dataController != null) {
            dataController.fetch(limitId);
        }
    }

    /**
     * Indicates that this {@link DataSource} will no longer be used and should clean up any resources associated with
     * it.
     *
     * @param completeShutdown If set to <code>true</code> all the attached listeners to the {@link DataController} will
     *                         be cleared and the async requests, if any, will be interrupted/cancelled. Else if set to
     *                         <code>false</code> only this listener will be detached from the associated {@link
     *                         DataController} and the existing async data requests, if any, will continue to stay
     *                         alive.
     */
    public void close(boolean completeShutdown) {

        listeners.clear();

        if (dataController != null) {
            dataController.removeListener(dataControllerListener);

            if (completeShutdown) {
                dataController.close();
            }

            dataController = null;
        }
    }

    protected Object getStatusLock() {
        return this;
    }

    /**
     * Called to dispatch fetching start indication via the delegate.
     */
    protected void onDataFetchStarted() {
        dispatchResult(new Runnable() {
            @Override
            public void run() {
                synchronized (getStatusLock()) {
                    listeners.map(new Delegate<DataSourceListener<Data>>() {
                        @Override
                        public void execute(DataSourceListener<Data> dataDataSourceListener) {
                            dataDataSourceListener.onDataFetchStarted();
                        }
                    });
                }
            }
        });
    }

    protected void onDataFetchFinished() {
        dispatchResult(new Runnable() {
            @Override
            public void run() {
                synchronized (getStatusLock()) {
                    listeners.map(new Delegate<DataSourceListener<Data>>() {
                        @Override
                        public void execute(DataSourceListener<Data> dataDataSourceListener) {
                            dataDataSourceListener.onDataFetchFinished();
                        }
                    });
                }
            }
        });
    }

    /**
     * Called to dispatch loading the data via the delegate.
     */
    protected void onDataReceived(final DataResult<Data> dataResult) {
        dispatchResult(new Runnable() {
            @Override
            public void run() {
                synchronized (getStatusLock()) {
                    listeners.map(new Delegate<DataSourceListener<Data>>() {
                        @Override
                        public void execute(DataSourceListener<Data> dataDataSourceListener) {
                            dataDataSourceListener.onDataReceived(dataResult);
                        }
                    });
                }
            }
        });
    }

    /**
     * Called to dispatch showing an error via the delegate.
     */
    protected void onErrorReceived(final ErrorInfo errorInfo) {
        dispatchResult(new Runnable() {
            @Override
            public void run() {
                synchronized (getStatusLock()) {
                    listeners.map(new Delegate<DataSourceListener<Data>>() {
                        @Override
                        public void execute(DataSourceListener<Data> dataDataSourceListener) {
                            dataDataSourceListener.onErrorReceived(errorInfo);
                        }
                    });
                }
            }
        });
    }

    protected void initializeListener(final DataSourceListener<Data> listener) {
        dispatchResult(new Runnable() {
            @Override
            public void run() {
                doInitializeListener(listener);
            }
        });
    }

    private void doInitializeListener(DataSourceListener<Data> listener) {
        synchronized (getStatusLock()) {
            if ((dataController != null) && dataController.isFetching()) {
                listener.onDataFetchStarted();

                ControllerResult<Data> currentControllerResult = dataController.get();
                if (currentControllerResult.getError() != null) {
                    listener.onErrorReceived(currentControllerResult);
                } else {
                    listener.onDataReceived(currentControllerResult);
                }
            }
        }
    }

    protected void dispatchResult(Runnable runnable) {
        synchronized (getStatusLock()) {
            if (handler != null) {
                ThreadingUtils.runOnHandler(handler, runnable);
            } else {
                runnable.run();
            }
        }
    }

    private DataControllerListener<Data> dataControllerListener = new DataControllerListener<Data>() {
        @Override
        public void onDataFetchStarted() {
            DataSource.this.onDataFetchStarted();
        }

        @Override
        public void onDataFetchFinished() {
            DataSource.this.onDataFetchFinished();
        }

        @Override
        public void onDataReceived(DataResult<Data> dataResult) {
            DataSource.this.onDataReceived(dataResult);
        }

        @Override
        public void onErrorReceived(ErrorInfo errorInfo) {
            DataSource.this.onErrorReceived(errorInfo);
        }
    };
}
