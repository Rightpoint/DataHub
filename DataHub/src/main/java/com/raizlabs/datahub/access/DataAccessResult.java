package com.raizlabs.datahub.access;

import com.raizlabs.datahub.DataHubError;

/**
 * A {@link DataAccessResult} contains results and relevant information about the access of data. Null data is not
 * supported as null is used to represent data as unavailable.
 *
 * @param <Data> The type of data that was accessed.
 */
public class DataAccessResult<Data> {

    /**
     * Builds a new {@link DataAccessResult} representing that the given data was found as a result.
     *
     * @param data The result data.
     * @param <T>  The type of data that was being accessed.
     * @return A {@link DataAccessResult} representing the given result data.
     */
    public static <T> DataAccessResult<T> fromResult(T data) {
        return new DataAccessResult<>(data);
    }

    /**
     * Builds a new {@link DataAccessResult} representing that no data was available.
     *
     * @param <T> The type of data that was being accessed.
     * @return A {@link DataAccessResult} representing that no data was available.
     */
    public static <T> DataAccessResult<T> fromUnavailable() {
        return fromResult(null);
    }

    /**
     * Builds a new {@link DataAccessResult} representing that the given error was returned when attempting to access
     * the data.
     *
     * @param error The error that was returned.
     * @param <T>   The type of data that was being accessed.
     * @return A {@link DataAccessResult} representing that the error was returned.
     */
    public static <T> DataAccessResult<T> fromError(DataHubError error) {
        return new DataAccessResult<>(error);
    }

    private DataHubError error;
    private Data data;

    protected DataAccessResult(DataHubError error) {
        this.error = error;
    }

    protected DataAccessResult(Data data) {
        this.data = data;
    }

    /**
     * @return The error returned when attempting to access the data, or null if no error was given.
     */
    public DataHubError getError() {
        return error;
    }

    /**
     * @return The accessed data or null if no data was available or an error was given.
     */
    public Data getData() {
        return data;
    }

    /**
     * @return True if there was no error and data was available.
     */
    public boolean hasData() {
        return (getError() == null) && (getData() != null);
    }
}
