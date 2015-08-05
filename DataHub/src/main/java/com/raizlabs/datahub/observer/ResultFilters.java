package com.raizlabs.datahub.observer;

import com.raizlabs.datahub.hub.DataHubResult;

/**
 * Class of provided {@link ResultFilter} implementations.
 */
public class ResultFilters {

    /**
     * Filter which removes any results whose data is null.
     */
    public static final ResultFilter<Object> NULL_DATA = new ResultFilter<Object>() {
        @Override
        public boolean shouldFilter(DataHubResult<?> result) {
            return (result.getData() == null);
        }
    };
}
