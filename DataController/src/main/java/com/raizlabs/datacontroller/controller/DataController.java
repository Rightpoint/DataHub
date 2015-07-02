package com.raizlabs.datacontroller.controller;

import android.util.Log;

import com.raizlabs.datacontroller.ErrorInfo;
import com.raizlabs.datacontroller.ResultInfo;
import com.raizlabs.datacontroller.access.DiskDataAccess;
import com.raizlabs.datacontroller.access.DiskDataAccessListener;
import com.raizlabs.datacontroller.access.MemoryDataAccess;
import com.raizlabs.datacontroller.access.WebDataAccess;
import com.raizlabs.datacontroller.access.WebDataAccessListener;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Class that manages the data fetch based on quick access strategy. It handles all the async calls for fresh data and
 * at the same time stores the returned data to local cache for quick fetch in future.
 *
 * @param <Data> The type of data being accessed.
 */
public abstract class DataController<Key, Data> {

    public enum FetchType {
        /**
         * Data will be fetched from memory cache or disk cache, if available, and a fresh data will be requested if the
         * cached data has expired or is unavailable.
         */
        NORMAL_ACCESS,

        /**
         * This guarantees a fresh data from the web even if the data is not expired in cache. While fetching for the
         * fresh data this will still immediately return the cached data, if available.
         */
        FRESH_ONLY,

        /**
         * Only cached data (either from memory or disk) should be fetched and no requests will be placed for fresh data from web source.<br/>
         * Note: This can return <code>null</code> if the cache is empty.
         */
        /**
         * Only cached data (either from memory or disk) should be fetched and requests will be placed for fresh data
         * from web source only if data is unavailable.<br/> Note: This can return <code>null</code> if the cache is
         * empty.
         */
        CACHE_ONLY
    }

    private MemoryDataAccess<Key, Data> memoryDataAccess;

    private DiskDataAccess<Key, Data> diskDataAccess;

    private WebDataAccess<Data> webDataAccess;

    private Set<DataControllerListener<Data>> listeners = new HashSet<>();

    private long dataLifeSpan, lastUpdatedTimestamp;

    private FetchType fetchType = FetchType.NORMAL_ACCESS;

    private boolean isFetching = false;

    private WebDataAccessListener<Data> webDataAccessListener = new WebDataAccessListener<Data>() {
        @Override
        public void onDataReceived(Data data) {
            DataController.this.lastUpdatedTimestamp = System.currentTimeMillis();
            notifyDataReceived(new ResultInfo<>(data, ResultInfo.WEB_DATA, false, DataController.this.lastUpdatedTimestamp, getDataLifeSpan()));
            //Update the memory & disk caches.
            memoryDataAccess.setData(getKey(), data);
            diskDataAccess.setData(getKey(), data, System.currentTimeMillis());
        }

        @Override
        public void onErrorReceived(ErrorInfo error) {
            notifyErrorResponse(error);
        }
    };

    private DiskDataAccessListener<Data> diskDataAccessListener = new DiskDataAccessListener<Data>() {
        @Override
        public void onDataReceived(Data data, long lastUpdatedTimestamp) {
            if(data == null) {
                notifyErrorResponse(new ErrorInfo("Data not found", "", ResultInfo.DISK_DATA, true, lastUpdatedTimestamp, getDataLifeSpan()));
                webDataAccess.getData(webDataAccessListener);
            } else {
                DataController.this.lastUpdatedTimestamp = lastUpdatedTimestamp;
                memoryDataAccess.setData(getKey(), data);
                if(fetchType != FetchType.CACHE_ONLY && (fetchType == FetchType.FRESH_ONLY || hasDataExpired())) {
                    notifyDataReceived(new ResultInfo<>(data, ResultInfo.DISK_DATA, true, lastUpdatedTimestamp, getDataLifeSpan()));
                    webDataAccess.getData(webDataAccessListener);
                } else {
                    notifyDataReceived(new ResultInfo<>(data, ResultInfo.DISK_DATA, false, lastUpdatedTimestamp, getDataLifeSpan()));
                }
            }
        }

        @Override
        public void onErrorReceived(ErrorInfo errorInfo) {
            notifyErrorResponse(new ErrorInfo(errorInfo.getErrorTitle(), errorInfo.getErrorDescription(), ResultInfo.DISK_DATA, true, lastUpdatedTimestamp, getDataLifeSpan()));
            webDataAccess.getData(webDataAccessListener);
        }
    };

    /**
     * Constructs a new controller around the given data access.
     *
     * @param dataLifeSpan   This data's life span.
     * @param timeUnit       Time unit used for the life span.
     * @param diskDataAccess A data access which can be used for quick access to local data.
     * @param webDataAccess  A slower data access which is used to access the most up-to-date remote data.
     */
    public DataController(long dataLifeSpan, TimeUnit timeUnit, MemoryDataAccess<Key, Data> memoryDataAccess, DiskDataAccess<Key, Data> diskDataAccess, WebDataAccess<Data> webDataAccess) {
        this.dataLifeSpan = TimeUnit.MILLISECONDS.convert(dataLifeSpan, timeUnit);
        this.memoryDataAccess = memoryDataAccess;
        this.diskDataAccess = diskDataAccess;
        this.webDataAccess = webDataAccess;
    }

    protected abstract Key getKey();

    private long getDataLifeSpan() {
        return dataLifeSpan;
    }

    public void addListener(DataControllerListener<Data> listener) {
        listeners.add(listener);
    }

    public boolean removeListener(DataControllerListener<Data> listener) {
        return listeners.remove(listener);
    }

    /**
     * Starts a full fetch of the data (based on FetchType) to obtain the most up-to-date data, and quickly returns the
     * local data, if available.
     *
     * @param fetchType Type of fetch requested.
     *
     * @return The cached data we have locally.
     */
    public synchronized void fetch(FetchType fetchType) {
        if(isFetching) {
            //Return back memory cached data immediately.
            Data data = memoryDataAccess.getData(getKey());
            if(data == null) {
                notifyErrorResponse(new ErrorInfo("Data not found", "", ResultInfo.MEMORY_DATA, true, lastUpdatedTimestamp, getDataLifeSpan()));
            } else {
                notifyDataReceived(new ResultInfo<>(data, ResultInfo.MEMORY_DATA, true, lastUpdatedTimestamp, getDataLifeSpan()));
            }
            //We need to avoid duplicate fetch requests when a fetch is already in progress.
            //Please note that any fetchType request to DISK or WEB will be ignored until the current fetch in progress is complete.
            return;
        }

        isFetching = true;
        this.fetchType = fetchType;
        Data data = memoryDataAccess.getData(getKey());
        if(data == null) {
            notifyErrorResponse(new ErrorInfo("Data not found", "", ResultInfo.MEMORY_DATA, true, lastUpdatedTimestamp, getDataLifeSpan()));
            diskDataAccess.getData(getKey(), diskDataAccessListener);
        } else {
            if(fetchType != FetchType.CACHE_ONLY && (fetchType == FetchType.FRESH_ONLY || hasDataExpired())) {
                notifyDataReceived(new ResultInfo<>(data, ResultInfo.MEMORY_DATA, true, lastUpdatedTimestamp, getDataLifeSpan()));
                webDataAccess.getData(webDataAccessListener);
            } else {
                notifyDataReceived(new ResultInfo<>(data, ResultInfo.MEMORY_DATA, false, lastUpdatedTimestamp, getDataLifeSpan()));
            }
        }
    }

    /**
     * Indicates that we are done with this controller and its resources should be released.
     */
    public synchronized void close() {
        isFetching = false;
        diskDataAccess.close();
        webDataAccess.close();
        listeners.clear();
    }

    private boolean hasDataExpired() {
        return (System.currentTimeMillis() - lastUpdatedTimestamp > dataLifeSpan);
    }

    /*
    private DiskDataAccessListener<Data> diskDataAccessListener = new DiskDataAccessListener<Data>() {
        @Override
        public void onDataReceived(Data data, long lastUpdatedTimestamp) {
            if (fetchType != FetchType.CACHE_ONLY && data == null) {
                notifyErrorResponse(new ErrorInfo("Data not found", "", ResultInfo.DISK_DATA, true, lastUpdatedTimestamp));
                webDataAccess.getData(webDataAccessListener);
            } else {
                DataController.this.lastUpdatedTimestamp = lastUpdatedTimestamp;
                memoryDataAccess.setData(getKey(), data);
                if (fetchType != FetchType.CACHE_ONLY && (fetchType == FetchType.FRESH_ONLY || hasDataExpired())) {
                    notifyDataReceived(new ResultInfo<>(data, ResultInfo.DISK_DATA, true, lastUpdatedTimestamp));
                    webDataAccess.getData(webDataAccessListener);
                } else {
                    notifyDataReceived(new ResultInfo<>(data, ResultInfo.DISK_DATA, false, lastUpdatedTimestamp));
                }
            }
        }

        @Override
        public void onErrorReceived(ErrorInfo errorInfo) {
            notifyErrorResponse(new ErrorInfo(errorInfo.getErrorTitle(), errorInfo.getErrorDescription(), ResultInfo.DISK_DATA, (fetchType != FetchType.CACHE_ONLY), lastUpdatedTimestamp));
            if (fetchType != FetchType.CACHE_ONLY) {
                webDataAccess.getData(webDataAccessListener);
            }
        }
    };*/

    private void notifyDataReceived(ResultInfo<Data> resultInfo) {
        if(!resultInfo.isFreshDataIncoming()) {
            isFetching = false;
        }
        synchronized(this) {
            Log.d("MERV", "notifyDataReceived[" + (resultInfo.getDataSourceType() == ResultInfo.MEMORY_DATA ? "MEMORY" : resultInfo.getDataSourceType() == ResultInfo.DISK_DATA ? "DISK" : "WEB") + "]: listeners(" + listeners.size() + ") Incoming?" + resultInfo.isFreshDataIncoming());
            // Notify the data listeners
            for(DataControllerListener<Data> listener : listeners) {
                listener.onDataReceived(resultInfo);
            }
        }
    }

    private void notifyErrorResponse(ErrorInfo errorInfo) {
        if(!errorInfo.isFreshDataIncoming()) {
            isFetching = false;
        }
        synchronized(this) {
            Log.d("MERV", "notifyErrorResponse[" + (errorInfo.getDataSourceType() == ResultInfo.MEMORY_DATA ? "MEMORY" : errorInfo.getDataSourceType() == ResultInfo.DISK_DATA ? "DISK" : "WEB") + "]: listeners(" + listeners.size() + ") Incoming?" + errorInfo.isFreshDataIncoming());
            // Notify the data listeners
            for(DataControllerListener<Data> listener : listeners) {
                listener.onErrorReceived(errorInfo);
            }
        }
    }
}
