package com.raizlabs.datahub;

public interface ErrorInfo {

    DCError getError();

    int getAccessTypeId();

    boolean isFetching();
}
