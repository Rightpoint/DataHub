package com.raizlabs.datahub.observer;

import com.raizlabs.datahub.hub.DataHubResult;

/**
 * An interface for a filter which may be run across {@link DataHubResult}.
 *
 * @param <T> The type of data for the results being filtered.
 */
public interface ResultFilter<T> {

    /**
     * Called to check if the given result should be filtered.
     *
     * @param result The result to check.
     * @return True to filter out the result, false to leave it.
     */
    boolean shouldFilter(DataHubResult<? extends T> result);
}
