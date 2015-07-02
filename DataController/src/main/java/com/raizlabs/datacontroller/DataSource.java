package com.raizlabs.datacontroller;

import android.os.Handler;

import com.raizlabs.datacontroller.controller.DataController;
import com.raizlabs.datacontroller.controller.DataControllerListener;
import com.raizlabs.datacontroller.util.ThreadingUtils;

public class DataSource<Data> {

    private DataController<?, Data> dataController;

    private DataSourceListener<Data> listener;

    private Handler handler;

    DataControllerListener<Data> dataControllerListener = new DataControllerListener<Data>() {
        @Override
        public void onDataReceived(ResultInfo<Data> resultInfo) {
            loadData(resultInfo);
        }

        @Override
        public void onErrorReceived(ErrorInfo errorInfo) {
            showError(errorInfo);
        }
    };

    /**
     * Constructs a {@link DataSource} which accesses data from the given controller and passes UI operations to the
     * given delegate. By default, all the callbacks will be directed to UI handler.
     *
     * @param dataController The controller to access data from.
     */
    public DataSource(DataController<?, Data> dataController) {
        this(dataController, ThreadingUtils.getUIHandler());
    }

    /**
     * Constructs a {@link DataSource} which accesses data from the given controller and passes UI operations to the
     * given delegate. Set a custom handler that already has an associated {@link android.os.Looper}..
     *
     * @param dataController The controller to access data from.
     * @param handler        The handler to handle future callbacks from.
     */
    public DataSource(DataController<?, Data> dataController, Handler handler) {
        this.dataController = dataController;
        this.handler = handler;
    }

    /**
     * Places a request for data fetch (based on FetchType) and registers the listener to receive fetch results. This
     * does not guarantee a refetch if one is already underway.
     *
     * @param listener  The listener to pass UI operations to.
     * @param fetchType Type of fetch requested.
     */
    public void fetch(DataSourceListener<Data> listener, DataController.FetchType fetchType) {
        this.listener = listener;
        showLoading();
        dataController.addListener(dataControllerListener);
        dataController.fetch(fetchType);
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
        if(completeShutdown) {
            dataController.close();
        } else {
            dataController.removeListener(dataControllerListener);
        }
    }

    /**
     * Called to dispatch fetching start indication via the delegate.
     */
    private void showLoading() {
        ThreadingUtils.runOnHandler(new Runnable() {
            @Override
            public void run() {
                if(listener != null) {
                    listener.onDataFetching();
                }
            }
        }, handler);
    }

    /**
     * Called to dispatch loading the data via the delegate.
     */
    private void loadData(final ResultInfo<Data> resultInfo) {
        ThreadingUtils.runOnHandler(new Runnable() {
            @Override
            public void run() {
                if(listener != null) {
                    listener.onDataReceived(resultInfo);
                }
            }
        }, handler);
    }

    /**
     * Called to dispatch showing an error via the delegate.
     */
    private void showError(final ErrorInfo errorInfo) {
        ThreadingUtils.runOnHandler(new Runnable() {
            @Override
            public void run() {
                if(listener != null) {
                    listener.onErrorReceived(errorInfo);
                }
            }
        }, handler);
    }
}
