package com.raizlabs.datahub.observer;

import com.raizlabs.datahub.hub.DataHubResult;

public interface ResultFilter<T> {

    boolean shouldFilter(DataHubResult<? extends T> result);
}
