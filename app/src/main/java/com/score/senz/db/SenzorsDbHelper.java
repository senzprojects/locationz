package com.score.senz.db;

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
    private static final int DATABASE_VERSION = 17;
    private static final String DATABASE_NAME = "Senz.db";

    // data types, keywords and queries
    private static final String TEXT_TYPE = " TEXT";
    private static final String SQL_CREATE_SENZ =
            "CREATE TABLE " + SenzorsDbContract.Senz.TABLE_NAME + " (" +
                    SenzorsDbContract.Senz._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + ", " +
                    SenzorsDbContract.Senz.COLUMN_NAME_NAME + TEXT_TYPE + " NOT NULL" + ", " +
                    SenzorsDbContract.Senz.COLUMN_NAME_VALUE + TEXT_TYPE + ", " +
                    SenzorsDbContract.Senz.COLUMN_NAME_USER + TEXT_TYPE + " NOT NULL" + ", " +
                    "UNIQUE" + "(" + SenzorsDbContract.Senz.COLUMN_NAME_NAME + "," + SenzorsDbContract.Senz.COLUMN_NAME_USER + ")" +
            ")";
    private static final String SQL_CREATE_USER =
            "CREATE TABLE " + SenzorsDbContract.User.TABLE_NAME + " (" +
                    SenzorsDbContract.User._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + "," +
                    SenzorsDbContract.User.COLUMN_NAME_USERNAME + TEXT_TYPE + "UNIQUE NOT NULL" + "," +
                    SenzorsDbContract.User.COLUMN_NAME_NAME + TEXT_TYPE +
            " )";

    private static final String SQL_DELETE_SENZ =
            "DROP TABLE IF EXISTS " + SenzorsDbContract.Senz.TABLE_NAME;
    private static final String SQL_DELETE_USER =
            "DROP TABLE IF EXISTS " + SenzorsDbContract.User.TABLE_NAME;

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
        Log.d(TAG, SQL_CREATE_SENZ);
        Log.d(TAG, SQL_CREATE_USER);

        db.execSQL(SQL_CREATE_SENZ);
        db.execSQL(SQL_CREATE_USER);
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
        db.execSQL(SQL_DELETE_SENZ);
        db.execSQL(SQL_DELETE_USER);

        onCreate(db);
    }

    /**
     * {@inheritDoc}
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
