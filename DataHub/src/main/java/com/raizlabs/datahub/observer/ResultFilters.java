package com.raizlabs.datahub.observer;

import com.raizlabs.datahub.hub.DataHubResult;

public class ResultFilters {

    public static final ResultFilter<Object> NULL_DATA = new ResultFilter<Object>() {
        @Override
        public boolean shouldFilter(DataHubResult<?> result) {
            return (result.getData() == null);
        }
    };
}
