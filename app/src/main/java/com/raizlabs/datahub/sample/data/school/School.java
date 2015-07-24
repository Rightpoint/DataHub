package com.raizlabs.datahub.sample.data.school;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.datahub.sample.AppDatabase;
import com.raizlabs.datahub.sample.data.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Table(databaseName = AppDatabase.NAME)
public class School extends BaseModel implements JsonParser<School>, Comparable<School>{

    @Column
    @PrimaryKey
    public String name;

    @Column
    public String type;

    @Column
    public String buildingName;

    @Column
    public String street;

    @Column
    public String city;

    @Column
    public String state;

    @Column
    public String zipcode;

    @Column
    public double locationX;

    @Column
    public double locationY;

    @Override
    public School parseJsonObject(JSONObject jsonObject) throws JSONException {
        name = jsonObject.getString("sch_name");
        type = jsonObject.getString("sch_type");
        buildingName = jsonObject.getString("bldg_name");
        street = jsonObject.getString("location_location");
        city = jsonObject.getString("location_city");
        state = jsonObject.getString("location_state");
        zipcode = jsonObject.getString("location_zip");
        JSONArray coordinates = jsonObject.getJSONObject("location").getJSONArray("coordinates");
        locationX = coordinates.getDouble(0);
        locationY = coordinates.getDouble(1);
        return this;
    }

    @Override
    public int compareTo(School another) {
        return name.compareTo(another.name);
    }
}
