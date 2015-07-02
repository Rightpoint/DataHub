package com.raizlabs.datacontroller.sample.dataacess;

import android.content.Context;
import android.preference.PreferenceManager;

public class DiskCacheManager {
    private static DiskCacheManager instance;

    public static DiskCacheManager getInstance() {
        if(instance == null) {
            throw new IllegalStateException(DiskCacheManager.class.getName() + " is not initialized.");
        }
        return instance;
    }

    public static void init(Context context) {
        if(instance == null) {
            instance = new DiskCacheManager(context);
        }
    }

    private Context context;

    private DiskCacheManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public String getString(String key, String defaultData) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defaultData);
    }

    public void setString(String key, String data) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, data).commit();
    }

    public long getLong(String key, long defaultData) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, defaultData);
    }

    public void setLong(String key, long data) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(key, data).commit();
    }
}
