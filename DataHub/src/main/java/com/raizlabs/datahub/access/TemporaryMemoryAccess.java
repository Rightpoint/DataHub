package com.raizlabs.datahub.access;

import com.raizlabs.datahub.hub.DataHub;

public class TemporaryMemoryAccess<Data> implements SynchronousDataAccess<Data> {

    private Data value;
    private boolean imported = false;
    private final int typeId;

    public TemporaryMemoryAccess() {
        this(DataHub.AccessTypeIds.MEMORY_DATA);
    }

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

    public synchronized void clear() {
        value = null;
        imported = false;
    }
}
