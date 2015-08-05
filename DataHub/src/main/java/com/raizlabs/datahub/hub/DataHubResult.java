package com.raizlabs.datahub.hub;

import com.raizlabs.datahub.DataHubError;
import com.raizlabs.datahub.DataHubErrorInfo;
import com.raizlabs.datahub.DataResult;
import com.raizlabs.datahub.access.DataAccessResult;

/**
 * A {@link DataHubResult} contains results and relevant information about the access of data from a {@link DataHub}.
 * Null data is not supported as null is used to represent data as unavailable.
 *
 * @param <Data> The type of data that was accessed.
 */
public class DataHubResult<Data> implements DataResult<Data>, DataHubErrorInfo {

    private DataAccessResult<Data> accessResult;
    private int typeId;
    private boolean isFetching;

    /**
     * Builds a new {@link DataHubResult} representing the result from a given {@link DataAccessResult}.
     *
     * @param accessResult The {@link DataAccessResult} containing the result data.
     * @param typeId       The type ID of the access.
     * @param isFetching   True if more data is being fetched.
     */
    public DataHubResult(DataAccessResult<Data> accessResult, int typeId, boolean isFetching) {
        this.accessResult = accessResult;
        this.typeId = typeId;
        this.isFetching = isFetching;
    }

    @Override
    public Data getData() {
        return accessResult.getData();
    }

    @Override
    public int getAccessTypeId() {
        return typeId;
    }

    @Override
    public boolean isFetching() {
        return isFetching;
    }

    @Override
    public DataHubError getError() {
        return accessResult.getError();
    }

    /**
     * @return True if there is an error, false if there is not.
     */
    public boolean hasError() {
        return (getError() != null);
    }

    /**
     * @return True if the result has a data object.
     */
    public boolean hasData() {
        return accessResult.hasData();
    }

    /**
     * @return The {@link DataAccessResult} containing the result.
     */
    public DataAccessResult<Data> getAccessResult() {
        return accessResult;
    }
}
