package com.raizlabs.datahub;

/**
 * A {@link DataHubErrorInfo} provides the information about a particular error that occurred within the context of a
 * {@link com.raizlabs.datahub.hub.DataHub}.
 */
public interface DataHubErrorInfo {

    /**
     * @return The contained error.
     */
    DataHubError getError();

    /**
     * @return The type ID of the access that caused or generated this error.
     */
    int getAccessTypeId();

    /**
     * @return True if another result is still being fetched by the {@link com.raizlabs.datahub.hub.DataHub}.
     */
    boolean isFetching();
}
