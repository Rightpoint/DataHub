package com.raizlabs.datacontroller.sample.dataacess;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.raizlabs.datacontroller.ErrorInfo;
import com.raizlabs.datacontroller.access.m.BaseWebDataAccess;
import com.raizlabs.datacontroller.access.m.WebDataAccessListener;
import com.raizlabs.datacontroller.sample.data.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public abstract class VolleyWebListDataAccess<Data extends JsonParser> extends BaseWebDataAccess<List<Data>> {

    private WebDataAccessListener<List<Data>> listener;

    private Request request;

    private Class<Data> clazz;

    public VolleyWebListDataAccess(Class<Data> clazz) {
        this.clazz = clazz;
    }

    public abstract String getUrl();

    @Override
    protected void requestData(WebDataAccessListener<List<Data>> listener) {
        this.listener = listener;
        request = createRequest();
        WebAccessManager.getInstance().requestData(request);
    }

    @Override
    protected void closeRequest() {
        if(request != null) {
            request.cancel();
        }
    }

    protected List<Data> parseJsonResponse(JSONArray jsonArray) {
        List<Data> list = new ArrayList<>();
        for(int i = 0; i < jsonArray.length(); i++) {
            try {
                Data data = clazz.newInstance();
                data.parseJsonObject(jsonArray.getJSONObject(i));
                list.add(data);
            } catch(InstantiationException e) {
                e.printStackTrace();
            } catch(IllegalAccessException e) {
                e.printStackTrace();
            } catch(JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private Request createRequest() {
        return new JsonArrayRequest(getUrl(), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(final JSONArray response) {
                Thread sample = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Simulate Delay
                        try {
                            Thread.sleep(5000);
                        } catch(InterruptedException e) {
                            e.printStackTrace();
                        }

                        if(VolleyWebListDataAccess.this.listener != null) {
                            VolleyWebListDataAccess.this.listener.onDataReceived(parseJsonResponse(response));
                        }
                    }
                });
                sample.start();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(final VolleyError error) {
                Thread sample = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Simulate Delay
                        try {
                            Thread.sleep(5000);
                        } catch(InterruptedException e) {
                            e.printStackTrace();
                        }

                        if(VolleyWebListDataAccess.this.listener != null) {
                            VolleyWebListDataAccess.this.listener.onErrorReceived(new ErrorInfo(error.getMessage(), error.getCause() == null ? null : error.getCause().getMessage(), ErrorInfo.WEB_DATA, false));
                        }
                    }
                });
                sample.start();
            }
        });
    }
}
