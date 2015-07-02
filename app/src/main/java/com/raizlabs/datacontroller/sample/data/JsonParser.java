package com.raizlabs.datacontroller.sample.data;

import org.json.JSONException;
import org.json.JSONObject;

public interface JsonParser<Data> {
    Data parseJsonObject(JSONObject jsonObject) throws JSONException;
}
