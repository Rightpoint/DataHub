package com.raizlabs.datacontroller.sample.dataacess;

import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.SelectListTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListenerAdapter;
import com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo;
import com.raizlabs.android.dbflow.runtime.transaction.process.SaveModelTransaction;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.datacontroller.access.DiskDataAccess;
import com.raizlabs.datacontroller.access.DiskDataAccessListener;

import java.util.List;

public class BaseDiskListDataAccess<Data extends Model> implements DiskDataAccess<String, List<Data>> {
    DiskDataAccessListener<List<Data>> listener;

    Class<Data> clazz;

    public BaseDiskListDataAccess(Class<Data> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void setData(String key, List<Data> data, long lastUpdatedTimestamp) {
        DiskCacheManager.getInstance().setLong(key, lastUpdatedTimestamp);
        TransactionManager.getInstance().addTransaction(new SaveModelTransaction<>(ProcessModelInfo.withModels(data)));
    }

    @Override
    public void getData(final String key, DiskDataAccessListener<List<Data>> listener) {
        this.listener = listener;
        TransactionManager.getInstance().addTransaction(new SelectListTransaction<>(new Select().from(clazz), new TransactionListenerAdapter<List<Data>>() {
            @Override
            public void onResultReceived(final List<Data> dataList) {
                super.onResultReceived(dataList);
                Thread sample = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Simulate Delay
                        try {
                            Thread.sleep(3000);
                        } catch(InterruptedException e) {
                            e.printStackTrace();
                        }

                        if(BaseDiskListDataAccess.this.listener != null) {
                            BaseDiskListDataAccess.this.listener.onDataReceived(dataList.isEmpty() ? null : dataList, DiskCacheManager.getInstance().getLong(key, 0));
                        }
                    }
                });
                sample.start();
            }
        }));
    }

    @Override
    public void close() {
        listener = null;
    }
}