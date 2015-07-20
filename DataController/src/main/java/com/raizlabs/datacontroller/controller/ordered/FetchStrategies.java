package com.raizlabs.datacontroller.controller.ordered;

import com.raizlabs.datacontroller.access.AsynchronousDataAccess;
import com.raizlabs.datacontroller.access.DataAccess;
import com.raizlabs.datacontroller.access.DataAccessResult;
import com.raizlabs.datacontroller.access.SynchronousDataAccess;

public class FetchStrategies {

    public static class Parallel<T> extends BaseFetchStrategy<T> {

        @Override
        public void onResult(DataAccessResult<T> result, AsynchronousDataAccess<T> access) {
            if (isPending()) {
                final int index = getAccessIndex(access);
                // Only process the result if the index increased
                if (index > getLastAccessIndex()) {
                    setLastAccessIndex(index);
                    // If this was the last one, finish everything
                    if ((access.getSourceId() == getFetchLimitId()) ||
                            (index >= getAsyncDataAccesses().size() - 1)) {
                        close();
                    }

                    processResult(result, access);
                }
            }
        }

        @Override
        protected void doFetch(int limitId) {

            SynchronousDataAccess<T> syncAccess = getDataController().getSyncDataAccess();
            if (syncAccess != null && isPending()) {
                DataAccessResult<T> syncResult = syncAccess.get();

                // If the id matches the limit, we're done
                if (syncAccess.getSourceId() == limitId) {
                    close();
                }

                if (syncResult != null) {
                    getDataController().processResult(syncResult, syncAccess);
                }
            }

            // Don't continue if we're already done from any above logic
            if (isPending()) {
                // Start each access
                // Can't do both operations simultaneously in case accesses return in line.
                for (AsynchronousDataAccess<T> access : getAsyncDataAccesses()) {
                    access.get(getCurrentCallback());

                    if (access.getSourceId() == limitId) {
                        break;
                    }
                }
            }
        }
    }

    public static class Serial<T> extends BaseFetchStrategy<T> {

        public interface DataValidator<T> {
            public boolean isFinal(DataAccessResult<T> result, DataAccess access);
        }

        private DataValidator<T> dataValidator;

        public Serial(DataValidator<T> validator) {
            this.dataValidator = validator;
        }

        @Override
        public void onResult(DataAccessResult<T> result, AsynchronousDataAccess<T> access) {
            if (isPending()) {
                final int index = getAccessIndex(access);

                // If this is the last access, last access allowed by the limit, or the validator says it's done, terminate
                if ((index >= getAsyncDataAccesses().size() - 1) ||
                        (access.getSourceId() == getFetchLimitId()) ||
                        dataValidator.isFinal(result, access)) {
                    close();
                }

                processResult(result, access);

                setLastAccessIndex(index);
            }

            if (isPending()) {
                queryNext();
            }
        }

        @Override
        protected void doFetch(int limitId) {
            SynchronousDataAccess<T> syncAccess = getDataController().getSyncDataAccess();

            // Process the synchronous access
            if (syncAccess != null) {
                DataAccessResult<T> syncResult = syncAccess.get();

                // If the id matches the limit, or the validator marks it as final, we're done
                if ((syncAccess.getSourceId() == limitId) || dataValidator.isFinal(syncResult, syncAccess)) {
                    close();
                }

                getDataController().processResult(syncResult, syncAccess);
            }

            // Don't continue if we're already done from any above logic
            if (isPending()) {
                queryNext();
            }
        }

        protected void queryNext() {
            final int nextIndex = getLastAccessIndex() + 1;

            // Ran off the end? Shouldn't get here...close!
            if (nextIndex >= getAsyncDataAccesses().size()) {
                close();
            }

            AsynchronousDataAccess<T> access = getAsyncDataAccesses().get(nextIndex);
            access.get(getCurrentCallback());
        }

        public static class Validators {
            public static <T> DataValidator<T> newAny() {
                return new DataValidator<T>() {
                    @Override
                    public boolean isFinal(DataAccessResult<T> result, DataAccess access) {
                        return true;
                    }
                };
            }

            public static <T> DataValidator<T> newValidOnly() {
                return new DataValidator<T>() {
                    @Override
                    public boolean isFinal(DataAccessResult<T> result, DataAccess access) {
                        return result.hasValidData();
                    }
                };
            }

            public static <T> DataValidator<T> newDataOrError() {
                return new DataValidator<T>() {
                    @Override
                    public boolean isFinal(DataAccessResult<T> result, DataAccess access) {
                        return result.getData() != null || result.getError() != null;
                    }
                };
            }
        }
    }
}
