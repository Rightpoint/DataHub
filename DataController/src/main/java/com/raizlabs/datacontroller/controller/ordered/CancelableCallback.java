package com.raizlabs.datacontroller.controller.ordered;

import com.raizlabs.datacontroller.access.DataAccessResult;
import com.raizlabs.datacontroller.access.AsynchronousDataAccess;

import java.lang.ref.WeakReference;

public class CancelableCallback<T> implements AsynchronousDataAccess.Callback<T> {

    private WeakReference<ResultProcessor<T>> processorReference;

    public CancelableCallback(ResultProcessor<T> processor) {
        this.processorReference = new WeakReference<>(processor);
    }

    public void close() {
        this.processorReference = null;
    }

    protected ResultProcessor<T> getProcessor() {
        if (processorReference != null) {
            return processorReference.get();
        } else {
            return null;
        }
    }

    @Override
    public void onResult(DataAccessResult<T> result, AsynchronousDataAccess<T> access) {
        ResultProcessor<T> processor = getProcessor();
        if (processor != null) {
            processor.onResult(result, access);
        }
    }
}
