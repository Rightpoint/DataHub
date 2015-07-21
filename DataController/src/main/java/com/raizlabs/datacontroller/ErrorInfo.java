package com.raizlabs.datacontroller;

public interface ErrorInfo {

    public static final int ACCESS_TYPE_NONE = -1;

    public DCError getError();

    public int getDataSourceId();

    public boolean isUpdatePending();
}
