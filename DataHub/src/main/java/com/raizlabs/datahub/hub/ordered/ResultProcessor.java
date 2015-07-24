package com.raizlabs.datahub.hub.ordered;

import com.raizlabs.datahub.access.DataAccessResult;
import com.raizlabs.datahub.access.AsynchronousDataAccess;

public interface ResultProcessor<T> {
    public void onResult(DataAccessResult<T> result, AsynchronousDataAccess<T> access);
}
