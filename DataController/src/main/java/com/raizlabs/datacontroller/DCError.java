package com.raizlabs.datacontroller;

public class DCError {

    public static class Types {
        public static final int UNDEFINED = 0;
        public static final int DATA_ACCESS = 100;
        public static final int DATA_ACCESS_NOT_FOUND = 405;
        public static final int INVALID_STATE = 500;
    }

    private int errorType;
    private Object tag;
    private Throwable throwable;

    public DCError(String message, int errorType) {
        this(message, errorType, null);
    }

    public DCError(String message, int errorType, Object tag) {
        this.errorType = errorType;
        this.tag = tag;
        this.throwable = new Exception(message);
    }

    public String getMessage() {
        return throwable.getMessage();
    }

    public int getErrorType() {
        return errorType;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Object getTag() {
        return tag;
    }
}
