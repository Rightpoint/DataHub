package com.raizlabs.datacontroller.sample.data.school;

import com.raizlabs.datacontroller.controller.ordered.OrderedDataController;
import com.raizlabs.datacontroller.sample.dataacess.BaseDiskListDataAccess;
import com.raizlabs.datacontroller.sample.dataacess.BaseMemoryDataAccess;
import com.raizlabs.datacontroller.sample.dataacess.VolleyWebListDataAccess;

import java.util.Arrays;
import java.util.List;

public class SchoolDataController extends OrderedDataController<List<School>> {

    private static final SchoolDataController INSTANCE = new SchoolDataController();

    public static SchoolDataController getInstance() { return INSTANCE; }

    /**
     * Constructs a new controller around the given data access.
     */
    private SchoolDataController() {
        super(new BaseMemoryDataAccess<List<School>>(), Arrays.asList(new BaseDiskListDataAccess<>(School.class), new SchoolWebDataAccess()));
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
