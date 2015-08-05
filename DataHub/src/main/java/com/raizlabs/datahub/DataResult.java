package com.raizlabs.datahub;

/**
 * A {@link DataResult} represents a result which specifically contains a piece of data.
 *
 * @param <Data>
 */
public interface DataResult<Data> {

    /**
     * @return The data of the result.
     */
    Data getData();

    /**
     * @return The type ID of the access that was used to obtain this result.
     */
    int getAccessTypeId();

    /**
     * @return True if more data is still being fetched.
     */
    boolean isFetching();
}
