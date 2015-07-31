package com.raizlabs.datahub.hub.helpers;

import com.raizlabs.datahub.access.AsynchronousDataAccess;
import com.raizlabs.datahub.access.DataAccessResult;
import com.raizlabs.datahub.utils.OneShotLock;

public class WaitForLockAsyncAccess<T> implements AsynchronousDataAccess<T> {

    private OneShotLock completionLock = new OneShotLock();

    private final DataAccessResult<T> result;
    private final OneShotLock startLock;

    private final int typeId;

    public WaitForLockAsyncAccess(DataAccessResult<T> result, OneShotLock lock, int typeId) {
        this.result = result;
        this.startLock = lock;
        this.typeId = typeId;
    }

    @Override
    public void get(final Callback<T> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startLock.waitUntilUnlocked();
                callback.onResult(result, WaitForLockAsyncAccess.this);
                completionLock.unlock();
            }
        }).start();
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
