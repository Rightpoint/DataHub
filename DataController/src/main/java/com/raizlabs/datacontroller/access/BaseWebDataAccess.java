package com.raizlabs.datacontroller.access;

import android.util.Log;

import com.raizlabs.datacontroller.ErrorInfo;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseWebDataAccess<Data> implements WebDataAccess<Data> {

    private Request currentRequest = new Request();

    @Override
    public final void getData(WebDataAccessListener<Data> listener) {
        synchronized (this) {
            Log.d("MERV", "BaseWebDataAccess isFetching:" + currentRequest.isFetching);
            if(!currentRequest.isFetching){
                currentRequest.isFetching = true;
                requestData(currentRequest);
            }
            currentRequest.addListener(listener);
        }
    }

    @Override
    public final void close() {
        //Reset the request so that it can be used again.
        currentRequest.cancel();
        closeRequest();
    }

    protected abstract void requestData(WebDataAccessListener<Data> listener);

    protected abstract void closeRequest();

    /**
     * Helper class which acts as a "proxy" listener. Manages a set of other listeners which it propagates events to.
     */

    private class Request implements WebDataAccessListener<Data> {

        private boolean isFetching = false;

        private Set<WebDataAccessListener<Data>> listeners = new HashSet<>();

        @Override
        public void onDataReceived(Data data) {
            synchronized (this) {
                isFetching = false;
                for (WebDataAccessListener<Data> listener : listeners) {
                    listener.onDataReceived(data);
                }
            }
        }

        @Override
        public void onErrorReceived(ErrorInfo errorInfo) {
            synchronized (this) {
                isFetching = errorInfo.isFreshDataIncoming();
                for (WebDataAccessListener<Data> listener : listeners) {
                    listener.onErrorReceived(errorInfo);
                }
            }
        }

        public void addListener(WebDataAccessListener<Data> listener) {
            listeners.add(listener);
        }

        public void cancel() {
            synchronized (this) {
                isFetching = false;
                listeners.clear();
            }
        }
    }
}
