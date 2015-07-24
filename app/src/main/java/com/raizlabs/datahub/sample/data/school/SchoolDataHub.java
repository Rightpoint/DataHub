package com.raizlabs.datahub.sample.data.school;

import com.raizlabs.datahub.hub.ordered.OrderedDataHub;
import com.raizlabs.datahub.sample.dataacess.BaseDiskListDataAccess;
import com.raizlabs.datahub.sample.dataacess.BaseMemoryDataAccess;
import com.raizlabs.datahub.sample.dataacess.VolleyWebListDataAccess;

import java.util.Arrays;
import java.util.List;

public class SchoolDataHub extends OrderedDataHub<List<School>> {

    private static final SchoolDataHub INSTANCE = new SchoolDataHub();

    public static SchoolDataHub getInstance() { return INSTANCE; }

    /**
     * Constructs a new controller around the given data access.
     */
    private SchoolDataHub() {
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
