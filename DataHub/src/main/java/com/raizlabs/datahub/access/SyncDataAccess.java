package com.raizlabs.datahub.access;

/**
 * A {@link SyncDataAccess} defines a means of obtaining data in a synchronous fashion.
 *
 * @param <Data> {@inheritDoc}
 */
public interface SyncDataAccess<Data> extends DataAccess<Data> {

    /**
     * Obtains the data. This returns an object which contains all of the information about the access of the data.
     *
     * @return A {@link DataAccessResult} containing the result information.
     */
    DataAccessResult<Data> get();
}
