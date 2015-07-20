package com.raizlabs.datacontroller.controller.helpers;

import com.raizlabs.datacontroller.access.DataAccessResult;
import com.raizlabs.datacontroller.access.TemporaryMemoryAccess;

public class TrackedTemporaryMemoryAccess<Data> extends TemporaryMemoryAccess<Data> {

    private boolean queried = false;


    public TrackedTemporaryMemoryAccess() {
        super();
    }

    public TrackedTemporaryMemoryAccess(int sourceId) {
        super(sourceId);
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
