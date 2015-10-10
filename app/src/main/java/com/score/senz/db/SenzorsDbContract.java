package com.score.senz.db;

import android.provider.BaseColumns;

/**
 * Keep database table attributes
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SenzorsDbContract {

    public SenzorsDbContract() {}

    /* Inner class that defines sensor table contents */
    public static abstract class Senz implements BaseColumns {
        public static final String TABLE_NAME = "senz";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_VALUE = "value";
        public static final String COLUMN_NAME_USER = "user";
    }

    /* Inner class that defines the user table contents */
    public static abstract class User implements BaseColumns {
        public static final String TABLE_NAME = "user";
        public static final String COLUMN_NAME_USERNAME = "username";
        public static final String COLUMN_NAME_NAME = "name";
    }

    /* Inner class that defines the shared_user table contents */
    public static abstract class SharedUser implements BaseColumns {
        public static final String TABLE_NAME = "shared_user";
        public static final String COLUMN_NAME_USER = "user";
        public static final String COLUMN_NAME_SENSOR = "sensor";
    }

}
