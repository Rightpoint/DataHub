package com.raizlabs.datacontroller.sample;

import android.app.Application;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.datacontroller.sample.dataacess.DiskCacheManager;
import com.raizlabs.datacontroller.sample.dataacess.MemoryCacheManager;
import com.raizlabs.datacontroller.sample.dataacess.WebAccessManager;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FlowManager.init(this);
        MemoryCacheManager.init(1000);
        WebAccessManager.init(this);
        DiskCacheManager.init(this);
    }
}
