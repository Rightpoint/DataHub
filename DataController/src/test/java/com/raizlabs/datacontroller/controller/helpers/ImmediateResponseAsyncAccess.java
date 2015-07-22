package com.raizlabs.datacontroller.controller.helpers;

import com.raizlabs.datacontroller.access.DataAccessResult;
import com.raizlabs.datacontroller.access.AsynchronousDataAccess;
import com.raizlabs.datacontroller.utils.OneShotLock;

public class ImmediateResponseAsyncAccess<T> implements AsynchronousDataAccess<T> {

    private OneShotLock completionLock = new OneShotLock();

    private final DataAccessResult<T> result;
    private final int typeId;

    public ImmediateResponseAsyncAccess(DataAccessResult<T> result, int typeId) {
        this.result = result;
        this.typeId = typeId;
    }

    @Override
    public void get(Callback<T> callback) {
        callback.onResult(result, this);
        completionLock.unlock();
    }

    @Override
    public void importData(T t) {

    }

    @Override
    public void close() {

    }

    @Override
    public int getTypeId() {
        return typeId;
    }

    public OneShotLock getCompletionLock() {
        return completionLock;
    }

    public void reset() {
        completionLock = new OneShotLock();
    }
}
