package com.raizlabs.datahub;

/**
 * A class which represents an error from a {@link com.raizlabs.datahub.hub.DataHub}.
 */
public class DataHubError {

    /**
     * Class of predefined constants for standard error types.
     */
    public static class Types {
        /**
         * Indicates that an undefined error occurred.
         */
        public static final int UNDEFINED = 0;
        /**
         * Indicates a general error inside a {@link com.raizlabs.datahub.access.DataAccess}.
         */
        public static final int DATA_ACCESS = 100;
        /**
         * Indicates that an error occurred because a particular {@link com.raizlabs.datahub.access.DataAccess} could
         * not be found.
         */
        public static final int DATA_ACCESS_NOT_FOUND = 405;
        /**
         * Indicates that an error occurred because something was in an invalid state.
         */
        public static final int INVALID_STATE = 500;
    }

    private int errorType;
    private Object tag;
    private Throwable throwable;

    /**
     * Creates a new error with the given message and error type.
     *
     * @param message   The message about the error.
     * @param errorType The type of error that was caused.
     * @see {@link com.raizlabs.datahub.DataHubError.Types} for predefined type constants.
     */
    public DataHubError(String message, int errorType) {
        this(message, errorType, null);
    }

    /**
     * Creates a new error with the given message and error type.
     *
     * @param message   The message about the error.
     * @param errorType An integer representing type of error that was caused. This may be one of the values in
     *                  {@link com.raizlabs.datahub.DataHubError.Types} or a user defined value.
     * @param tag       An optional object which may be stored to be retrieved from this error. This may include any
     *                  metadata or anything similar.
     * @see {@link com.raizlabs.datahub.DataHubError.Types} for predefined type constants.
     */
    public DataHubError(String message, int errorType, Object tag) {
        this.errorType = errorType;
        this.tag = tag;
        this.throwable = new Exception(message);
    }

    /**
     * @return The message associated with this error.
     */
    public String getMessage() {
        return throwable.getMessage();
    }

    /**
     * Returns the error type associated with this error. This may be one of the values in
     * {@link com.raizlabs.datahub.DataHubError.Types} or a user defined value.
     *
     * @return The error type associated with this error.
     */
    public int getErrorType() {
        return errorType;
    }

    /**
     * @return A throwable which contains information about the source of the error.
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * @return The user defined tag that was included with this object, or null if none exists.
     */
    public Object getTag() {
        return tag;
    }
}
