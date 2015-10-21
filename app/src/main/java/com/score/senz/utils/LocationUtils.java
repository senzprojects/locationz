package com.score.senz.utils;

import android.location.Criteria;
import android.location.LocationManager;

/**
 * Created by eranga on 9/21/15.
 */
public class LocationUtils {
    /**
     * Get best available location provider via Criteria
     *
     * @return location provider
     */
    public static String getBestLocationProvider(LocationManager locationManager) {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);

        return locationManager.getBestProvider(criteria, true);
    }
}
