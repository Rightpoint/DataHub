package com.raizlabs.datahub.sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.raizlabs.datahub.DataResult;
import com.raizlabs.datahub.ErrorInfo;
import com.raizlabs.datahub.observer.DataObserver;
import com.raizlabs.datahub.observer.DataObserverListener;
import com.raizlabs.datahub.sample.data.RecyclerAdapter;
import com.raizlabs.datahub.sample.data.school.School;
import com.raizlabs.datahub.sample.data.school.SchoolAdapter;
import com.raizlabs.datahub.sample.data.school.SchoolDataHub;
import com.raizlabs.datahub.sample.dataacess.MemoryCacheManager;
import com.raizlabs.datahub.util.ThreadingUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    Timer timer;

    private ViewFlipper viewFlipper;

    private ViewGroup emptyView;

    private RecyclerView recyclerView;

    private TextView textExpiryInfo;

    private RecyclerAdapter<School, SchoolAdapter.ViewHolder> adapter;

    private final DataObserverListener<List<School>> listener = new DataObserverListener<List<School>>() {
        @Override
        public void onDataFetchStarted() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        public void onDataReceived(DataResult<List<School>> dataResult) {
            updateExpiryInfo(dataResult);

            if(!dataResult.isFetching()) {
                setProgressBarIndeterminateVisibility(false);
            }

            Toast.makeText(getApplicationContext(), "onDataReceived\nSource: " + (dataResult.getAccessTypeId() == DataResult.MEMORY_DATA ? "MEMORY" : dataResult.getAccessTypeId() == DataResult.DISK_DATA ? "DISK" : "WEB") + "\nIncoming? " + dataResult.isFetching(), Toast.LENGTH_LONG).show();

            List<School> list = dataResult.getData();
            if(list == null || list.isEmpty()) {
                setEmptyState(true);
            } else {
                Collections.sort(list);
                adapter.loadData(list);
                setEmptyState(false);
            }
        }

        @Override
        public void onErrorReceived(ErrorInfo errorInfo) {
            updateExpiryInfo(errorInfo);

            if(!errorInfo.isFetching()) {
                setProgressBarIndeterminateVisibility(false);
            }
            Toast.makeText(getApplicationContext(), "onErrorReceived\nSource: " + (errorInfo.getDataSourceType() == DataResult.MEMORY_DATA ? "MEMORY" : errorInfo.getDataSourceType() == DataResult.DISK_DATA ? "DISK" : "WEB") + " \nIncoming? " + errorInfo.isFetching() + "\nReason: " + errorInfo.getErrorMessage().toUpperCase(), Toast.LENGTH_LONG).show();
        }
    };

    private DataObserver<List<School>> source = new DataObserver<>(SchoolDataHub.getInstance());

    private void setEmptyState(boolean isEmptyState) {
        viewFlipper.setDisplayedChild(isEmptyState ? viewFlipper.indexOfChild(emptyView) : viewFlipper.indexOfChild(recyclerView));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        Log.d("MERV", "onCreate");

        setContentView(R.layout.activity_main);
        setTitle("BOSTON PUBLIC SCHOOLS");

        adapter = new SchoolAdapter();
        adapter.addOnItemClickedListener(new RecyclerAdapter.OnItemClickedListener<School, SchoolAdapter.ViewHolder>() {
            @Override
            public void onItemClicked(SchoolAdapter.ViewHolder viewHolder, int position, School school) {
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", school.locationY, school.locationX, school.locationY, school.locationX, school.name);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Log.d("MERV", "Map Activity");
                getApplicationContext().startActivity(intent);
            }
        });

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        emptyView = (ViewGroup) findViewById(R.id.list_empty);

        recyclerView = (RecyclerView) findViewById(R.id.list_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        textExpiryInfo = (TextView) findViewById(R.id.textExpiryInfo);

        Button buttonRefresh = (Button) findViewById(R.id.buttonRefresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                source.fetch(listener, MDataController.FetchType.NORMAL_ACCESS);
            }
        });

        Button buttonForceUpdate = (Button) findViewById(R.id.buttonForceUpdate);
        buttonForceUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                source.fetch(listener, MDataController.FetchType.FRESH_ONLY);
            }
        });

        Button buttonClearMemory = (Button) findViewById(R.id.buttonClearMemory);
        buttonClearMemory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Memory cache cleared!", Toast.LENGTH_SHORT).show();
                MemoryCacheManager.getInstance().evictAll();
            }
        });

        setEmptyState(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MERV", "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MERV", "onResume");
        source.fetch(listener, MDataController.FetchType.CACHE_ONLY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MERV", "onPause");
        source.close(false);
    }

    private void updateExpiryInfo(final DataResult<List<School>> dataResult) {
        if(timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ThreadingUtils.runOnHandler(new Runnable() {
                    @Override
                    public void run() {
                        long expiresIn = dataResult.getDataLifeSpan() - System.currentTimeMillis() + dataResult.getLastUpdatedTimestamp();
                        if(expiresIn <= 0) {
                            textExpiryInfo.setTextColor(Color.RED);
                            textExpiryInfo.setText("Data has expired! Consider refresh.");
                        } else {
                            textExpiryInfo.setTextColor(Color.DKGRAY);
                            textExpiryInfo.setText("Data expires in " + (expiresIn / 1000) + " seconds.");
                        }
                    }
                }, ThreadingUtils.getUIHandler());
            }
        }, 0, 1000);
    }
}