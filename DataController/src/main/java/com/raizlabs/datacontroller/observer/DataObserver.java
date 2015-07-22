package com.raizlabs.datacontroller.observer;

import android.os.Handler;

import com.raizlabs.datacontroller.DataResult;
import com.raizlabs.datacontroller.ErrorInfo;
import com.raizlabs.datacontroller.controller.DataController;
import com.raizlabs.datacontroller.controller.DataControllerListener;
import com.raizlabs.datacontroller.controller.DataControllerResult;
import com.raizlabs.datacontroller.util.Delegate;
import com.raizlabs.datacontroller.util.MappableSet;
import com.raizlabs.datacontroller.util.ThreadingUtils;

public class DataObserver<Data> {

    private DataController<Data> dataController;

    private MappableSet<DataObserverListener<Data>> listeners;

    private Handler listenerHandler;

    /**
     * Constructs a {@link DataObserver} which accesses data from the given controller. All update callbacks will be
     * dispatched on the UI thread.
     *
     * @param dataController The controller to access data from.
     */
    public DataObserver(DataController<Data> dataController) {
        this(dataController, ThreadingUtils.getUIHandler());
    }

    /**
     * Constructs a {@link DataObserver} which accesses data from the given controller and dispatches all update callbacks
     * via the given {@link Handler}.
     *
     * @param dataController  The controller to access data from.
     * @param listenerHandler The Handler to dispatch future callbacks to, or null to dispatch them straight from the
     *                        threads the {@link com.raizlabs.datacontroller.controller.DataController} is calling from.
     */
    public DataObserver(DataController<Data> dataController, Handler listenerHandler) {
        this.dataController = dataController;
        this.listenerHandler = listenerHandler;
        this.listeners = new MappableSet<>();

        this.dataController.addListener(dataControllerListener);
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

    public DataControllerResult<Data> get() {
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
     * Indicates that this {@link DataObserver} will no longer be used and should clean up any resources associated with
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

    protected Object getStateLock() {
        return this;
    }

    /**
     * Called to dispatch fetching start indication via the delegate.
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
     * Called to dispatch loading the data via the delegate.
     */
    protected void onDataReceived(final DataResult<Data> dataResult) {
        dispatch(new Runnable() {
            @Override
            public void run() {
                synchronized (getStateLock()) {
                    listeners.map(new Delegate<DataObserverListener<Data>>() {
                        @Override
                        public void execute(DataObserverListener<Data> dataDataObserverListener) {
                            dataDataObserverListener.onDataReceived(dataResult);
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
        dispatch(new Runnable() {
            @Override
            public void run() {
                synchronized (getStateLock()) {
                    listeners.map(new Delegate<DataObserverListener<Data>>() {
                        @Override
                        public void execute(DataObserverListener<Data> dataDataObserverListener) {
                            dataDataObserverListener.onErrorReceived(errorInfo);
                        }
                    });
                }
            }
        });
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
            if ((dataController != null) && dataController.isFetching()) {
                listener.onDataFetchStarted();

                DataControllerResult<Data> currentDataControllerResult = dataController.get();
                if (currentDataControllerResult.getError() != null) {
                    listener.onErrorReceived(currentDataControllerResult);
                } else {
                    listener.onDataReceived(currentDataControllerResult);
                }
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

    private DataControllerListener<Data> dataControllerListener = new DataControllerListener<Data>() {
        @Override
        public void onDataFetchStarted() {
            DataObserver.this.onDataFetchStarted();
        }

        @Override
        public void onDataFetchFinished() {
            DataObserver.this.onDataFetchFinished();
        }

        @Override
        public void onDataReceived(DataResult<Data> dataResult) {
            DataObserver.this.onDataReceived(dataResult);
        }

        @Override
        public void onErrorReceived(ErrorInfo errorInfo) {
            DataObserver.this.onErrorReceived(errorInfo);
        }
    };
}
