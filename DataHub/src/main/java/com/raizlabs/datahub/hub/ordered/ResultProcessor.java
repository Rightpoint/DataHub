package com.raizlabs.datahub.hub.ordered;

import com.raizlabs.datahub.access.AsyncDataAccess;
import com.raizlabs.datahub.access.DataAccessResult;

/**
 * Defines a mechanism for processing {@link DataAccessResult}s.
 *
 * @param <T> The type of data being processed.
 */
public interface ResultProcessor<T> {
    /**
     * Called to process a result.
     *
     * @param result The result.
     * @param access The access which provided the result.
     */
    void onResult(DataAccessResult<T> result, AsyncDataAccess<T> access);
}
