package com.raizlabs.datahub.sample;

import android.app.Application;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.datahub.sample.dataacess.DiskCacheManager;
import com.raizlabs.datahub.sample.dataacess.MemoryCacheManager;
import com.raizlabs.datahub.sample.dataacess.WebAccessManager;

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
