package com.raizlabs.datahub.hub.ordered;

import com.raizlabs.datahub.access.AsyncDataAccess;
import com.raizlabs.datahub.access.DataAccess;
import com.raizlabs.datahub.access.DataAccessResult;
import com.raizlabs.datahub.access.SyncDataAccess;

/**
 * Class which contains some predefined {@link FetchStrategy} implementations for common uses.
 */
public class FetchStrategies {

    /**
     * {@link FetchStrategy} implementation which immediately starts all accesses and waits for the last to respond.
     * This will send updates from each access as they come in, provided that an access higher up the list has not
     * already responded.
     * <p></p>
     * For example, if we have 5 accesses (indexed 0-4) and index 2 responds, we will dispatch the
     * result as long as 3 or 4 have not responded yet, but will ignore the update if they have. This will continue
     * until 4 has responded.
     * <p></p>
     * This is useful if you would like any data as soon as possible for a quick response, but would prefer updates
     * from the "fresher" sources at the end of the list as soon as they are available.
     *
     * @param <T> {@inheritDoc}
     */
    public static class Parallel<T> extends BaseFetchStrategy<T> {

        @Override
        protected void doFetch(int limitId) {
            SyncDataAccess<T> syncAccess = getDataHubDelegate().getSyncAccess();
            if (syncAccess != null && isFetching()) {
                DataAccessResult<T> syncResult = syncAccess.get();
                // If the id matches the limit, we're done
                if (syncAccess.getTypeId() == limitId) {
                    close();
                }

                if (syncResult != null) {
                    getDataHubDelegate().processResult(syncResult, syncAccess);
                }
            }

            // Don't continue if we're already done from any above logic
            if (isFetching()) {
                // Start each access
                for (AsyncDataAccess<T> access : getAsyncDataAccesses()) {
                    access.get(getCurrentCallback());

                    if (access.getTypeId() == limitId) {
                        break;
                    }
                }
            }
        }

        @Override
        public void onResult(DataAccessResult<T> result, AsyncDataAccess<T> access) {
            if (isFetching()) {
                final int index = getAccessIndex(access);
                // Only process the result if the index increased
                if (index > getLastAsyncAccessIndex()) {
                    setLastAsyncAccessIndex(index);
                    // If this was the last one, finish everything
                    if ((access.getTypeId() == getFetchLimitId()) ||
                            (index >= getAsyncDataAccesses().size() - 1)) {
                        close();
                    }

                    processResult(result, access);
                }
            }
        }
    }

    /**
     * {@link FetchStrategy} implementation which queries each access in order until one of the responses is deemed
     * "final" or the last access is queried. This will send updates from each access as they are individually queried.
     * This is useful if you may have some data in the faster accesses that is totally valid and may deem updates from
     * later accesses unnecessary, and would like to avoid querying the later accesses.
     * <p></p>
     * For example, if you have a local database that caches data from an API, you may know that the data in your
     * database already matches your API. Therefore you can use this to determine the data from the database is good
     * enough and avoid calling the API at all.
     * <p></p>
     * This implementation leverages a {@link com.raizlabs.datahub.hub.ordered.FetchStrategies.Serial.DataFinalizer} to
     * determine when data is "final" and we can stop. See
     * {@link com.raizlabs.datahub.hub.ordered.FetchStrategies.Serial.Finalizers} for some provided implementations.
     *
     * @param <T> {@inheritDoc}
     */
    public static class Serial<T> extends BaseFetchStrategy<T> {

        /**
         * Interface for a delegate which determines whether results are "final" and we may stop querying later
         * accesses.
         *
         * @param <T> The type of data being accessed.
         */
        public interface DataFinalizer<T> {
            /**
             * Called to determine if the given result is final and we may stop querying later accesses.
             *
             * @param result The result to be checked.
             * @param access The access that provided the result.
             * @return True if the result is final and we may stop, false if it is not.
             */
            boolean isFinal(DataAccessResult<T> result, DataAccess access);
        }

        private DataFinalizer<T> dataFinalizer;

        /**
         * Creates a new {@link Serial} which uses the given {@link DataFinalizer}.
         *
         * @param finalizer The finalizer to use for determining the final data. See
         *                  {@link FetchStrategies.Serial.Finalizers} for some provided implementations.
         */
        public Serial(DataFinalizer<T> finalizer) {
            this.dataFinalizer = finalizer;
        }

        @Override
        protected void doFetch(int limitId) {
            SyncDataAccess<T> syncAccess = getDataHubDelegate().getSyncAccess();

            // Process the synchronous access
            if (syncAccess != null) {
                DataAccessResult<T> syncResult = syncAccess.get();

                // If the id matches the limit, or the validator marks it as final, we're done
                if ((syncAccess.getTypeId() == limitId) || dataFinalizer.isFinal(syncResult, syncAccess)) {
                    close();
                }

                getDataHubDelegate().processResult(syncResult, syncAccess);
            }

            // Don't continue if we're already done from any above logic
            if (isFetching()) {
                queryNext();
            }
        }

        @Override
        public void onResult(DataAccessResult<T> result, AsyncDataAccess<T> access) {
            if (isFetching()) {
                final int index = getAccessIndex(access);

                // If this is the last access, last access allowed by the limit, or the validator says it's done, terminate
                if ((index >= getAsyncDataAccesses().size() - 1) ||
                        (access.getTypeId() == getFetchLimitId()) ||
                        dataFinalizer.isFinal(result, access)) {
                    close();
                }

                processResult(result, access);

                setLastAsyncAccessIndex(index);
            }

            if (isFetching()) {
                queryNext();
            }
        }

        /**
         * Queries the next access based on which index responded last.
         */
        protected void queryNext() {
            final int nextIndex = getLastAsyncAccessIndex() + 1;

            // Ran off the end? Shouldn't get here...close!
            if (nextIndex >= getAsyncDataAccesses().size()) {
                close();
            }

            AsyncDataAccess<T> access = getAsyncDataAccesses().get(nextIndex);
            access.get(getCurrentCallback());
        }

        /**
         * Class of existing implementations of {@link DataFinalizer}.
         */
        public static class Finalizers {
            /**
             * Creates a {@link DataFinalizer} which indicates everything is final.
             *
             * @param <T> The type of data being assessed.
             * @return The created {@link DataFinalizer}.
             */
            public static <T> DataFinalizer<T> newAny() {
                return new DataFinalizer<T>() {
                    @Override
                    public boolean isFinal(DataAccessResult<T> result, DataAccess access) {
                        return true;
                    }
                };
            }

            /**
             * Creates a {@link DataFinalizer} which indicates nothing is final. This essentially forces all accesses
             * to be queried in order until we reach the end.
             *
             * @param <T> The type of data being assessed.
             * @return The created {@link DataFinalizer}.
             */
            public static <T> DataFinalizer<T> newNone() {
                return new DataFinalizer<T>() {
                    @Override
                    public boolean isFinal(DataAccessResult<T> result, DataAccess access) {
                        return false;
                    }
                };
            }

            /**
             * Creates a {@link DataFinalizer} which indicates that a result is final if it has data as determined by
             * {@link DataAccessResult#hasData()}.
             *
             * @param <T> The type of data being assessed.
             * @return The created {@link DataFinalizer}.
             */
            public static <T> DataFinalizer<T> newAnyData() {
                return new DataFinalizer<T>() {
                    @Override
                    public boolean isFinal(DataAccessResult<T> result, DataAccess access) {
                        return result.hasData();
                    }
                };
            }

            /**
             * Creates a {@link DataFinalizer} which indicates that a result is final if the data or the error are
             * non-null.
             *
             * @param <T> The type of data being assessed.
             * @return The created {@link DataFinalizer}.
             */
            public static <T> DataFinalizer<T> newDataOrError() {
                return new DataFinalizer<T>() {
                    @Override
                    public boolean isFinal(DataAccessResult<T> result, DataAccess access) {
                        return result.getData() != null || result.getError() != null;
                    }
                };
            }
        }
    }
}
