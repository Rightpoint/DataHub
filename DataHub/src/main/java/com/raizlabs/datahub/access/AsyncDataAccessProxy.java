package com.raizlabs.datahub.access;

/**
 * {@link AsyncDataAccess} implementation which points to another {@link AsyncDataAccess}. The target access can
 * be changed, so this class can act as a pointer to another access, which can be changed.
 *
 * @param <Data> {@inheritDoc}
 */
public class AsyncDataAccessProxy<Data> implements AsyncDataAccess<Data> {

    private AsyncDataAccess<Data> target;
    private int defaultTypeId = AccessTypeIds.NONE;

    /**
     * Creates a default {@link AsyncDataAccessProxy} which points to nothing.
     */
    public AsyncDataAccessProxy() {

    }

    /**
     * Creates an {@link AsyncDataAccessProxy} which points to the given {@link AsyncDataAccess}.
     *
     * @param target The target {@link AsyncDataAccess} to point to.
     */
    public AsyncDataAccessProxy(AsyncDataAccess<Data> target) {
        this();
        setTarget(target);
    }

    /**
     * @return The current {@link AsyncDataAccess} being pointed to.
     */
    public AsyncDataAccess<Data> getTarget() {
        return target;
    }

    /**
     * Sets the {@link AsyncDataAccess} to point to.
     *
     * @param target The {@link AsyncDataAccess} to point to.
     */
    public void setTarget(AsyncDataAccess<Data> target) {
        this.target = target;
    }

    /**
     * Sets the type id to return if there is no target.
     *
     * @param typeId The type id to return.
     */
    public void setDefaultTypeId(int typeId) {
        this.defaultTypeId = typeId;
    }

    @Override
    public void get(AsyncDataCallback<Data> asyncDataCallback) {
        if (target != null) {
            target.get(asyncDataCallback);
        } else {
            asyncDataCallback.onResult(DataAccessResult.<Data>fromUnavailable(), this);
        }
    }

    @Override
    public void close() {
        if (target != null) {
            target.close();
        }
    }

    @Override
    public int getTypeId() {
        if (target != null) {
            return target.getTypeId();
        } else {
            return defaultTypeId;
        }
    }

    @Override
    public void importData(Data data) {
        if (target != null) {
            target.importData(data);
        }
    }
}