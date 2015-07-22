package com.raizlabs.datacontroller;

public interface ErrorInfo {

    DCError getError();

    int getAccessTypeId();

    boolean isFetching();
}
