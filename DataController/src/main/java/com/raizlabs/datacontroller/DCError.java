package com.raizlabs.datacontroller;

public class DCError extends java.lang.Error {

    public static class Types {
        public static final int UNDEFINED = 0;
        public static final int DATA_ACCESS = 100;
        public static final int DATA_UNAVAILABLE = 404;
        public static final int INVALID_STATE = 500;
    }

    private int errorType;
    private Object tag;

    public static DCError fromUnavailable() {
        return fromUnavailable(null);
    }

    public static DCError fromUnavailable(Object tag) {
        return new DCError("Data Unavailable", Types.DATA_UNAVAILABLE, tag);
    }

    public DCError(String message, int errorType) {
        this(message, errorType, null);
    }

    public DCError(String message, int errorType, Object tag) {
        super(message);
        this.errorType = errorType;
        this.tag = tag;
    }

    public int getErrorType() {
        return errorType;
    }

    public Object getTag() {
        return tag;
    }
}
