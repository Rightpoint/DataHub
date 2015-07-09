package com.raizlabs.datacontroller;

import android.support.annotation.IntDef;

public class DCError extends java.lang.Error {

    //@formatter:off
    @IntDef({
            DATA_ACCESS,
            INVALID_STATE
    })
    public @interface ErrorType { }
    //@formatter:on

    public static final int DATA_ACCESS = 100;
    public static final int INVALID_STATE = 400;

    private @ErrorType int errorType;

    public DCError(String message, @ErrorType int errorType) {
        super(message);
        this.errorType = errorType;
    }

    public int getErrorType() {
        return errorType;
    }
}
