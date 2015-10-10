package com.score.senz.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.score.senz.pojos.Senz;
import com.score.senz.pojos.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Do all database insertions, updated, deletions from here
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class SenzorsDbSource {

    private static final String TAG = SenzorsDbSource.class.getName();
    private static Context context;

    /**
     * Init db helper
     *
     * @param context application context
     */
    public SenzorsDbSource(Context context) {
        Log.d(TAG, "Init: db source");
        this.context = context;
    }

    /**
     * Insert user to database
     *
     * @param user user
     */
    public void createUser(User user) {
        Log.d(TAG, "AddUser: adding user - " + user.getUsername());
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.User.COLUMN_NAME_USERNAME, user.getUsername());

        // Insert the new row, if fails throw an error
        db.insertOrThrow(SenzorsDbContract.User.TABLE_NAME, SenzorsDbContract.User.COLUMN_NAME_USERNAME, values);
        db.close();
    }

    /**
     * Get user if exists in the database, other wise create user and return
     *
     * @param username username
     * @return user
     */
    public User getOrCreateUser(String username) {
        Log.d(TAG, "GetOrCreateUser: " + username);

        // get matching user if exists
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();
        Cursor cursor = db.query(SenzorsDbContract.User.TABLE_NAME, // table
                null, SenzorsDbContract.User.COLUMN_NAME_USERNAME + "=?", // constraint
                new String[]{username}, // prams
                null, // order by
                null, // group by
                null); // join

        if (cursor.moveToFirst()) {
            // have matching user
            // so get user data
            // we return id as password since we no storing users password in database
            String _id = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User._ID));
            String _username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_USERNAME));

            // clear
            cursor.close();
            db.close();

            Log.d(TAG, "have user, so return it: " + username);
            return new User(_id, _username);
        } else {
            // no matching user
            // so create user
            ContentValues values = new ContentValues();
            values.put(SenzorsDbContract.User.COLUMN_NAME_USERNAME, username);

            // inset data
            long id = db.insert(SenzorsDbContract.User.TABLE_NAME, SenzorsDbContract.User.COLUMN_NAME_USERNAME, values);
            db.close();

            Log.d(TAG, "no user, so user created:" + username);
            return new User(Long.toString(id), username);
        }
    }

    /**
     * Add senz to the database
     *
     * @param senz senz object
     */
    public void createSenz(Senz senz) {
        Log.d(TAG, "AddSensor: adding senz from - " + senz.getSender());
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.Senz.COLUMN_NAME_NAME, "Location");
        values.put(SenzorsDbContract.Senz.COLUMN_NAME_USER, senz.getSender().getId());

        // Insert the new row, if fails throw an error
        db.insertOrThrow(SenzorsDbContract.Senz.TABLE_NAME, SenzorsDbContract.Senz.COLUMN_NAME_VALUE, values);
        db.close();
    }

    public void updateSenz(User user, String value) {
        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getWritableDatabase();

        // content values to inset
        ContentValues values = new ContentValues();
        values.put(SenzorsDbContract.Senz.COLUMN_NAME_VALUE, value);

        // update
        db.update(SenzorsDbContract.Senz.TABLE_NAME,
                values,
                SenzorsDbContract.Senz.COLUMN_NAME_USER + " = ?",
                new String[]{String.valueOf(user.getId())});

        db.close();
    }

    /**
     * Get all sensors, two types of sensors here
     * 1. my sensors
     * 2. friends sensors
     *
     * @return sensor list
     */
    public List<Senz> getSenzes() {
        Log.d(TAG, "GetSensors: getting all sensor");
        List<Senz> sensorList = new ArrayList<Senz>();

        SQLiteDatabase db = SenzorsDbHelper.getInstance(context).getReadableDatabase();

        // get matching data via JOIN query
        String query = "SELECT * " +
                "FROM senz JOIN user " +
                "ON senz.user = user._id";
        Cursor cursor = db.rawQuery(query, null);

        // sensor/user attributes
        String _senzId;
        String _senzName;
        String _senzValue;
        String _userId;
        String _username;
        Senz senz = new Senz();

        // extract attributes
        while (cursor.moveToNext()) {
            HashMap<String, String> senzAttributes = new HashMap<>();

            // get senz attributes
            _senzName = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Senz.COLUMN_NAME_NAME));
            _senzValue = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.Senz.COLUMN_NAME_VALUE));

            // get user attributes
            _userId = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User._ID));
            _username = cursor.getString(cursor.getColumnIndex(SenzorsDbContract.User.COLUMN_NAME_USERNAME));

            // senz
            if (_senzValue != null && !_senzValue.isEmpty()) {
                senzAttributes.put(_senzName, _senzValue);
            }
            senz.setAttributes(senzAttributes);
            senz.setSender(new User(_userId, _username));

            // fill senz list
            sensorList.add(senz);
        }

        // clean
        cursor.close();
        db.close();

        Log.d(TAG, "GetSensors: sensor count " + sensorList.size());
        return sensorList;
    }

}
