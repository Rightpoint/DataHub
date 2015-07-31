package com.raizlabs.datahub.hub.ordered;

import com.raizlabs.datahub.access.AsynchronousDataAccess;
import com.raizlabs.datahub.access.DataAccessResult;

public interface ResultProcessor<T> {
    public void onResult(DataAccessResult<T> result, AsynchronousDataAccess<T> access);
}
