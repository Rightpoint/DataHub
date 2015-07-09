package com.raizlabs.datacontroller;

public class ErrorInfo {

    public static final int ACCESS_TYPE_NONE = -1;

    private final DCError error;
    private final int dataAccessType;
    private final boolean isUpdatePending;

    public ErrorInfo(DCError error, int dataAccessType, boolean isUpdatePending) {
        this.error = error;

        this.dataAccessType = dataAccessType;
        this.isUpdatePending = isUpdatePending;
    }

    public DCError getError() {
        return error;
    }

    public int getDataAccessType() {
        return dataAccessType;
    }

    public boolean isUpdatePending() {
        return isUpdatePending;
    }
}
