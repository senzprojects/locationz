package com.score.senzors.db;

import android.provider.BaseColumns;

/**
 * Keep database table attributes
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SenzorsDbContract {

    public SenzorsDbContract() {}

    /* Inner class that defines sensor table contents */
    public static abstract class Sensor implements BaseColumns {
        public static final String TABLE_NAME = "sensor";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_VALUE = "value";
        public static final String COLUMN_NAME_IS_MINE = "is_mine";
        public static final String COLUMN_NAME_USER = "user";
        public static final String TRIGGER_FOREIGN_KEY_INSERT= "fk_insert_sensor";
        public static final String TRIGGER_UNIQUE_KEY_INSERT= "unique_insert_sensor";
    }

    /* Inner class that defines the user table contents */
    public static abstract class User implements BaseColumns {
        public static final String TABLE_NAME = "user";
        public static final String COLUMN_NAME_PHONE = "phone";
    }

    /* Inner class that defines the shared_user table contents */
    public static abstract class SharedUser implements BaseColumns {
        public static final String TABLE_NAME = "shared_user";
        public static final String COLUMN_NAME_USER = "user";
        public static final String COLUMN_NAME_SENSOR = "sensor";
    }

}
