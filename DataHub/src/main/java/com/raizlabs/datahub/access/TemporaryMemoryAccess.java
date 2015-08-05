package com.raizlabs.datahub.access;

/**
 * A {@link SyncDataAccess} which simply keeps the value around in its own lifetime. Data can be imported and will be
 * stored locally and returned until the value is changed, cleared, or this object itself is dereferenced and garbage
 * collected.
 *
 * @param <Data> {@inheritDoc}
 */
public class TemporaryMemoryAccess<Data> implements SyncDataAccess<Data> {

    private Data value;
    private boolean imported = false;
    private final int typeId;

    /**
     * Creates a new {@link TemporaryMemoryAccess} which defaults to the {@link AccessTypeIds#MEMORY_DATA} type ID.
     */
    public TemporaryMemoryAccess() {
        this(AccessTypeIds.MEMORY_DATA);
    }

    /**
     * Creates a new {@link TemporaryMemoryAccess} which returns the given type ID.
     *
     * @param typeId The type ID to return.
     */
    public TemporaryMemoryAccess(int typeId) {
        this.typeId = typeId;
    }

    @Override
    public synchronized DataAccessResult<Data> get() {
        if (imported) {
            return DataAccessResult.fromResult(value);
        } else {
            return DataAccessResult.fromUnavailable();
        }
    }

    @Override
    public synchronized void importData(Data data) {
        value = data;
        imported = true;
    }

    @Override
    public synchronized void close() {
        clear();
    }

    @Override
    public int getTypeId() {
        return typeId;
    }

    /**
     * Clears the value of this {@link TemporaryMemoryAccess}.
     */
    public synchronized void clear() {
        value = null;
        imported = false;
    }
}
