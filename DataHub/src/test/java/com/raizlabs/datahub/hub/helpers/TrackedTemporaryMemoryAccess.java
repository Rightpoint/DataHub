package com.raizlabs.datahub.hub.helpers;

import com.raizlabs.datahub.access.DataAccessResult;
import com.raizlabs.datahub.access.TemporaryMemoryAccess;

public class TrackedTemporaryMemoryAccess<Data> extends TemporaryMemoryAccess<Data> {

    private boolean queried = false;


    public TrackedTemporaryMemoryAccess() {
        super();
    }

    public TrackedTemporaryMemoryAccess(int typeId) {
        super(typeId);
    }

    @Override
    public synchronized DataAccessResult<Data> get() {
        queried = true;
        return super.get();
    }

    public boolean wasQueried() {
        return queried;
    }

    public void reset() {
        queried = false;
    }
}
