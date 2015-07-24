package com.raizlabs.datahub.sample.dataacess;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;

public class WebAccessManager {
    private static WebAccessManager instance;

    public static WebAccessManager getInstance() {
        if(instance == null) {
            throw new IllegalStateException(WebAccessManager.class.getName() + " is not initialized.");
        }
        return instance;
    }

    public static void init(Context context) {
        if(instance == null) {
            instance = new WebAccessManager(context);
        }
    }

    private Context context;

    private WebAccessManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void requestData(Request request) {
        Volley.newRequestQueue(context).add(request);
    }
}
