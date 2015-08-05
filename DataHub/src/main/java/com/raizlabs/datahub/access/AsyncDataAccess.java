package com.raizlabs.datahub.access;

/**
 * An {@link AsyncDataAccess} defines a means of obtaining data in an asynchronous fashion, calling a callback
 * when it has completed.
 *
 * @param <Data> {@inheritDoc}
 */
public interface AsyncDataAccess<Data> extends DataAccess<Data> {

    /**
     * Defines a callback to be invoked when an {@link AsyncDataAccess} completes.
     *
     * @param <Data> The type of data being accessed.
     */
    interface AsyncDataCallback<Data> {

        /**
         * Called when a result has been obtained.
         *
         * @param result A {@link DataAccessResult} containing the result information.
         * @param access The {@link AsyncDataAccess} returning the result.
         */
        void onResult(DataAccessResult<Data> result, AsyncDataAccess<Data> access);
    }

    /**
     * Obtains the data asynchronously. This will call the given callback with all of the information about the access
     * of the data when the execution completes.
     *
     * @param asyncDataCallback A callback to call with results when the execution completes.
     */
    void get(AsyncDataCallback<Data> asyncDataCallback);
}
