package com.raizlabs.datacontroller.controller.ordered;

import com.raizlabs.datacontroller.DataAccessResult;
import com.raizlabs.datacontroller.access.AsynchronousDataAccess;

public interface ResultProcessor<T> {
    public void onResult(DataAccessResult<T> result, AsynchronousDataAccess<T> access);
}
