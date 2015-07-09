package com.raizlabs.datacontroller;

import android.os.Handler;

import com.raizlabs.datacontroller.controller.DataController;
import com.raizlabs.datacontroller.controller.DataControllerListener;
import com.raizlabs.datacontroller.util.ThreadingUtils;

public class DataSource<Data> {

    private DataController<Data> dataController;

    private DataSourceListener<Data> listener;

    private Handler handler;

    /**
     * Constructs a {@link DataSource} which accesses data from the given controller. All callbacks will be dispatched
     * on the UI thread.
     *
     * @param dataController The controller to access data from.
     */
    public DataSource(DataController<Data> dataController) {
        this(dataController, ThreadingUtils.getUIHandler());
    }

    /**
     * Constructs a {@link DataSource} which accesses data from the given controller and dispatches all callbacks via
     * the given {@link Handler}.
     *
     * @param dataController The controller to access data from.
     * @param handler        The handler to dispatch future callbacks to, or null to dispatch them straight from the
     *                       threads the {@link com.raizlabs.datacontroller.controller.DataController} is calling from.
     */
    public DataSource(DataController<Data> dataController, Handler handler) {
        this.dataController = dataController;
        this.handler = handler;
    }

    public ResultInfo<Data> get() {
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
        listener = null;

        if (dataController != null) {
            dataController.removeListener(dataControllerListener);

            if (completeShutdown) {
                dataController.close();
            }
        }
    }

    /**
     * Called to dispatch fetching start indication via the delegate.
     */
    protected void onFetchStarted() {
        dispatchResult(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onDataFetchStarted();
                }
            }
        });
    }

    protected void onFetchFinished() {
        dispatchResult(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onDataFetchFinished();
                }
            }
        });
    }

    /**
     * Called to dispatch loading the data via the delegate.
     */
    protected void onDataLoaded(final ResultInfo<Data> resultInfo) {
        dispatchResult(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onDataReceived(resultInfo);
                }
            }
        });
    }

    /**
     * Called to dispatch showing an error via the delegate.
     */
    protected void onError(final ErrorInfo errorInfo) {
        dispatchResult(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onErrorReceived(errorInfo);
                }
            }
        });
    }

    protected void dispatchResult(Runnable runnable) {
        if (handler != null) {
            ThreadingUtils.runOnHandler(handler, runnable);
        } else {
            runnable.run();
        }
    }

    DataControllerListener<Data> dataControllerListener = new DataControllerListener<Data>() {
        @Override
        public void onDataFetchStarted() {
            DataSource.this.onFetchStarted();
        }

        @Override
        public void onDataFetchFinished() {
            DataSource.this.onFetchFinished();
        }

        @Override
        public void onDataReceived(ResultInfo<Data> resultInfo) {
            DataSource.this.onDataLoaded(resultInfo);
        }

        @Override
        public void onErrorReceived(ErrorInfo errorInfo) {
            DataSource.this.onError(errorInfo);
        }
    };
}
