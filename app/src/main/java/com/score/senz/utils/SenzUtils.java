package com.score.senz.utils;

import android.content.Context;
import com.score.senz.db.SenzorsDbSource;
import com.score.senz.pojos.Sensor;
import com.score.senz.pojos.User;

/**
 * Utility class to handle sensors
 *
 * @author eranga herath(erangaeb@gmail.com)
 */
public class SenzUtils {

    /**
     * First time setup of the app
     * We add my sensors to database
     *
     * @param context application context
     * @param user senz user
     */
    public static void addMySensorsToDb(Context context, User user) {
        Sensor sensor = new Sensor("0", "Location", "LocationValue", true, user, null);
        new SenzorsDbSource(context).addSensor(sensor);
    }

}
