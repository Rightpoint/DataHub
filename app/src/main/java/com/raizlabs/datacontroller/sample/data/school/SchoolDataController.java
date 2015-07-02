package com.raizlabs.datacontroller.sample.data.school;

import com.raizlabs.datacontroller.controller.DataController;
import com.raizlabs.datacontroller.sample.dataacess.BaseDiskListDataAccess;
import com.raizlabs.datacontroller.sample.dataacess.BaseMemoryDataAccess;
import com.raizlabs.datacontroller.sample.dataacess.VolleyWebListDataAccess;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SchoolDataController extends DataController<String, List<School>> {

    private static final SchoolDataController INSTANCE = new SchoolDataController();

    public static SchoolDataController getInstance() { return INSTANCE; }

    /**
     * Constructs a new controller around the given data access.
     */
    private SchoolDataController() {
        super(2, TimeUnit.MINUTES, new BaseMemoryDataAccess<List<School>>(), new BaseDiskListDataAccess<>(School.class), new SchoolWebDataAccess());
    }

    @Override
    protected String getKey() {
        return SchoolWebDataAccess.URL;
    }

    static class SchoolWebDataAccess extends VolleyWebListDataAccess<School>{

        private static final String URL = "https://data.cityofboston.gov/resource/492y-i77g.json";

        public SchoolWebDataAccess(){
            super(School.class);
        }

        @Override
        public String getUrl() {
            return URL;
        }
    }
}
