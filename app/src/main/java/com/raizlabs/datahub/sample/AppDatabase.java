package com.raizlabs.datahub.sample;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * The main database utilized for our application.
 */
@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public class AppDatabase {

    public static final String NAME = "AppDatabase";

    public static final int VERSION = 1;
}
