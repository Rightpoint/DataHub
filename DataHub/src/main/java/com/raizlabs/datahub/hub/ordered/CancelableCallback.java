package com.raizlabs.datahub.hub.ordered;

import com.raizlabs.datahub.access.AsyncDataAccess;
import com.raizlabs.datahub.access.DataAccessResult;

import java.lang.ref.WeakReference;

/**
 * Class which acts as a {@link AsyncDataAccess.AsyncDataCallback} and forwards callbacks to a {@link ResultProcessor}.
 * This class weakly references its {@link ResultProcessor} and may be cancelled via a call to {@link #cancel()}. If
 * the weak reference dies or this callback has been cancelled, future results will be ignored.
 *
 * @param <T> {@inheritDoc}
 */
public class CancelableCallback<T> implements AsyncDataAccess.AsyncDataCallback<T> {

    private WeakReference<ResultProcessor<T>> processorReference;

    public CancelableCallback(ResultProcessor<T> processor) {
        this.processorReference = new WeakReference<>(processor);
    }

    public void cancel() {
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
    public void onResult(DataAccessResult<T> result, AsyncDataAccess<T> access) {
        ResultProcessor<T> processor = getProcessor();
        if (processor != null) {
            processor.onResult(result, access);
        }
    }
}
