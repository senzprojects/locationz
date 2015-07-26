package com.score.senzors.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Perform creating tables here
 *
 * @author erangaeb@gmail.com(eranga herath)
 */
public class SenzorsDbHelper extends SQLiteOpenHelper {

    private static final String TAG = SenzorsDbHelper.class.getName();

    // we use singleton database
    private static SenzorsDbHelper senzorsDbHelper;

    // If you change the database schema, you must increment the database version
    private static final int DATABASE_VERSION = 12;
    private static final String DATABASE_NAME = "Senzors.db";

    // data types, keywords and queries
    private static final String TEXT_TYPE = " TEXT";
    private static final String SMALLINT_TYPE = " SMALLINT";
    private static final String SQL_CREATE_SENSOR =
            "CREATE TABLE " + SenzorsDbContract.Sensor.TABLE_NAME + " (" +
                    SenzorsDbContract.Sensor._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                    SenzorsDbContract.Sensor.COLUMN_NAME_NAME + TEXT_TYPE + "," +
                    SenzorsDbContract.Sensor.COLUMN_NAME_VALUE + TEXT_TYPE + "," +
                    SenzorsDbContract.Sensor.COLUMN_NAME_IS_MINE + SMALLINT_TYPE + "," +
                    SenzorsDbContract.Sensor.COLUMN_NAME_USER + " INTEGER NOT NULL" + "," +
                    "FOREIGN KEY" + "(" + SenzorsDbContract.Sensor.COLUMN_NAME_USER + ") " +
                    "REFERENCES "+ SenzorsDbContract.User.TABLE_NAME + "(" + SenzorsDbContract.User._ID + ")" +
                    "UNIQUE" + "(" + SenzorsDbContract.Sensor.COLUMN_NAME_NAME + ","  + SenzorsDbContract.Sensor.COLUMN_NAME_USER + ")" +
            " )";
    private static final String SQL_CREATE_USER =
            "CREATE TABLE " + SenzorsDbContract.User.TABLE_NAME + " (" +
                    SenzorsDbContract.Sensor._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                    SenzorsDbContract.User.COLUMN_NAME_PHONE + TEXT_TYPE + "UNIQUE NOT NULL" +
            " )";
    private static final String SQL_CREATE_SHARED_USER =
            "CREATE TABLE " + SenzorsDbContract.SharedUser.TABLE_NAME + " (" +
                    SenzorsDbContract.SharedUser._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                    SenzorsDbContract.SharedUser.COLUMN_NAME_USER + " INTEGER NOT NULL" + "," +
                    SenzorsDbContract.SharedUser.COLUMN_NAME_SENSOR + " INTEGER NOT NULL" + "," +
                    "FOREIGN KEY" + "(" + SenzorsDbContract.SharedUser.COLUMN_NAME_USER + ") " +
                    "REFERENCES "+ SenzorsDbContract.User.TABLE_NAME + "(" + SenzorsDbContract.User._ID + ")" +
                    "FOREIGN KEY" + "(" + SenzorsDbContract.SharedUser.COLUMN_NAME_SENSOR + ") " +
                    "REFERENCES "+ SenzorsDbContract.Sensor.TABLE_NAME + "(" + SenzorsDbContract.Sensor._ID + ")" +
            " )";

    private static final String SQL_DELETE_SENSOR =
            "DROP TABLE IF EXISTS " + SenzorsDbContract.Sensor.TABLE_NAME;
    private static final String SQL_DELETE_USER =
            "DROP TABLE IF EXISTS " + SenzorsDbContract.User.TABLE_NAME;
    private static final String SQL_DELETE_SHARED_USER =
            "DROP TABLE IF EXISTS " + SenzorsDbContract.SharedUser.TABLE_NAME;

    // trigger that use to define foreign key constraint of "user" in "sensor" table
    // we need to define foreign key constraint because of sqlite older versions not default supporting foreign key constraints
    private static final String SQL_CREATE_TRIGGER_SENSOR_FOREIGN_KEY =
            "CREATE TRIGGER " + SenzorsDbContract.Sensor.TRIGGER_FOREIGN_KEY_INSERT + " " +
            "BEFORE INSERT ON " + SenzorsDbContract.Sensor.TABLE_NAME + " " +
                    "FOR EACH ROW BEGIN " +
                            "SELECT CASE WHEN((" +
                                    "SELECT " + SenzorsDbContract.User._ID + " " +
                                    "FROM " + SenzorsDbContract.User.TABLE_NAME + " " +
                                    "WHERE " + SenzorsDbContract.User._ID + "=" + "NEW." + SenzorsDbContract.Sensor.COLUMN_NAME_USER + ") " +
                            "IS NULL) " +
                            "THEN RAISE(ABORT, 'Foreign key violation') END; " +
                            "END;";

    private static final String SQL_DELETE_TRIGGER_SENSOR_FOREIGN_KEY =
            "DROP TRIGGER IF EXISTS " + SenzorsDbContract.Sensor.TRIGGER_FOREIGN_KEY_INSERT;

    // trigger that use to define unique constraints in "sensor" table
    // 'user' and 'sensor_name' should ne unique together
    private static final String SQL_CREATE_TRIGGER_SENSOR_UNIQUE_KEY =
            "CREATE TRIGGER " + SenzorsDbContract.Sensor.TRIGGER_UNIQUE_KEY_INSERT + " " +
            "BEFORE INSERT ON " + SenzorsDbContract.Sensor.TABLE_NAME + " " +
                    "FOR EACH ROW BEGIN " +
                            "SELECT CASE WHEN((" +
                                    "SELECT " + SenzorsDbContract.User._ID + " " +
                                    "FROM " + SenzorsDbContract.User.TABLE_NAME + " " +
                                    "WHERE " + SenzorsDbContract.User._ID + "=" + "NEW." + SenzorsDbContract.Sensor.COLUMN_NAME_USER + ") " +
                            "IS NOT NULL) " +
                            "THEN RAISE(ABORT, 'Unique key violation') END; " +
                            "END;";

    private static final String SQL_DELETE_TRIGGER_SENSOR_UNIQUE_KEY =
            "DROP TRIGGER IF EXISTS " + SenzorsDbContract.Sensor.TRIGGER_UNIQUE_KEY_INSERT;

    /**
     * Init context
     * Init database
     * @param context application context
     */
    public SenzorsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * We are reusing one database instance in all over the app for better memory usage
     * @param context application context
     * @return db helper instance
     */
    synchronized static SenzorsDbHelper getInstance(Context context) {
        if (senzorsDbHelper == null) {
            senzorsDbHelper = new SenzorsDbHelper(context.getApplicationContext());
        }
        return (senzorsDbHelper);
    }

    /**
     * {@inheritDoc}
     */
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "OnCreate: creating db helper, db version - " + DATABASE_VERSION);
        db.execSQL(SQL_CREATE_SENSOR);
        db.execSQL(SQL_CREATE_USER);
        db.execSQL(SQL_CREATE_SHARED_USER);
        //db.execSQL(SQL_CREATE_TRIGGER_SENSOR_FOREIGN_KEY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // enable foreign key constraint here
        Log.d(TAG, "OnConfigure: Enable foreign key constraint");
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        Log.d(TAG, "OnUpgrade: updating db helper, db version - " + DATABASE_VERSION);
        db.execSQL(SQL_DELETE_SHARED_USER);
        db.execSQL(SQL_DELETE_SENSOR);
        db.execSQL(SQL_DELETE_USER);
        //db.execSQL(SQL_DELETE_TRIGGER_SENSOR_FOREIGN_KEY);
        onCreate(db);
    }

    /**
     * {@inheritDoc}
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
