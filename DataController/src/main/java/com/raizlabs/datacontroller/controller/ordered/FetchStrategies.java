package com.raizlabs.datacontroller.controller.ordered;

import android.provider.ContactsContract;

import com.raizlabs.datacontroller.DataAccessResult;
import com.raizlabs.datacontroller.access.AsynchronousDataAccess;

import java.util.ArrayList;
import java.util.List;

public class FetchStrategies {

    public static class Parallel<T> implements FetchStrategy<T>, ResultProcessor<T> {
        private OrderedDataController<T> dataController;

        private List<AsynchronousDataAccess<T>> dataAccesses;
        private int lastAccessIndex;

        private CancelableCallback<T> previousCallback;

        @Override
        public void setDataController(OrderedDataController<T> dataController) {
            this.dataController = dataController;
        }

        @Override
        public synchronized void fetch() {
            // If we're already running, don't start another update.
            if (isPending()) {
                return;
            }

            lastAccessIndex = -1;

            // Stop any existing calls to "reset"
            close();

            CancelableCallback<T> callback = new CancelableCallback<>(this);
            previousCallback = callback;

            this.dataAccesses = new ArrayList<>(dataController.getAsyncDataAccesses());

            // Start each access
            // Can't do both operations simultaneously in case accesses return in line.
            for (AsynchronousDataAccess<T> access : dataAccesses) {
                access.get(callback);
            }

        }

        @Override
        public synchronized boolean isPending() {
            return (previousCallback != null);
        }

        @Override
        public synchronized void close() {
            if (previousCallback != null) {
                previousCallback.close();
                previousCallback = null;
            }
        }

        @Override
        public synchronized void onResult(DataAccessResult<T> result, AsynchronousDataAccess<T> access) {
            if (onAccessResult(getAccessIndex(access))) {
                dataController.processAsyncResult(result, access);
            }
        }

        /**
         * @param index
         * @return True if the result of the given index should be processed.
         */
        private synchronized boolean onAccessResult(int index) {
            // Only process the result if the index increased
            if (index > lastAccessIndex && isPending()) {
                lastAccessIndex = index;

                // This was the last one, finish everything
                if (index >= dataAccesses.size() - 1) {
                    close();
                }

                return true;
            } else {
                return false;
            }
        }

        private int getAccessIndex(AsynchronousDataAccess<T> access) {
            return dataAccesses.indexOf(access);
        }
    }

    public static class Serial<T> implements FetchStrategy<T>, ResultProcessor<T> {

        public interface DataValidator<T> {
            public boolean isReturnable(DataAccessResult<T> result, AsynchronousDataAccess<T> access);
            public boolean isFinal(DataAccessResult<T> result, AsynchronousDataAccess<T> access);
        }

        private OrderedDataController<T> dataController;
        private DataValidator<T> dataValidator;

        private List<AsynchronousDataAccess<T>> dataAccesses;
        private int lastAccessIndex;

        private CancelableCallback<T> previousCallback;

        public Serial(DataValidator<T> validator) {
            this.dataValidator = validator;
        }

        @Override
        public void setDataController(OrderedDataController<T> dataController) {
            this.dataController = dataController;
        }

        @Override
        public synchronized void fetch() {
            // If we're already running, don't start another update.
            if (isPending()) {
                return;
            }

            lastAccessIndex = -1;

            // Stop any existing calls to "reset"
            close();

            previousCallback = new CancelableCallback<>(this);

            this.dataAccesses = new ArrayList<>(dataController.getAsyncDataAccesses());

            queryNext();
        }

        @Override
        public synchronized boolean isPending() {
            return (previousCallback != null);
        }

        @Override
        public synchronized void close() {
            if (previousCallback != null) {
                previousCallback.close();
                previousCallback = null;
            }
        }

        @Override
        public synchronized void onResult(DataAccessResult<T> result, AsynchronousDataAccess<T> access) {
            int index = dataAccesses.indexOf(access);

            // If this is the last access or the validator says it's done, terminate
            if ((index >= dataAccesses.size() - 1) || dataValidator.isFinal(result, access)) {
                close();
            }

            if (dataValidator.isReturnable(result, access)) {
                dataController.processAsyncResult(result, access);
            }

            if (isPending()) {
                queryNext();
            }
        }

        protected void queryNext() {
            dataAccesses.get(++lastAccessIndex).get(previousCallback);
        }

        public static class Validators {
            public static <T> DataValidator<T> newAny() {
                return new DataValidator<T>() {
                    @Override
                    public boolean isReturnable(DataAccessResult<T> result, AsynchronousDataAccess<T> access) {
                        return true;
                    }

                    @Override
                    public boolean isFinal(DataAccessResult<T> result, AsynchronousDataAccess<T> access) {
                        return true;
                    }
                };
            }

            public static <T> DataValidator<T> newValidOnly() {
                return new DataValidator<T>() {
                    @Override
                    public boolean isReturnable(DataAccessResult<T> result, AsynchronousDataAccess<T> access) {
                        return result.hasValidData();
                    }

                    @Override
                    public boolean isFinal(DataAccessResult<T> result, AsynchronousDataAccess<T> access) {
                        return result.hasValidData();
                    }
                };
            }

            public static <T> DataValidator<T> newDataOrError() {
                return new DataValidator<T>() {
                    @Override
                    public boolean isReturnable(DataAccessResult<T> result, AsynchronousDataAccess<T> access) {
                        return result.getData() != null || result.getError() != null;
                    }

                    @Override
                    public boolean isFinal(DataAccessResult<T> result, AsynchronousDataAccess<T> access) {
                        return result.getData() != null || result.getError() != null;
                    }
                };
            }
        }
    }
}
